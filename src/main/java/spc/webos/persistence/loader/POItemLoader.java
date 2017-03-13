package spc.webos.persistence.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.sf.cglib.beans.BulkBean;
import spc.webos.persistence.BulkBeanItem;
import spc.webos.persistence.IPersistence;
import spc.webos.persistence.SQLItem;
import spc.webos.persistence.Script;
import spc.webos.util.FTLUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

/**
 * ����ģʽSqlItem���������
 * 
 * @author Hate
 * 
 */
public class POItemLoader
{
	/**
	 * ��ȡһ��Ŀ¼�µ������ļ�
	 * 
	 * @param voSQLMap
	 *            ֱ�Ӷ���ʽ���ʵ�SQL�������
	 * @param voMapping
	 *            ÿ��vo�����й�������vo���Ե�������Ϣ
	 * @throws Exception
	 */
	public static void readMappingDir(Resource path, Map voSQLMap, Map voMapping) throws Exception
	{
		File file = path.getFile();
		if (!file.exists()) log.warn("cannot load mapping file dir: " + file.getAbsolutePath());
		File[] files = file.listFiles();
		for (int i = 0; files != null && i < files.length; i++)
		{
			try
			{
				if (files[i].getName().endsWith("xml"))
				{
					readClassXML(new FileInputStream(files[i]), voSQLMap, voMapping);
					log.info("loaded po file:{}", files[i].getName());
				}
			}
			catch (Exception e)
			{
				System.err.println("load file failure:" + files[i].getName());
				throw e;
			}
		}
	}

	/**
	 * ��ȡһ�������ļ������е�Class˵��
	 * 
	 * @param location
	 * @param voSQLMap
	 * @throws Exception
	 */
	public static void readClassXML(InputStream location, Map<Class<?>, SQLItem[]> voSQLMap,
			Map<Class<?>, BulkBeanItem> voMapping) throws Exception
	{
		SAXReader reader = new SAXReader(false);
		Document doc = reader.read(location);
		Element root = doc.getRootElement();
		String slave = root.attributeValue("slave");
		String ds = root.attributeValue("ds");
		if (StringX.nullity(ds)) ds = root.attributeValue("jt");
		List classes = root.elements("class");
		for (int i = 0; i < classes.size(); i++)
		{
			Element classElement = (Element) classes.get(i);
			ClassDesc classDesc = new ClassDesc();

			classDesc.ds = ds;
			classDesc.slave = slave;
			
			readProperty(classDesc, classElement); // ��ȡһ��VO�������
			try
			{ // modified by chenjs 2011-12-01 ����಻��������־���沢����������Ϣ
				Class clazz = Class.forName(classDesc.getName(), false,
						Thread.currentThread().getContextClassLoader());

				// process... ����SQLģ��
				voSQLMap.put(clazz, process(classDesc));

				// ��ȡVo��������Vo������������
				classDesc.setVoProperties(new ArrayList());
				readResultProperty(classDesc, classElement, "many-to-one"); // ��ȡmany-to-oneģʽ������VO����
				readResultProperty(classDesc, classElement, "one-to-many"); // ��ȡone-to-manyģʽ������VO����
				readResultProperty(classDesc, classElement, "result"); // ��ȡresult����
				if (classDesc.getVoProperties().size() > 0)
					voMapping.put(clazz, convert(classDesc));
			}
			catch (NoClassDefFoundError | ClassNotFoundException cnfe)
			{
				log.warn("NoClassDefFoundError | ClassNotFoundException: {}, {}",
						classDesc.getName(), cnfe.toString());
				continue;
			}
		}
		location.close();
	}

	/**
	 * ��һ��VO�й������Ե�������Ϣ��List�е�ClassVOPropertyDesc��ʽ�� ���List�е�BulkBeanItem��ʽ��
	 * ���Persistence��ʹ�õ�����
	 * 
	 * @param classDesc
	 * @return
	 * @throws Exception
	 */
	static BulkBeanItem convert(ClassDesc classDesc) throws Exception
	{
		BulkBeanItem bulkBeanItem = new BulkBeanItem();
		List voProperties = classDesc.getVoProperties();
		// 1. ����VO�����й�����VO����List��������Ϣ
		Class clazz = Class.forName(classDesc.name, false,
				Thread.currentThread().getContextClassLoader());
		Class[] clazzArray = new Class[voProperties.size()];
		String[] setter = new String[voProperties.size()];
		String[] getter = new String[voProperties.size()];
		for (int i = 0; i < voProperties.size(); i++)
		{
			ResultPropertyDesc propertyDesc = (ResultPropertyDesc) voProperties.get(i);
			clazzArray[i] = clazz.getDeclaredField(propertyDesc.getName()).getType();
			String name = StringUtils.capitalize(propertyDesc.getName());
			setter[i] = "set" + name;
			getter[i] = "get" + name;
		}
		bulkBeanItem.setVoProperties(BulkBean.create(clazz, getter, setter, clazzArray));

		// ÿһ������VO���� ������ Sql��ѯ����
		List prop = new ArrayList(voProperties.size());
		for (int i = 0; i < voProperties.size(); i++)
		{
			Object[] bulkBeans = new Object[5]; // 900_20160115 ��������������5��λ��
			prop.add(bulkBeans);
			ResultPropertyDesc propertyDesc = (ResultPropertyDesc) voProperties.get(i);
			bulkBeans[4] = propertyDesc.getName(); // 900_20160115 ��������������5��λ��
			bulkBeans[2] = clazz.getDeclaredField(propertyDesc.getName()).getType();
			if (propertyDesc.getSelect() != null)
			{ // �����Բ��ǹ�������VO���ԣ� ����һ����ͨ��sql����
				bulkBeans[0] = propertyDesc.getSelect();
				continue;
			}
			// ֻ��Թ�������������VOʱ������
			bulkBeans[3] = Class.forName(propertyDesc.getJavaType(), false,
					Thread.currentThread().getContextClassLoader());

			// ��������VO���Ժ͹�����VO����֮��������ϵ
			// ��������
			clazzArray = new Class[propertyDesc.getter.length];
			setter = new String[propertyDesc.getter.length];
			getter = new String[propertyDesc.getter.length];
			for (int j = 0; j < propertyDesc.getter.length; j++)
			{
				clazzArray[j] = clazz.getDeclaredField(propertyDesc.getter[j]).getType();
				String name = StringUtils.capitalize(propertyDesc.getter[j]);
				setter[j] = "set" + name;
				getter[j] = "get" + name;
			}
			bulkBeans[0] = BulkBean.create(clazz, getter, setter, clazzArray);

			// ����VO������
			setter = new String[propertyDesc.getter.length];
			getter = new String[propertyDesc.getter.length];
			for (int j = 0; j < propertyDesc.getter.length; j++)
			{
				String name = StringUtils.capitalize(propertyDesc.setter[j]);
				setter[j] = "set" + name;
				getter[j] = "get" + name;
			}
			bulkBeans[1] = BulkBean.create(
					Class.forName(propertyDesc.getJavaType(), false,
							Thread.currentThread().getContextClassLoader()),
					getter, setter, clazzArray);
		}
		bulkBeanItem.setProperties(prop);
		return bulkBeanItem;
	}

	/**
	 * ��ȡVo�й�������VO���Ե�������Ϣ�� Ҳ������ȡ����Sql������Ϊ���������
	 * 
	 */
	static void readResultProperty(ClassDesc classDesc, Element classElement, String type)
			throws Exception
	{
		List property = classElement.elements(type);
		for (int i = 0; i < property.size(); i++)
		{
			Element ele = (Element) property.get(i);
			ResultPropertyDesc propertyDesc = new ResultPropertyDesc();
			propertyDesc.setName(ele.attributeValue("name")); // 900_20160105
																// ��ԭ������propertyͳһ��Ϊname
			propertyDesc.setJavaType(ele.attributeValue("javaType", "String"));
			propertyDesc.setSelect(ele.attributeValue("select"));
			propertyDesc.setRemark(ele.attributeValue("remark"));
			propertyDesc.setManyToOne(true);
			if (propertyDesc.getSelect() == null)
			{
				propertyDesc.setManyToOne(type.equals("many-to-one"));
				readVOPropertyRelation(propertyDesc, ele);
			}
			classDesc.getVoProperties().add(propertyDesc);
		}
	}

	/**
	 * ��ȡVO������Թ�ϵ, �൱�ڱ��������ϵ�� ���˱�(VO)�е���Щ�ֶ���ϵ�Ź�����(VO)�е���Щ�ֶε���Ϣ
	 * 
	 */
	static void readVOPropertyRelation(ResultPropertyDesc propertyDesc, Element propertyElement)
			throws Exception
	{
		List properties = propertyElement.elements("map");
		if (properties != null && properties.size() > 0)
		{
			propertyDesc.setter = new String[properties.size()];
			propertyDesc.getter = new String[properties.size()];
			for (int i = 0; i < properties.size(); i++)
			{ // 900_20160105 ��ԭ����getter/setter�����޸�Ϊ from/to
				Element ele = (Element) properties.get(i);
				propertyDesc.getter[i] = ele.attributeValue("from");
				propertyDesc.setter[i] = ele.attributeValue("to", propertyDesc.getter[i]); // Ĭ����VO����������ͬ
			}
		}
		else if (!StringX.nullity(propertyElement.attributeValue("map")))
		{
			String[] map = StringX.split(propertyElement.attributeValue("map"), ",");
			propertyDesc.getter = new String[map.length];
			propertyDesc.setter = new String[map.length];
			for (int i = 0; i < map.length; i++)
			{
				int idx = map[i].indexOf(':');
				if (idx > 0)
				{
					propertyDesc.getter[i] = map[i].substring(0, idx).trim();
					propertyDesc.setter[i] = map[i].substring(idx + 1).trim();
				}
				else propertyDesc.getter[i] = propertyDesc.setter[i] = map[i].trim();
			}
		}
		else
		{ // ����ֱ��ʹ�����Խ�������
			propertyDesc.getter = StringX.split(propertyElement.attributeValue("from"), ",");
			String to = propertyElement.attributeValue("to");
			propertyDesc.setter = StringX.nullity(to) ? propertyDesc.getter
					: StringX.split(to, ",");
		}
	}

	/**
	 * ��ȡһ���������������Ϣ, ��������к������ݿ�manual sequence�ֶΣ�ֻ����һ��sequence�ֶΣ� ����һ��String[]
	 * {0:��ı����� 1���ֶ���}�Ķ��� CacheManager cacheManager,
	 * 
	 * @param clazzDesc
	 * @param classElement
	 * @throws Exception
	 */
	public static void readProperty(ClassDesc clazzDesc, Element classElement) throws Exception
	{
		List properties = new ArrayList();
		clazzDesc.setProperties(properties);
		clazzDesc.setName(classElement.attributeValue("name"));
		clazzDesc.setTable(classElement.attributeValue("table"));

		String ds = classElement.attributeValue("ds");
		if (StringX.nullity(ds)) ds = classElement.attributeValue("jt");
		if (!StringX.nullity(ds)) clazzDesc.ds = ds;
		
		String slave = classElement.attributeValue("slave");
		if (!StringX.nullity(slave)) clazzDesc.slave = slave;

		clazzDesc.setParent(classElement.attributeValue("parent"));
		clazzDesc.setRemark(classElement.attributeValue("remark"));
		String insertMode = classElement.attributeValue("insertMode");
		if (!StringX.nullity(insertMode)) clazzDesc.setInsertMode(Integer.parseInt(insertMode));

		// ��ȡstatic fields
		Element staticFields = classElement.element("static");
		if (staticFields != null)
		{
			List p = staticFields.elements("p");
			for (int i = 0; i < p.size(); i++)
				clazzDesc.getStaticFields().add(((Element) p.get(i)).getText().trim());
		}

		// ��ȡdeclare��Ϣ
		Element declare = classElement.element("declare");
		if (declare != null) clazzDesc.setDeclare(declare.getText());

		declare = classElement.element("insert-preFn");
		if (declare != null) clazzDesc.setInsertPreFn(declare.getText());
		declare = classElement.element("insert-postFn");
		if (declare != null) clazzDesc.setInsertPostFn(declare.getText());

		declare = classElement.element("update-preFn");
		if (declare != null) clazzDesc.setUpdatePreFn(declare.getText());
		declare = classElement.element("update-postFn");
		if (declare != null) clazzDesc.setUpdatePostFn(declare.getText());

		declare = classElement.element("select-preFn");
		if (declare != null) clazzDesc.setSelectPreFn(declare.getText());
		declare = classElement.element("select-postFn");
		if (declare != null) clazzDesc.setSelectPostFn(declare.getText());

		declare = classElement.element("delete-preFn");
		if (declare != null) clazzDesc.setDeletePreFn(declare.getText());
		declare = classElement.element("delete-postFn");
		if (declare != null) clazzDesc.setDeletePostFn(declare.getText());

		Map columnConverter = new HashMap();
		// ����ValueObject��������Ϣ
		List property = classElement.elements("property");
		for (int i = 0; i < property.size(); i++)
		{
			Element ele = (Element) property.get(i);
			ClassPropertyDesc cpd = new ClassPropertyDesc();

			String name = ele.attributeValue("name");
			String column = ele.attributeValue("column");
			if (StringX.nullity(name) && !StringX.nullity(column) && column.indexOf('_') > 0)
			{ // 900, ֧��column�м����»��ߣ�Ȼ���Զ�����java bean������
				String[] words = StringX.split(column, "_");
				name = words[0].toLowerCase();
				for (int j = 1; j < words.length; j++)
					name += words[j].substring(0, 1).toUpperCase()
							+ words[j].substring(1).toLowerCase();
				cpd.setName(name);
			}
			else
			{
				cpd.setName(name);
				cpd.setColumn(ele.attributeValue("column", cpd.getName()));
				if (StringX.nullity(cpd.getColumn())) cpd.setColumn(cpd.getName().toUpperCase());
			}

			cpd.version = new Boolean(ele.attributeValue("version", "false")).booleanValue(); // 810,
																								// �ֹ���
			cpd.setUpdatable(new Boolean(ele.attributeValue("updatable", "true")).booleanValue()); // 900
																									// ֧�ֲ����޸�
			cpd.setPrimary(new Boolean(ele.attributeValue("primary", "false")).booleanValue());
			cpd.setPrepare(new Boolean(ele.attributeValue("prepare", "false")).booleanValue());
			cpd.setUuid(new Boolean(ele.attributeValue("uuid", "false")).booleanValue());
			if (cpd.isUuid()) cpd.setUpdatable(false); // 900_201060107
														// UUID�ֶβ����޸�
			String jdbcType = ele.attributeValue("jdbcType");
			if (jdbcType != null) cpd.setJdbcType(jdbcType.toUpperCase());
			cpd.setJavaType(ele.attributeValue("javaType", "String"));
			if (cpd.getJavaType().equalsIgnoreCase("IBlob")) cpd.setPrepare(true);
			String converter = ele.attributeValue("converter", null);
			if (converter != null) columnConverter.put(cpd.getName(), converter);
			if (cpd.getJavaType().equalsIgnoreCase("CompositeNode")) // ���VO����ΪCompositeNode��ô�������XMLת��
			{
				columnConverter.put(cpd.getName(), "XML");
				cpd.setPrepare(true); // ����xml������ܰ���'���в���prepare��ʽ���
			}
			if (cpd.isPrepare()) clazzDesc.setPrepare(true);
			cpd.setDefaultValue(ele.attributeValue("default"));
			String sequence = StringX.null2emptystr(ele.attributeValue("sequence"));
			if (!StringX.nullity(sequence))
			{
				cpd.setJavaType("Long");
				cpd.setSequence(sequence.toUpperCase());
				cpd.setUpdatable(false); // 900_20160107 �����ֶ�Ĭ�ϲ������޸�
				// 900_20160725 ���������ֶζ����벻�����ݿ����
				clazzDesc.sequence = true;
			}
			cpd.setNullValue(ele.attributeValue("nullValue"));
			cpd.setRemark(ele.attributeValue("remark"));
			readExp(cpd, ele); // ��ȡinsert, update, select���ʽ
			properties.add(cpd);
		}
		if (columnConverter.size() > 0) clazzDesc.setColumnConverter(columnConverter);
	}

	public static void readPO(Class po, Map<Class<?>, SQLItem[]> voSQLMap,
			Map<Class<?>, BulkBeanItem> voMapping)
	{
		log.info("PO annotation:{}", po); // ���߳��¿��ܻᲢ����ӡ
		ClassDesc classDesc = new ClassDesc();
		classDesc.name = po.getName();
		try
		{
			if (!readPO(classDesc, po))
			{
				log.info("PO annotation fail:{}", po);
				return; // ��ȡһ��PO�������
			}

			// process... ����SQLģ��
			voSQLMap.put(po, process(classDesc));

			// �Ƿ��й�������
			if (classDesc.getVoProperties().size() > 0) voMapping.put(po, convert(classDesc));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean readPO(ClassDesc clazzDesc, Class po) throws Exception
	{
		if (po.getAnnotation(Entity.class) == null) return false;
		Table table = (Table) po.getAnnotation(Table.class);
		if (table == null) return false;
		clazzDesc.table = table.name();
		List properties = new ArrayList();
		clazzDesc.setProperties(properties);
		clazzDesc.setVoProperties(new ArrayList());
		Field[] fields = po.getDeclaredFields();
		for (Field f : fields)
		{
			if (f.getAnnotation(Column.class) == null && f.getAnnotation(JoinColumn.class) == null)
				continue;
			ClassPropertyDesc cpd = new ClassPropertyDesc();
			cpd.name = f.getName();
			Annotation[] anns = f.getAnnotations();
			for (Annotation ann : anns)
			{
				if (ann instanceof Id) cpd.primary = true;
				else if (ann instanceof Version) cpd.version = true;
				else if (ann instanceof Column)
				{
					Column col = (Column) ann;
					cpd.column = col.name();
					if (StringX.nullity(cpd.column)) cpd.column = cpd.name;
					cpd.javaType = f.getType().getSimpleName();
					Map<String, Object> attr = new HashMap<>();
					if (!StringX.nullity(col.columnDefinition()))
						attr = (Map<String, Object>) JsonUtil.gson2obj(col.columnDefinition());
					cpd.prepare = attr.containsKey("prepare") && (Boolean) attr.get("prepare");
					if (cpd.prepare) clazzDesc.prepare = true;
					properties.add(cpd);
				}
				else if (ann instanceof JoinColumn)
				{ // ��������
					JoinColumn jcol = (JoinColumn) ann;
					ResultPropertyDesc propertyDesc = new ResultPropertyDesc();
					propertyDesc.setName(cpd.name);

					propertyDesc.javaType = f.getGenericType().getTypeName();
					int idx = propertyDesc.javaType.indexOf('<');
					if (idx > 0) propertyDesc.javaType = propertyDesc.javaType.substring(idx + 1,
							propertyDesc.javaType.length() - 1);
					// System.out.println("JoinColumn:" + cpd.name + ",
					// "
					// + f.getGenericType().getTypeName() + ", " +
					// propertyDesc.javaType);

					// �������õ�ManyToOne������Ϊ����OneToMany
					propertyDesc.setManyToOne(f.getAnnotation(ManyToOne.class) != null);

					// JPAֻ��֧��һ���ֶεĹ�������
					propertyDesc.getter = new String[] { jcol.name() };
					propertyDesc.setter = new String[] {
							StringX.nullity(jcol.referencedColumnName()) ? jcol.name()
									: jcol.referencedColumnName() };

					clazzDesc.getVoProperties().add(propertyDesc);
				}
			}
		}
		return true;
	}

	/**
	 * ��ȡselect,update,insert���ʽ
	 * 
	 * @param classPropertyDesc
	 * @param propertyElement
	 * @throws Exception
	 */
	public static void readExp(ClassPropertyDesc classPropertyDesc, Element propertyElement)
			throws Exception
	{
		List exp = propertyElement.elements("select");
		if (exp != null && exp.size() == 1)
		{
			String selectExp = ((Element) exp.get(0)).getText();
			classPropertyDesc.setSelect(SQLItemXmlLoader.formatSQL(selectExp).trim());
		}
		exp = propertyElement.elements("update");
		if (exp != null && exp.size() == 1)
		{
			String updateExp = ((Element) exp.get(0)).getText();
			classPropertyDesc.setUpdate(SQLItemXmlLoader.formatSQL(updateExp).trim());
		}
		exp = propertyElement.elements("insert");
		if (exp != null && exp.size() == 1)
		{
			String insertExp = ((Element) exp.get(0)).getText();
			classPropertyDesc.setInsert(SQLItemXmlLoader.formatSQL(insertExp).trim());
		}
	}

	/**
	 * ����SQLģ��
	 * 
	 * @param classDesc
	 *            Java������
	 * @param properties
	 *            Java����������
	 * @param voSQLMap
	 * @throws Exception
	 */
	public static SQLItem[] process(ClassDesc classDesc) throws Exception
	{
		SQLItem[] items = new SQLItem[4];
		StringWriter out = new StringWriter();
		try (InputStreamReader isr = new InputStreamReader(
				new POItemLoader().getClass().getResourceAsStream("sqlitem.ftl")))
		{
			Template t = new Template("SQL_ITEM", isr, null);
			Map root = new HashMap();
			// 900, Ĭ��ʹ��oracle
			root.put(IPersistence.DB_TYPE_KEY, "ORACLE"); // persistence.getDefautlJdbcTemplate().getDbType());
			root.put("classDesc", classDesc);
			root.put("properties", classDesc.getProperties());

			items[SQLItem.SELECT] = genSQLItem(root, t, out, classDesc, SQLItem.SELECT);
			items[SQLItem.DELETE] = genSQLItem(root, t, out, classDesc, SQLItem.DELETE);
			items[SQLItem.INSERT] = genSQLItem(root, t, out, classDesc, SQLItem.INSERT);
			items[SQLItem.UPDATE] = genSQLItem(root, t, out, classDesc, SQLItem.UPDATE);
			// System.out.println("VOSQL:update:"+items[SQLItem.UPDATE].sql);
			return items;
		}
	}

	static SQLItem genSQLItem(Map root, Template temp, StringWriter out, ClassDesc classDesc,
			short type) throws Exception
	{
		root.put("sqlType", String.valueOf(type));
		out.getBuffer().setLength(0);
		temp.process(root, out);
		out.flush();
		SQLItem item = new SQLItem();
		item.columnConverters = classDesc.getColumnConverter();
		item.resultClass = classDesc.getName();
		item.jt = classDesc.getDataSource();
		item.slave = classDesc.slave;
		if (item.type == SQLItem.INSERT) item.sequnce = classDesc.sequence; // 900,
																			// ����ʱ�����ҪΨһ��
		item.sql = SQLItemXmlLoader.formatSQL(out.toString());
		Configuration cfg = new Configuration();
		cfg.setNumberFormat(FTLUtil.numberFormat);
		item.t = new Template(classDesc.getName() + ':' + type, new StringReader(item.sql), cfg);
		item.type = type;
		if (type != SQLItem.INSERT) item.pretty = true; // 900, ȥ��where 1=1
		item.prepared = classDesc.isPrepare(); // 937_20170214
		// if (classDesc.isPrepare() && type != SQLItem.SELECT) item.prepared =
		// true;
		// else item.type = type; //
		if (type == SQLItem.SELECT)
		{
			if (classDesc.getSelectPreFn() != null
					&& classDesc.getSelectPreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getSelectPreFn(), false, 0, true);
			if (classDesc.getSelectPostFn() != null
					&& classDesc.getSelectPostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getSelectPostFn(), false, 0, true));
			}
		}
		else if (type == SQLItem.UPDATE)
		{
			if (classDesc.getUpdatePreFn() != null
					&& classDesc.getUpdatePreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getUpdatePreFn(), false, 0, true);
			if (classDesc.getUpdatePostFn() != null
					&& classDesc.getUpdatePostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getUpdatePostFn(), false, 0, true));
			}
		}
		else if (type == SQLItem.INSERT)
		{
			if (classDesc.getInsertPreFn() != null
					&& classDesc.getInsertPreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getInsertPreFn(), false, 0, true);
			if (classDesc.getInsertPostFn() != null
					&& classDesc.getInsertPostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getInsertPostFn(), false, 0, true));
			}
		}
		else if (type == SQLItem.DELETE)
		{
			if (classDesc.getDeletePreFn() != null
					&& classDesc.getDeletePreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getDeletePreFn(), false, 0, true);
			if (classDesc.getDeletePostFn() != null
					&& classDesc.getDeletePostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getDeletePostFn(), false, 0, true));
			}
		}
		return item;
	}

	static Logger log = LoggerFactory.getLogger(POItemLoader.class);
	// public static final int OPERATOR_SELECT = 0;
	// public static final int OPERATOR_INSERT = 1;
	// public static final int OPERATOR_UPDATE = 2;
	// public static final int OPERATOR_DELETE = 3;
}
