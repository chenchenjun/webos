package spc.webos.persistence.loader;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import spc.webos.constant.Common;
import spc.webos.model.SQLPO;
import spc.webos.persistence.SQLItem;
import spc.webos.util.FTLUtil;
import spc.webos.util.StringX;

/**
 * 753, ֧�������ݿ�������SQL
 * 
 * @author chenjs
 * 
 */
public class DBSQLItemLoader
{
	static Logger log = LoggerFactory.getLogger(DBSQLItemLoader.class);

	public static void loadSQLItem(List<SQLPO> items, Map sqlMap, boolean productMode)
			throws Exception
	{
		if (items == null) return;
		log.info("SQLItem in DB:" + items.size());
		for (int i = 0; i < items.size(); i++)
		{
			SQLPO vo = (SQLPO) items.get(i);
			addSQLItem2Map(parseItem(new SQLItem(), vo), vo, sqlMap);
		}
	}

	public static void addSQLItem2Map(SQLItem item, SQLPO vo, Map sqlMap)
	{
		Map module = (Map) sqlMap.get(vo.getMdl().trim());
		if (module == null) module = new HashMap();
		module.put(vo.getId().trim(), item);
		sqlMap.put(vo.getMdl().trim(), module);
	}

	public static SQLItem parseItem(SQLItem item, SQLPO vo) throws Exception
	{
		item.type = (short) ((int) vo.getType());
		item.jt = vo.getDs(); // ��ȡjt
		item.slave = vo.getSlave(); // ��ȡ������Դ����
		if (!StringX.nullity(vo.getInjection()))
			item.injection = StringX.split(vo.getInjection(), ",");

		if ("public".equalsIgnoreCase(vo.getAuth())) item.auth = SQLItem.AUTH_public;
		else if ("login".equalsIgnoreCase(vo.getAuth())) item.auth = SQLItem.AUTH_login;

		if (item.type == SQLItem.SELECT)
		{ // select
			item.resultClass = StringX.null2emptystr(vo.getResultClass(),
					SQLItem.DEFAULT_RESULT_CLASS);

			// �Ƿ���ֶ�����Сд����
			// String columns = ele.attributeValue("column");
			// if (!StringX.nullity(columns))
			// {
			// item.column = new HashMap();
			// String[] cols = columns.split(",");
			// for (int i = 0; i < cols.length; i++)
			// item.column.put(cols[i].toLowerCase(), cols[i]);
			// }

			// һ��SQL�������洦��, �����Ҫ������Service���Լ�ʵ��.
			// item.cacheBeanName = ele.attributeValue("cacheBeanName", null);
			item.firstRowOnly = Common.YES.equals(vo.getFirstRowOnly());
		}
		item.prepared = Common.YES.equals(vo.getPrepared());
		item.procedure = Common.YES.equals(vo.getProc());
		StringBuilder sql = new StringBuilder(); // ֧�ֶ���ֶ����ϴ��sql
		sql.append(StringX.null2emptystr(vo.getText()));
		sql.append(StringX.null2emptystr(vo.getText1()));
		sql.append(StringX.null2emptystr(vo.getText2()));
		sql.append(StringX.null2emptystr(vo.getText3()));
		item.sql = SQLItemXmlLoader.formatSQL(sql.toString());
		if ("1".equals(vo.getPaging()))
		{ // 2017-02-24�������ݿ����÷�ҳ����
			item.sql = SQLItemXmlLoader.paging(item.sql,
					StringX.null2emptystr(vo.getStartTag(), "start"),
					StringX.null2emptystr(vo.getLimitTag(), "end"), vo.getEndTag());
		}
		Configuration cfg = new Configuration();
		cfg.setNumberFormat(FTLUtil.numberFormat);
		item.t = new Template(vo.getMdl() + '.' + vo.getId(), new StringReader(item.sql), cfg);
		return item;
	}
}
