package spc.webos.persistence.loader;

/**
 * ��ͨSQL�����ļ�������
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import freemarker.template.Configuration;
import freemarker.template.Template;
import spc.webos.persistence.ReportItem;
import spc.webos.persistence.SQLItem;
import spc.webos.persistence.Script;
import spc.webos.util.FTLUtil;
import spc.webos.util.StringX;

public class SQLItemXmlLoader
{
	static Logger log = LoggerFactory.getLogger(SQLItemXmlLoader.class);

	public static void readSqlDir(ResourceLoader resourceLoader, String dir, Map sqlMap,
			boolean productMode) throws Exception
	{
		readSqlDir(resourceLoader.getResource(dir), sqlMap, productMode);
	}

	// 2015-11-16 ���ڼ��ض��jar:META-INF/webos/persistence/sql/*.xml
	public static void readSqlDir(Resource path, Map sqlMap, boolean productMode) throws Exception
	{
		File file = path.getFile();
		if (!file.exists()) log.warn("cannot load sql file dir: " + file.getAbsolutePath());
		File[] files = file.listFiles();
		for (int i = 0; files != null && i < files.length; i++)
		{
			readSqlFile(files[i].getName(), new FileInputStream(files[i]), sqlMap, productMode);
		}
	}

	// ��һ��������sql�����ļ���֧��jarģʽ
	public static void readSqlFile(String fileName, InputStream location, Map sqlMap,
			boolean productMode) throws Exception
	{
		if (!(fileName.endsWith(".xml") || fileName.endsWith(".ftl")))
		{
			log.warn("file:" + fileName + " is not .xml or .ftl!!!");
			return; // ֧��sql�����ļ���ftl�ļ�����д
		}
		log.debug("sqlfile:{}", fileName);
		String namespace = fileName.substring(0, fileName.length() - 4).toLowerCase();
		Map namespaceSQLMap = new HashMap();
		try
		{
			boolean bool = parseXmlFile(namespace, location, namespaceSQLMap, productMode); // �ڵ���ģʽ�£�ĳЩ�ļ�Ҳ����ֻ����һ�Σ�������˵�ģ��
			if (!productMode && bool) sqlMap.put(namespace, fileName);
			else sqlMap.put(namespace, namespaceSQLMap);
		}
		catch (Exception e)
		{
			System.err.println("load sql config file failure:" + namespace);
			throw e;
		}
		finally
		{
			location.close();
		}
	}

	/**
	 * ��ĳһSQL�����ļ�(�����ռ�)������Sql����һ��Map�з���
	 * 
	 * @param location
	 * @return
	 * @throws Exception
	 */
	public static boolean parseXmlFile(String module, InputStream location, Map namespaceSQLMap,
			boolean forceLoading) throws Exception
	{
		SAXReader reader = new SAXReader(false);
		Document doc = reader.read(location);
		Element root = doc.getRootElement();
		boolean debug = new Boolean(root.attributeValue("debug", StringX.FALSE)).booleanValue();
		if (debug && !forceLoading) return true;

		// ��ȡ�����ļ���Ĭ������Դ
		String ds = root.attributeValue("jt");
		if (ds == null || ds.length() == 0) ds = root.attributeValue("ds");
		String slave = root.attributeValue("slave");
		// Map namespaceSQLMap = new HashMap(); // ÿһ��namespace����һ��Map��, ��߲����ٶ�
		String id = "id";
		// �������select SQL
		List sqls = root.elements("select");
		for (int i = 0; i < sqls.size(); i++)
		{
			Element ele = (Element) sqls.get(i);
			SQLItem item = new SQLItem();
			item.jt = ds;
			item.slave = slave;
			namespaceSQLMap.put(ele.attributeValue(id).toLowerCase(),
					parseItem(item, module + "." + ele.attributeValue(id), ele, SQLItem.SELECT));
		}
		sqls = root.elements("delete");
		for (int i = 0; i < sqls.size(); i++)
		{
			Element ele = (Element) sqls.get(i);
			SQLItem item = new SQLItem();
			item.jt = ds;
			namespaceSQLMap.put(ele.attributeValue(id).toLowerCase(),
					parseItem(item, module + "." + ele.attributeValue(id), ele, SQLItem.DELETE));
		}
		sqls = root.elements("update");
		for (int i = 0; i < sqls.size(); i++)
		{
			Element ele = (Element) sqls.get(i);
			SQLItem item = new SQLItem();
			item.jt = ds;
			namespaceSQLMap.put(ele.attributeValue(id).toLowerCase(),
					parseItem(item, module + "." + ele.attributeValue(id), ele, SQLItem.UPDATE));
		}
		sqls = root.elements("insert");
		for (int i = 0; i < sqls.size(); i++)
		{
			Element ele = (Element) sqls.get(i);
			SQLItem item = new SQLItem();
			item.jt = ds;
			namespaceSQLMap.put(ele.attributeValue(id).toLowerCase(),
					parseItem(item, module + "." + ele.attributeValue(id), ele, SQLItem.INSERT));
		}
		sqls = root.elements("prepared");
		for (int i = 0; i < sqls.size(); i++)
		{
			Element ele = (Element) sqls.get(i);
			SQLItem item = new SQLItem();
			item.jt = ds;
			parseItem(item, module + "." + ele.attributeValue(id), ele, SQLItem.UPDATE);
			item.prepared = true;
			namespaceSQLMap.put(ele.attributeValue(id).toLowerCase(), item);
		}

		sqls = root.elements("procedure");
		for (int i = 0; i < sqls.size(); i++)
		{
			Element ele = (Element) sqls.get(i);
			SQLItem item = new SQLItem();
			item.jt = ds;
			parseItem(item, module + "." + ele.attributeValue(id), ele, SQLItem.CALL);
			item.prepared = true;
			item.procedure = true;
			namespaceSQLMap.put(ele.attributeValue(id).toLowerCase(), item);
		}

		// report
		sqls = root.elements("report");
		for (int i = 0; i < sqls.size(); i++)
		{
			Element ele = (Element) sqls.get(i);
			ReportItem item = parseReportItem(module + "." + ele.attributeValue(id), ele);
			namespaceSQLMap.put(ele.attributeValue(id).toLowerCase(), item);
		}
		return false;
	}

	/*
	 * ���ر�������
	 */
	static ReportItem parseReportItem(String itemId, Element ele) throws Exception
	{
		ReportItem item = new ReportItem();

		if (ele.attributeValue("import") != null) item.dependence = StringX
				.split(ele.attributeValue("import").replace('.', '_').toLowerCase(), ",");

		// ��ȡ����������,���ò���messageformat
		String rowIndex = ele.attributeValue("rowIndex");
		if (!StringX.nullity(rowIndex)) item.rowIndex = rowIndex;

		// ��ȡǰ��,���ú����б�
		String fns = ele.attributeValue("preFnodes");
		if (!StringX.nullity(fns)) item.preFnodes = fns.split(",");
		fns = ele.attributeValue("postFnodes");
		if (!StringX.nullity(fns)) item.postFnodes = fns.split(",");

		// ��ȡ��̬�ű�
		List scripts = ele.elements("script");
		for (int i = 0; scripts != null && i < scripts.size(); i++)
		{
			Element e = (Element) scripts.get(i);
			boolean pre = new Boolean(e.attributeValue("pre", StringX.FALSE)).booleanValue();
			boolean main = new Boolean(e.attributeValue("main", StringX.FALSE)).booleanValue();
			String strType = e.attributeValue("type", "bs");
			int type = 0;
			if (strType.equalsIgnoreCase("bs")) type = Script.MATRIX_INNER_EXP;
			else if (strType.equalsIgnoreCase("inner")) type = Script.MATRIX_INNER_EXP;
			else if (strType.equalsIgnoreCase("outer")) type = Script.MATRIX_OUTER_EXP;
			Script script = new Script(formatSQL(e.getText()),
					new Boolean(e.attributeValue("template", StringX.FALSE)).booleanValue(), type,
					e.attributeValue("target", itemId).replace('.', '_'),
					new Boolean(e.attributeValue("function", StringX.TRUE)).booleanValue());
			if (pre)
			{
				if (item.preScripts == null) item.preScripts = new ArrayList();
				item.preScripts.add(script);
			}
			else if (main) item.main = script;
			else
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(script);
			}
		}

		return item;
	}

	/**
	 * ����ÿһ����sqlItem
	 * 
	 * @param ele
	 * @param sqlType
	 * @return
	 */
	static SQLItem parseItem(SQLItem item, String itemId, Element ele, short sqlType)
			throws Exception
	{
		item.type = sqlType;

		// ��ȡ�Ⱦ�SQL
		if (ele.attributeValue("import") != null) item.dependence = StringX
				.split(ele.attributeValue("import").replace('.', '_').toLowerCase(), ",");

		// ��ȡjt
		String ds = ele.attributeValue("ds");
		if (StringX.nullity(ds)) ds = ele.attributeValue("jt");
		if (!StringX.nullity(ds)) item.jt = ds;

		String slave = ele.attributeValue("slave");
		if (!StringX.nullity(slave)) item.slave = slave;

		String injection = ele.attributeValue("injection");
		if (!StringX.nullity(injection)) item.injection = StringX.split(injection, ",");

		// ��ȡǰ��,���ú����б�
		// String fns = ele.attributeValue("preFnodes");
		// if (!StringX.nullity(fns)) item.preFnodes = fns.split(",");
		// fns = ele.attributeValue("postFnodes");
		// if (!StringX.nullity(fns)) item.postFnodes = fns.split(",");

		// ��ȡ�����ֵ�����dict
		String dict = ele.attributeValue("dict");
		if (!StringX.nullity(dict)) item.dict = StringX.str2map(dict, '#');

		item.prepared = new Boolean(StringX.null2emptystr(ele.attributeValue("prepare"), "false"))
				.booleanValue(); // 937_20170214

		String auth = ele.attributeValue("auth");
		if ("public".equalsIgnoreCase(auth)) item.auth = SQLItem.AUTH_public;
		else if ("login".equalsIgnoreCase(auth)) item.auth = SQLItem.AUTH_login;

		// ��ȡ����������,���ò���messageformat
		String rowIndex = ele.attributeValue("rowIndex");
		if (!StringX.nullity(rowIndex)) item.rowIndex = rowIndex;

		// 900 ����sql�� ȥ��where 1=1
		item.pretty = new Boolean(StringX.null2emptystr(ele.attributeValue("pretty"), "false"))
				.booleanValue();
		boolean paging = false; // �Ƿ�select ��ҳ
		String start = "start", limit = "limit", end = null; // ��ҳ����
		if (sqlType == SQLItem.SELECT)
		{ // select
			String clazz = ele.attributeValue("resultClass");
			if (clazz == null || clazz.length() == 0)
				clazz = ele.attributeValue("class", SQLItem.DEFAULT_RESULT_CLASS);
			item.resultClass = clazz;

			// �Ƿ���ֶ�����Сд����
			String columns = ele.attributeValue("column");
			if (!StringX.nullity(columns))
			{
				item.column = new HashMap();
				String[] cols = columns.split(",");
				for (int i = 0; i < cols.length; i++)
					item.column.put(cols[i].toLowerCase(), cols[i]);
			}

			// һ��SQL�������洦��, �����Ҫ������Service���Լ�ʵ��.
			// item.cacheBeanName = ele.attributeValue("cacheBeanName", null);
			paging = "true".equals(StringX.null2emptystr(ele.attributeValue("paging"), "false"));
			start = StringX.null2emptystr(ele.attributeValue("start"), "start");
			limit = StringX.null2emptystr(ele.attributeValue("limit"), "limit");
			end = StringX.null2emptystr(ele.attributeValue("end"), "");
			String first = ele.attributeValue("firstRowOnly");
			if (StringX.nullity(first)) first = ele.attributeValue("first", StringX.FALSE);
			item.firstRowOnly = new Boolean(first).booleanValue();
		}
		else
		{ // ִ�ж����޸���䣬 ��Ҫָ���ָ��� delim, �޸����ݵ���䣬����Ӱ�쵽������Ϣ����Ҫ�Ǽ�
			// String flushCaches = ele.attributeValue("flushCaches");
			// if (flushCaches != null)
			// {
			// String[] flushCacheArray = StringUtils.split(flushCaches, ",");
			// item.flushClazzArray = new Class[flushCacheArray.length];
			// for (int i = 0; i < flushCacheArray.length; i++)
			// {
			// item.flushClazzArray[i] = Class.forName(flushCacheArray[i]);
			// }
			// }
			item.delim = ele.attributeValue("delim");
		}
		List sql = ele.elements("sql"); // sql ������������<sql>��ǩ��
		if (sql == null || sql.size() == 0) item.sql = formatSQL(ele.getText());
		else item.sql = formatSQL(((Element) sql.get(0)).getText());
		if (paging)
		{ // 910, ����Ƿ�ҳ���Զ����
			item.sql = paging(item.sql, start, limit, end);
		}
		Configuration cfg = new Configuration();
		cfg.setNumberFormat(FTLUtil.numberFormat);
		item.t = new Template(itemId, new StringReader(item.sql), cfg);
		// ����postFn, preFn
		List script = ele.elements("script");
		if (script != null && script.size() > 0)
		{
			for (int i = 0; i < script.size(); i++)
			{
				Element e = (Element) script.get(i);
				if (new Boolean(e.attributeValue("pre", StringX.FALSE)).booleanValue())
				{
					item.preScript = new Script(formatSQL(e.getText()),
							new Boolean(e.attributeValue("template", StringX.FALSE)).booleanValue(),
							0,
							new Boolean(e.attributeValue("function", StringX.TRUE)).booleanValue());
				}
				else
				{
					if (item.postScripts == null) item.postScripts = new ArrayList();
					String strType = e.attributeValue("type", "bs");
					int type = 0;
					if (strType.equalsIgnoreCase("bs")) type = Script.BEENSHELL;
					else if (strType.equalsIgnoreCase("inner")) type = Script.MATRIX_INNER_EXP;
					else if (strType.equalsIgnoreCase("outer")) type = Script.MATRIX_OUTER_EXP;
					item.postScripts.add(new Script(formatSQL(e.getText()),
							new Boolean(e.attributeValue("template", StringX.FALSE)).booleanValue(),
							type, new Boolean(e.attributeValue("function", StringX.TRUE))
									.booleanValue()));
				}
			}
		}

		// ��ȡ�ֶ�ӳ��ת������
		Map clmconverters = new HashMap();
		List converters = ele.elements("converter");
		for (int i = 0; converters != null && i < converters.size(); i++)
		{
			Element e = (Element) converters.get(i);
			String clm = e.attributeValue("column", null);
			String con = e.attributeValue("converter", null);
			if (clm != null && con != null) clmconverters.put(clm.toLowerCase(), con);
		}
		if (clmconverters.size() > 0) item.columnConverters = clmconverters;

		return item;
	}

	// �������ݿ��ҳ
	public static String paging(String sql, String start, String limit, String end)
	{
		return "<#if _DB_TYPE_=\"MYSQL\"><#else>select * from(select <#if _DB_TYPE_=\"ORACLE\">rownum<#else>ROW_NUMBER() over()</#if> as rn, x.* from(</#if>"
				+ sql + "<#if _DB_TYPE_=\"MYSQL\"> limit ${" + start + "!(\"0\")},"
				+ (StringX.nullity(end) ? "${" + limit + "!(\"25\")}"
						: "${" + end + "}-${" + start + "!(\"0\")}")
				+ "<#else>)x)y where y.rn>${" + start
				+ "!(\"0\")} and y.rn<=" + (StringX.nullity(end)
						? "${" + start + "!(\"0\")}+${" + limit + "!(\"25\")}" : "${" + end + "}")
				+ "</#if>";
	}

	/**
	 * �Ѵ�xml�����ļ���õ�SQL���, ��ʽ��Ϊһ����׼��SQL���, ��ȥ�س�, ע�͵��ַ�
	 * 
	 * @param sql
	 * @return
	 */
	public static String formatSQL(String sql) throws Exception
	{
		BufferedReader br = new BufferedReader(new StringReader(sql));
		StringBuffer strBufSQL = new StringBuffer();
		while (true)
		{
			String line = br.readLine();
			if (line == null) break;
			if (line.length() == 0) continue;
			// System.out.println("line=" + line);
			int index = line.indexOf("//"); // ȥ��SQL�����ע��
			if (index >= 0) line = line.substring(0, index);
			index = line.indexOf("--");
			if (index >= 0) line = line.substring(0, index);
			line = StringX.trim(line.replace('\t', ' '));
			if (line.length() == 0) continue;
			strBufSQL.append(line);
			// ���SQL��һ�н�βû�ж���, ��ô��һ���ո�
			if (!line.endsWith(StringX.COMMA)) strBufSQL.append(' ');
		}
		return StringX.trim(strBufSQL.toString());
	}
}
