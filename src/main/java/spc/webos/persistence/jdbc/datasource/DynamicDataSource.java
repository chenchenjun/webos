package spc.webos.persistence.jdbc.datasource;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.alibaba.druid.pool.DruidDataSource;

import spc.webos.util.StringX;

/**
 * ��̬�л�����Դ��������ÿ������Դ������
 * 
 * @author chenjs
 * 
 */
public class DynamicDataSource extends AbstractRoutingDataSource implements ApplicationContextAware
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected DataSource defaultDataSource;
	protected Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();
	protected boolean targetDSIsNull = true; // ��ʾ��������ԴΪ�գ�ʹ���Զ�spring contextɨ�����
	protected String defaultDS;
	private static final ThreadLocal<String> DS = new ThreadLocal<>(); // ��ǰ�̻߳�����������Դ��Ϣ

	@PostConstruct
	public void init()
	{
		String[] dses = cxt.getBeanNamesForType(ConnectionPoolDataSource.class);
		log.info("DS in spring:{}, targetDSIsNull:{}", Arrays.toString(dses), targetDSIsNull);
		if (!targetDSIsNull) return;
		if (dses == null || dses.length == 0)
		{
			super.setTargetDataSources(targetDataSources);
			return;
		}
		for (String ds : dses)
			targetDataSources.put(ds, cxt.getBean(ds, ConnectionPoolDataSource.class));
		super.setTargetDataSources(targetDataSources);

		if (!StringX.nullity(defaultDS))
		{ // ����Ĭ������Դ
			log.info("default DS:{}", defaultDS);
			super.setDefaultTargetDataSource(
					defaultDataSource = cxt.getBean(defaultDS, DataSource.class));
		}
	}

	@Override
	protected DataSource determineTargetDataSource()
	{ // ����ԭ���������ж�̬����Դ����ָ����spring bean id���֣���̬��spring context�л�ȡ
		String lookupKey = (String) determineCurrentLookupKey();
		if (StringX.nullity(lookupKey)) return defaultDataSource;
		DataSource ds = (DataSource) targetDataSources.get(lookupKey);
		if (ds != null) return ds;
		ds = cxt.getBean(lookupKey, DataSource.class);
		if (ds != null) targetDataSources.put(lookupKey, ds);
		else
		{
			log.warn("Cannot find DS:{}", lookupKey);
			throw new IllegalStateException(
					"Cannot determine target DataSource for lookup key [" + lookupKey + "]");
		}
		return ds;
	}

	@Override
	public void setTargetDataSources(Map<Object, Object> targetDataSources)
	{
		super.setTargetDataSources(targetDataSources);
		targetDSIsNull = false;
		this.targetDataSources.putAll(targetDataSources);
	}

	public String getCurrentDbType()
	{
		try
		{
			javax.sql.DataSource ds = determineTargetDataSource();
			if (ds instanceof DruidDataSource)
				return ((DruidDataSource) ds).getDbType().toUpperCase();
		}
		catch (Exception e)
		{
		}
		return null;
	}

	public String getCurrentDbName()
	{
		try
		{
			javax.sql.DataSource ds = determineTargetDataSource();
			if (ds instanceof DruidDataSource) return ((DruidDataSource) ds).getName();
		}
		catch (Exception e)
		{
		}
		return null;
	}

	protected Object determineCurrentLookupKey()
	{
		return DS.get();
	}

	public static void current(String ds)
	{
		DS.set(ds);
	}

	public static String current()
	{
		return DS.get();
	}

	public void setDefaultDS(String defaultDS)
	{
		this.defaultDS = defaultDS;
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		cxt = arg0;
	}

	ApplicationContext cxt;
}
