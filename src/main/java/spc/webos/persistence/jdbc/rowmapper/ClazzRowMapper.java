package spc.webos.persistence.jdbc.rowmapper;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.jdbc.support.JdbcUtils;

import spc.webos.persistence.PO;
import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.util.FileUtil;
import spc.webos.util.POJOUtil;

/**
 * �ض�����д�����
 * 
 * @author spc
 * 
 */
public class ClazzRowMapper extends AbstractRowMapper
{
	String[] columnName;

	public String[] getColumnName()
	{
		return columnName;
	}

	public ClazzRowMapper(Class rowClazz)
	{
		this.rowClazz = rowClazz;
		wrapper.registerCustomEditor(Date.class, longDateEditor);
	}

	public ClazzRowMapper(Class rowClazz, String charsetDB)
	{
		this.rowClazz = rowClazz;
		this.charsetDB = charsetDB;
		wrapper.registerCustomEditor(Date.class, longDateEditor);
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		long s = 0;
		if (log.isDebugEnabled()) s = System.currentTimeMillis();
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Object rowObj = null;
		try
		{
			rowObj = rowClazz.newInstance();
			if (rowObj instanceof PO) ((PO) rowObj).beforeLoad();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		wrapper.setWrappedInstance(rowObj);

		for (int i = 1; i <= columnCount; i++)
		{
			String clmName = rsmd.getColumnName(i);
			String fieldName = POJOUtil.getProperty(rowClazz, clmName);
			if (fieldName == null)
			{
				log.debug("NO clm:{} in {}", clmName, rowClazz.getName());
				continue;
			}
			try
			{
				Class clazz = wrapper.getPropertyType(fieldName);
				if (clazz == ByteArrayBlob.class)
				{
					try
					{
						InputStream is = rs.getBinaryStream(clmName);
						if (is == null) continue;
						wrapper.setPropertyValue(fieldName,
								new ByteArrayBlob(FileUtil.is2bytes(is)));
						// if (clazz == ByteArrayBlob.class)
						// wrapper.setPropertyValue(fieldName,
						// new ByteArrayBlob(FileUtil.is2bytes(is)));
						// else
						// {
						// File file = JdbcUtil.getTempFile((PO) rowObj,
						// JdbcUtil.genFileNameByVO(rowObj, fieldName), is);
						// wrapper.setPropertyValue(fieldName, new
						// spc.webos.persistence.jdbc.blob.FileBlob(
						// file));
						// if (log.isDebugEnabled()) log.debug("file path: "
						// + file.getAbsolutePath());
						// }
					}
					catch (Exception e)
					{
						log.warn("getBinaryStream for clm:" + clmName, e);
					}
					continue;
				}
			}
			catch (Exception e)
			{
				log.warn("field:" + fieldName + ", class:" + wrapper.getPropertyType(fieldName), e);
			}

			Object obj = JdbcUtils.getResultSetValue(rs, i);
			if (obj == null) continue; // ����ֶ�Ϊ��, ֱ��������һ�ֶ�
			if (isConverter(fieldName))
			{ // �ֶ���Ҫת��
				wrapper.setPropertyValue(fieldName, converter(fieldName, obj));
				continue;
			}
			String value = obj.toString().trim();
			try
			{
				if (charsetDB != null) value = new String(value.getBytes(charsetDB));
			}
			catch (Exception e)
			{
				throw new RuntimeException(
						"Error in coverting to Charset(" + charsetDB + "), value=" + obj, e);
			}

			if (Date.class == wrapper.getPropertyType(fieldName))
			{ // �����������ݸ�ʽ����Ϊ1981-01-26 �м���-�ַ�
				if (value.length() < 12) value += " 00:00:00";
			}
			wrapper.setPropertyValue(fieldName, value);
		}
		if (rowObj instanceof PO) ((PO) rowObj).afterLoad();
		if (log.isDebugEnabled())
		{
			long end = System.currentTimeMillis();
			String msg = "ClazzRowMapper cost:" + (end - s) + ", rowObj:" + rowObj.getClass();
			if (end - s > 10) log.debug(msg);
		}
		return rowObj;
	}

	/**
	 * ���ǵ����ݿ������MetaData��Ϣ���������ݿ�ԭ��, �����õ��ֶ�����Сд������, ���Բ��ô˷�����������Ĵ�Сд���е�������
	 * 
	 * @param fieldName
	 * @return
	 */
	// String getRealFieldName(String fieldName)
	// {
	// Map field = getClazzField(rowClazz);
	// String attrField = (String) field.get(fieldName);
	// return attrField == null ? fieldName : attrField;
	// }
	String charsetDB;
	BeanWrapperImpl wrapper = new BeanWrapperImpl(true);
	Class rowClazz;
	CustomDateEditor longDateEditor = new CustomDateEditor(
			new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"), true);

	// CustomDateEditor shortDateEditor = new CustomDateEditor(new
	// SimpleDateFormat("yyyy-MM-dd"),
	// true);
	// static Map clazzField = new HashMap(); // ���ڴ洢class���������

	/**
	 * ������������ֶ�����
	 * 
	 * @param clazz
	 * @return
	 */
	// static synchronized Map getClazzField(Class clazz)
	// {
	// Map field = (Map) clazzField.get(clazz);
	// if (field != null) return field;
	// field = new RowMap();
	// while (!clazz.equals(Object.class))
	// {
	// Field[] fields = clazz.getDeclaredFields();
	// for (int i = 0; fields != null && i < fields.length; i++)
	// {
	// String fieldName = fields[i].getName();
	// field.put(fieldName, fieldName);
	// }
	// clazz = clazz.getSuperclass();
	// }
	//
	// clazzField.put(clazz, field);
	// return field;
	// }
}
