package spc.webos.persistence.jdbc.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.persistence.IPersistence;
import spc.webos.util.StringX;

public class DynamicDSAdvice
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected Object jt(String jt, ProceedingJoinPoint pjp) throws Throwable
	{
		String old = IPersistence.CURRENT_JT.get();
		boolean set = (force || old == null) && !StringX.nullity(jt) && !jt.equals(old);
		if (set)
		{
			log.info("JT routing:{}, old:{}, force:{}", jt, old, force);
			IPersistence.CURRENT_JT.set(jt);
		}
		try
		{
			return pjp.proceed();
		}
		finally
		{
			if (set)
			{
				IPersistence.CURRENT_JT.set(old);
				log.info("JT remove:{}, old:{}, force:{}", jt, old, force);
			}
		}
	}

	protected Object routing(String ds, ProceedingJoinPoint jp) throws Throwable
	{
		if (DynamicDataSource.current() != null && !StringX.nullity(ds)
				&& !DynamicDataSource.current().equals(ds))
		{ // ��ǰ�̻߳����Ѿ���������Դ�л���������л���ͬ����Դ���쳣����Ϊ��֧�ֶ�����Դ����
			throw new AppException(AppRetCode.DB_MULTI_CHANGE_DS,
					new Object[] { ((MethodSignature) jp.getSignature()).getMethod().getName(),
							DynamicDataSource.current(), ds });
		}
		return set(ds, jp);
	}

	protected Object set(String ds, ProceedingJoinPoint jp) throws Throwable
	{
		// ��Ϊ�������Ƕ�׵��ã����ǵ�����ԭ����������Դֻ����һ��
		// �����ǰ�̻߳����Ѿ�����������Դ���Ժ�ķ�����ò��������ã�����˭����˭��������̻߳���
		String oldJT = IPersistence.CURRENT_JT.get(); // �л�DSʱ����Ҫȡ������JT·�����á�
		String oldDS = DynamicDataSource.current();
		boolean set = (force || oldDS == null) && !StringX.nullity(ds) && !ds.equals(oldDS);
		if (set)
		{
			DynamicDataSource.current(ds);
			IPersistence.CURRENT_JT.set(null);
			log.info("DS routing:{}, oldDS:{}, oldJT:{}, force:{}", ds, oldDS, oldJT, force);
		}
		else log.debug("NO DS routing:{}, old:{}, force:{}", ds, oldDS, force);
		try
		{
			return jp.proceed();
		}
		finally
		{
			if (set)
			{
				log.info("DS remove:{}, oldDS:{}, oldJT:{}", DynamicDataSource.current(), oldDS,
						oldJT);
				DynamicDataSource.current(oldDS); // ��յ�ǰ�̻߳���������Դ����
				IPersistence.CURRENT_JT.set(oldJT);
			}
		}
	}

	protected boolean force; // ǿ���л�����ǰ����Դ, ��Ҫ���ĳЩ�����Ƕ�������(���ݿ���ˮ������)

	public void setForce(boolean force)
	{
		this.force = force;
	}
}
