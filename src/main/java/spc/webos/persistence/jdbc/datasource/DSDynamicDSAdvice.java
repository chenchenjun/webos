package spc.webos.persistence.jdbc.datasource;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * �������ذ���·�����̶�����ĳЩ���µķ���ʹ��ĳ������Դ
 * 
 * @author chenjs
 *
 */
public class DSDynamicDSAdvice extends DynamicDSAdvice
{
	public Object routing(ProceedingJoinPoint pjp) throws Throwable
	{ // RulesDynamicDSAdvice ����������DDDSAִ��
		// System.out.print("DefaultDynamicDSAdvice:"+jp.getSignature());
		// 939_20170302 �����ǰ�ӿ�������DataSource��������������Դ��������ע��Ϊ׼
		Method method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(),
				((MethodSignature) pjp.getSignature()).getParameterTypes());
		if (method.getAnnotation(DataSource.class) != null)
		{
			log.debug("DataSource exists...");
			return pjp.proceed();
		}

		return set(ds, pjp);
	}

	String ds;

	public void setDs(String ds)
	{
		this.ds = ds;
	}
}
