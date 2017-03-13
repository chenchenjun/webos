package spc.webos.tcc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.tcc.TCCTransactional.XidParamPath;
import spc.webos.tcc.repository.MemoryTCCRepository;
import spc.webos.util.StringX;

public class TccAdvice
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	public static ThreadLocal<Transaction> CUR_TRANSACTION = new ThreadLocal<>();
	public static ThreadLocal<TccAdvice> CUR_ADVICE = new ThreadLocal<>();
	@Autowired(required = false)
	protected Repository repository = new MemoryTCCRepository();

	// ͬ��tcc���񣬵�ǰtcc�������з���ͬ��ִ��
	public Object tcc(ProceedingJoinPoint jp) throws Throwable
	{
		MethodSignature sig = (MethodSignature) jp.getSignature();
		Method method = jp.getTarget().getClass().getMethod(sig.getName(), sig.getParameterTypes());
		TCCTransactional trans = method.getAnnotation(TCCTransactional.class);
		if (trans == null)
		{ // ���Դӽӿ��л�ȡע��
			method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
			trans = method.getAnnotation(TCCTransactional.class);
		}
		if (trans == null)
		{
			log.info("No TCC Xid in {}", method.toString());
			return jp.proceed(); // û������tcc��������Ӧ
		}
		if (trans.asyn()) return atcc(trans, jp); // ������첽tcc����

		String xid = repository.createXid(); // ������һ��Ψһ����ID
		Transaction transaction = begin(trans, xid, jp, method);
		boolean tryFail = true;
		try
		{
			Object ret = jp.proceed();
			tryFail = false;
			confirm(transaction); // confirm����ʧ��
			return ret;
		}
		catch (Throwable t)
		{
			if (tryFail) cancel(transaction, t); // cancel����ʧ��, ���ʧ��ֻ���ڲ���¼
			throw t;
		}
		finally
		{
			removeTransaction();
		}
	}

	// ����ע��·����ȡ�������
	protected String getXid(XidParamPath path, Object[] args, int index)
	{
		if (StringX.nullity(path.value()))
		{ // ��ǰ��������xid String����
			if (StringX.nullity((String) args[index])) return (String) args[index];
			log.debug("Xid arg:{}", args[index]);
			return (String) args[index];
		}
		BeanWrapperImpl wrapper = new BeanWrapperImpl(false);
		wrapper.setWrappedInstance(args[index]);

		String xid = (String) wrapper.getPropertyValue(path.value());
		log.debug("Xid path:{}, xid:{}", path.value(), xid);
		return xid;
	}

	// �ҵ���һ��ע��DSParam�Ĳ���ע����Ϣ���Լ���ǰ����ֵ
	protected Object[] findXIDParamAndArg(Annotation[][] annotations, Object[] args)
	{
		if (annotations == null) return null;
		for (int i = 0; i < args.length; i++)
			for (int j = 0; annotations[i] != null && j < annotations[i].length; j++)
				if (annotations[i][j] instanceof XidParamPath)
					return new Object[] { annotations[i][j], i };
		return null;
	}

	// �첽tcc���ã���ǰtcc��������ķ���������첽ִ�У���Ҫ���ֻ�����������Ϣ
	public Object atcc(TCCTransactional trans, ProceedingJoinPoint jp) throws Throwable
	{
		CUR_ADVICE.set(this); // �����첽tcc
		try
		{
			begin(trans, repository.createXid(), jp,
					((MethodSignature) jp.getSignature()).getMethod());
			return jp.proceed();
		}
		finally
		{
			removeTransaction();
			CUR_ADVICE.set(null);
		}
	}

	public Object doTry(ProceedingJoinPoint jp) throws Throwable
	{
		Transaction transaction = currentTransaction();
		if (transaction == null) return jp.proceed();

		MethodSignature sig = (MethodSignature) jp.getSignature();
		Class<?> declar = sig.getDeclaringType();
		String m = sig.getName();
		String clazz = declar.getCanonicalName();
		Method method = declar.getMethod(sig.getName(), sig.getParameterTypes());
		TCCTransactional tcc = method.getAnnotation(TCCTransactional.class);
		if (tcc == null)
		{
			log.info("No TCC interface in: {}", method.toString());
			return jp.proceed();
		}

		// ����ǰ�����ӷ�����뵽��Դ������������
		int seq = transaction.getTerminatorsList().size();
		String sn = null; // ����ԭ�ӽ��׵�ҵ����ˮ����Ϣ
		// ͨ��ע��ģʽ����ȡ��ǰԭ�ӽ��׵�ҵ����ˮ�ţ���������ˮ�Ŵ���TM
		Object[] args = jp.getArgs();
		Object[] tidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (tidParamAndArg != null)
			sn = getXid((XidParamPath) tidParamAndArg[0], args, (int) tidParamAndArg[1]);

		Terminator t = repository.addTerminator(transaction, tcc, sn, seq, jp.getThis(), clazz, m,
				sig.getParameterTypes(), args);
		if (!tcc.doTry())
		{
			log.info("TX no try:({}_{},{})", transaction.xid, seq, sn);
			return null;
		}

		log.info("TX try:({}_{},{}), {}.{}", transaction.xid, seq, sn, clazz, m);
		try
		{
			// Object ret = jp.proceed(args);
			Object ret = jp.proceed();
			t.status = Transaction.STATUS_TRIED;
			return ret;
		}
		catch (AppException ae)
		{
			if (AppRetCode.REPEAT_SN.equalsIgnoreCase(ae.getCode()))
			{ // �����ǰtryԭ�ӷ�������Ϊ��ˮ���ظ������ܷ���cancel
				log.info("TCC repeat atom sn, xid:{}_{}, sn:{}", t.xid, t.seq, t.sn);
				t.cannotCancel = true;
				t.status = Transaction.STATUS_TRY_FAIL;
				repository.updateStatus(t);
			}
			throw ae;
		}
	}

	public Transaction begin(TCCTransactional tcc, String xid, ProceedingJoinPoint jp,
			Method method) throws Exception
	{
		Transaction transaction = new Transaction(xid);
		transaction.proxy = method.toString();

		// ͨ��ע��ģʽ����ȡҵ��sn������sn��������TM�й��������ϵ
		Object[] args = jp.getArgs();
		Object[] xidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (xidParamAndArg != null) transaction.sn = getXid((XidParamPath) xidParamAndArg[0], args,
				(int) xidParamAndArg[1]);
		log.info("{} begin:({},{}), {}", tcc.asyn() ? "AsynTX" : "TX", xid, transaction.sn,
				transaction.proxy);

		startTransaction(transaction);
		transaction.args = args;
		repository.create(transaction); // ����ǰ������Ϣ���뵽������Դ��
		return transaction;
	}

	public static void startTransaction(Transaction transaction)
	{
		CUR_TRANSACTION.set(transaction);
	}

	public static void removeTransaction()
	{
		CUR_TRANSACTION.remove();
	}

	public void confirm(Transaction transaction) throws Exception
	{
		log.info("TX confirm:{}, terminators:{}, proxy:{}", transaction.xid,
				transaction.getTerminatorsList().size(), transaction.proxy);
		try
		{
			transaction.confirm();
		}
		catch (Exception e)
		{
			log.warn("TX confirm Fail:" + transaction.xid + ", sn:" + transaction.sn + ", "
					+ transaction.proxy, e);
			throw e;
		}
		finally
		{ // ����ִ�гɹ���������״̬����²��ɹ���������
			repository.updateStatus(transaction); // ֻ�޸�����״̬���Ƿ�ɾ������Դ���ڲ�����
		}
	}

	public void cancel(Transaction transaction, Throwable t)
	{
		log.info("TX cancel:({},{}), terminators:{}  by ### {}", transaction.xid, transaction.sn,
				transaction.getTerminatorsList().size(), t.toString());
		try
		{
			transaction.cancel();
		}
		catch (Exception e)
		{ // cancel�쳣���������������ߣ�����ڲ�����������������ֻ����tryXXҵ���쳣
			log.warn("TX cancel fail:" + transaction.xid + ", sn:" + transaction.sn + ", "
					+ transaction.proxy, e);
		}
		finally
		{ // ����ִ�гɹ���������״̬����²��ɹ���������
			repository.updateStatus(transaction); // ֻ�޸�����״̬���Ƿ�ɾ������Դ���ڲ�����
		}
	}

	public void setRepository(Repository repository)
	{
		this.repository = repository;
	}

	public Repository getRepository()
	{
		return repository;
	}

	public static Transaction currentTransaction()
	{
		return CUR_TRANSACTION.get();
	}
}
