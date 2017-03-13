package spc.webos.persistence.jdbc.datasource;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.config.AppConfig;
import spc.webos.util.FTLUtil;
import spc.webos.util.SpringUtil;

/**
 * ����������Ϣ�Զ��������з�Ƭ����Դ��spring context, ����MasterSlaveDS����
 * ����eacģ�飬�����eac.jdbc.shard.num=100������ �Զ�����eac.jdbc.shard.001.url,
 * eac.jdbc.shard.001.username, eac.jdbc.shard.001.passowrd
 * 
 * @author chenjs
 *
 */
public class ShardingDS
{
	protected String db;
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public void init() throws Exception
	{
		int num = AppConfig.getInstance().getProperty(db + ".jdbc.shard.num", -1);
		log.info("sharding ds:{}, num:{}", db, num);
		if (num < 0) return;
		Map<String, Object> root = new HashMap<>();
		root.put("db", db);
		String beans = FTLUtil.ftl("webos/sharding_ds", root);
		log.debug("sharding ds:{}, beans:\n{}", db, beans);
		SpringUtil.registerXMLBean(beans, false);
	}

	public void setDb(String db)
	{
		this.db = db;
	}
}
