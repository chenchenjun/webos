package spc.webos.tcc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.model.TccTransLogPO;
import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.tcc.TCCTransactional.XidParamPath;
import spc.webos.tcc.service.TccAtomService;
import spc.webos.util.FileUtil;

/**
 * ����tccԭ�ӷ���implʵ���࣬������ɵǼ�tcc_tranlog��
 * 
 * @author chenjs
 *
 */
public class TccAtomAdvice extends TccAdvice
{
	protected TccAtomService tccAtomService;

	// tcc atom serviceʵ�������� tryXXX����
	public Object doTry(ProceedingJoinPoint jp) throws Throwable
	{
		// 1. ��ǰ�����Ƿ�����ԭ��ʵ�ַ���
		Method methodImpl = jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
				((MethodSignature) jp.getSignature()).getParameterTypes());
		TCCTransactional tcc = methodImpl.getAnnotation(TCCTransactional.class);
		if (tcc == null) return jp.proceed(); // ��ǰtryִ�п�����rpc�ͻ��ˣ�����implʵ�ֶ�
		String sn = null; // ����ԭ�ӽ��׵�ҵ����ˮ����Ϣ
		// ͨ��ע��ģʽ����ȡ��ǰԭ�ӽ��׵�ҵ����ˮ�ţ���������ˮ�Ŵ���TM
		MethodSignature sig = (MethodSignature) jp.getSignature();
		Class<?> declar = sig.getDeclaringType();
		String m = sig.getName();
		String clazz = declar.getCanonicalName();
		Method method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
		Object[] args = jp.getArgs();
		Object[] tidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (tidParamAndArg != null)
			sn = getXid((XidParamPath) tidParamAndArg[0], args, (int) tidParamAndArg[1]);
		log.info("impl try sn:{}", sn);
		TccTransLogPO po = new TccTransLogPO(sn);
		po.setArgs(new ByteArrayBlob(FileUtil.fst(args)));
		po.setMethod(m);
		po.setClazz(clazz);
		po.setTypes(Arrays.toString(sig.getParameterTypes()));
		po.setTryTm(FastDateFormat.getInstance(Transaction.DF_ALL).format(new Date()));
		if (!tcc.tranlog()) return jp.proceed();
		// ��ǰԭ�ӷ���impl��Ҫ�Ǽ�tranlog��
		tccAtomService.doTring(po); // start try
		Object ret = jp.proceed();
		tccAtomService.doTried(new TccTransLogPO(sn)); // finish
		return ret;
	}

	// tcc atom serviceʵ�������� confirmXXX����
	public Object doConfirm(ProceedingJoinPoint jp) throws Throwable
	{
		Object ret = jp.proceed();
		// 1. ��ǰ�����Ƿ�����ԭ��ʵ�ַ���
		Method method = jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
				((MethodSignature) jp.getSignature()).getParameterTypes());
		TCCTransactional trans = method.getAnnotation(TCCTransactional.class);
		if (trans == null || !trans.tranlog()) return ret;

		// ��Ҫ�Ǽ�tranlog��
		MethodSignature sig = (MethodSignature) jp.getSignature();
		method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
		String sn = null; // ����ԭ�ӽ��׵�ҵ����ˮ����Ϣ
		// ͨ��ע��ģʽ����ȡ��ǰԭ�ӽ��׵�ҵ����ˮ�ţ���������ˮ�Ŵ���TM
		Object[] args = jp.getArgs();
		Object[] tidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (tidParamAndArg != null)
			sn = getXid((XidParamPath) tidParamAndArg[0], args, (int) tidParamAndArg[1]);

		log.info("impl confirm sn:{}", sn);
		int rows = tccAtomService.doConfirm(new TccTransLogPO(sn));
		// confirmʱ�Ҳ���ԭ����
		if (rows <= 0) throw new AppException(AppRetCode.CMM_BIZ_ERR);
		return ret;
	}

	// tcc atom serviceʵ�������� cancelXXX����
	public Object doCancel(ProceedingJoinPoint jp) throws Throwable
	{
		Object ret = jp.proceed();
		// 1. ��ǰ�����Ƿ�����ԭ��ʵ�ַ���
		Method method = jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
				((MethodSignature) jp.getSignature()).getParameterTypes());
		TCCTransactional trans = method.getAnnotation(TCCTransactional.class);
		if (trans == null || !trans.tranlog()) return ret;

		// ��Ҫ�Ǽ�tranlog��
		MethodSignature sig = (MethodSignature) jp.getSignature();
		method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
		String sn = null; // ����ԭ�ӽ��׵�ҵ����ˮ����Ϣ
		// ͨ��ע��ģʽ����ȡ��ǰԭ�ӽ��׵�ҵ����ˮ�ţ���������ˮ�Ŵ���TM
		Object[] args = jp.getArgs();
		Object[] tidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (tidParamAndArg != null)
			sn = getXid((XidParamPath) tidParamAndArg[0], args, (int) tidParamAndArg[1]);

		log.info("impl cancel sn:{}", sn);
		tccAtomService.doCancel(new TccTransLogPO(sn));
		return ret;
	}

	public void setTccAtomService(TccAtomService tccAtomService)
	{
		this.tccAtomService = tccAtomService;
	}
}
