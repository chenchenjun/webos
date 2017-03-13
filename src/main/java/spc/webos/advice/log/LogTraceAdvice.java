package spc.webos.advice.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;

import spc.webos.advice.log.LogTrace.LTPath;
import spc.webos.advice.log.LogTrace.LogTraceNoType;
import spc.webos.service.seq.UUID;
import spc.webos.service.seq.impl.TimeMillisUUID;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.common.SUI;

public class LogTraceAdvice
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected UUID uuid = new TimeMillisUUID(0);

	public Object trace(ProceedingJoinPoint pjp) throws Throwable
	{
		// 1. ��ǰ�����Ƿ���������Ҫ��־׷��
		Method method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(),
				((MethodSignature) pjp.getSignature()).getParameterTypes());
		LogTrace trace = method.getAnnotation(LogTrace.class);
		if (trace == null)
		{ // ��ǰ�ӿ��Ƿ�������־׷��
			MethodSignature sig = (MethodSignature) pjp.getSignature();
			method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
			trace = method.getAnnotation(LogTrace.class);
			if (trace == null) return pjp.proceed();
		}

		// 2. ��ǰ�̻߳����Ѿ���������־׷����Ϣ����ֱ�ӷ���
		if (LogUtil.getTraceNo() != null && !trace.replace()) return pjp.proceed();

		// 3. �ҵ���־׷�ٺ���Ϣ
		String location = trace.location();
		if (StringX.nullity(location)) location = method.getName();
		boolean set = LogUtil.setTraceNo(
				StringX.null2emptystr(trace.appCd(), SpringUtil.APPCODE)
						+ getTraceNo(trace, method.getParameterAnnotations(), pjp.getArgs()),
				location, trace.replace());
		try
		{
			return pjp.proceed();
		}
		finally
		{ // ����ǵ�ǰ�߳�������logutil.traceno�� ��ǰ�߳�ִ���������̻߳�����˭����˭���
			if (set) LogUtil.removeTraceNo();
		}
	}

	protected String getTraceNo(LogTrace trace, Annotation[][] annotations, Object[] args)
	{
		if (trace.value() == LogTraceNoType.AUTO) return uuid.format(uuid.uuid());
		if (trace.value() == LogTraceNoType.PARAM) return getTraceNo(annotations, args);
		if (trace.value() == LogTraceNoType.REQUEST_USER)
		{
			SUI sui = SUI.SUI.get();
			if (sui != null) return sui.getUserCode() + "-" + sui.requestCount();
			return null;
		}
		return null;
	}

	public void setWorkId(int id)
	{
		uuid = new TimeMillisUUID(id);
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	// �ҵ���һ��ע��DSParam�Ĳ���ע����Ϣ���Լ���ǰ����ֵ
	protected String getTraceNo(Annotation[][] annotations, Object[] args)
	{
		if (annotations == null) return null;
		for (int i = 0; i < args.length; i++)
			for (int j = 0; annotations[i] != null && j < annotations[i].length; j++)
				if (annotations[i][j] instanceof LTPath)
					return getTraceNo((LTPath) annotations[i][j], args[i]);
		return null;
	}

	protected String getTraceNo(LTPath path, Object arg)
	{
		if (StringX.nullity(path.value())) return arg != null ? arg.toString() : path.nullValue(); // ���ע��·��û����ֱ��ʹ�ô˲���
		BeanWrapperImpl wrapper = new BeanWrapperImpl(false);
		wrapper.setWrappedInstance(arg);
		Object value = wrapper.getPropertyValue(path.value());
		log.debug("Path:{}, value:{}, arg:{}", path.value(), value, arg);
		return value == null ? path.nullValue() : value.toString();
	}
}
