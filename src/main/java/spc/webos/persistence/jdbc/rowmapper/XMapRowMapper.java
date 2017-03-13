package spc.webos.persistence.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.support.JdbcUtils;

import spc.webos.persistence.SQLItem;
import spc.webos.persistence.jdbc.rowtype.RowXMap;
import spc.webos.util.StringX;
import spc.webos.util.SpringUtil;

/**
 * ��һ����������List���������ݿ��м�¼, ʹ֮����Map��ʽ����
 * 
 * @author spc
 */
public class XMapRowMapper extends AbstractRowMapper
{
	ResultSetMetaData rsmd;
	String[] columnName;

	public ResultSetMetaData getResultSetMetaData()
	{
		return rsmd;
	}

	public XMapRowMapper(String charsetDB)
	{
		this.charsetDB = charsetDB;
	}

	public String[] getColumnName()
	{
		return columnName;
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		rsmd = rs.getMetaData();
		if (columnName == null)
		{
			columnName = new String[rsmd.getColumnCount()];
			for (int i = 0; i < columnName.length; i++)
				columnName[i] = rsmd.getColumnName(i + 1).toLowerCase();
		}
		int columnCount = rsmd.getColumnCount();
		String[] fieldNames = getFieldNames(rsmd);
		SQLItem item = SQLItem.getCurrentItem();
		Map mapOfColValues = new RowXMap(fieldNames);
		for (int i = 1; i <= columnCount; i++)
		{
			String key = rsmd.getColumnName(i).toLowerCase();
			Object obj = JdbcUtils.getResultSetValue(rs, i);
			if (obj == null)
			{
				mapOfColValues.put(key, StringX.EMPTY_STRING);
				continue;
			}
			if (isConverter(key))
			{ // �ֶ���Ҫת�� modify by spc 090601
				mapOfColValues.put(key, converter(key, obj));
				continue;
			}
			// update by sturdypine.chen 2007-04-18 ʹ�����ݿ�������ԭ���ͣ�����Ϊstring����
			// String value = value.toString().trim();
			try
			{
				if ((obj instanceof String)) obj = obj.toString().trim();
				if ((obj instanceof String) && charsetDB != null) obj = new String(obj.toString()
						.getBytes(charsetDB));
			}
			catch (Exception e)
			{
				throw new RuntimeException("Error in coverting to Charset(" + charsetDB + "), value="
						+ obj, e);
			}
			if (item != null && SpringUtil.APPCXT != null && item.dict != null && obj instanceof String)
			{ // ���ڴ������ֵ�ת��
				String v = null;
				String d = (String) item.dict.get(key);
				if (d != null) v = SpringUtil.getMessage(d, (String) obj, null, null);
				mapOfColValues.put(key, v != null ? v : obj);
			}
			else mapOfColValues.put(key, obj);
		}
		return mapOfColValues;
	}

	synchronized String[] getFieldNames(ResultSetMetaData rsmd) throws SQLException
	{
		String[] fieldNames = new String[rsmd.getColumnCount()];
		for (int i = 1; i <= fieldNames.length; i++)
		{
			fieldNames[i - 1] = rsmd.getColumnName(i);
		}
		// String[] column = (String[])
		// mapColumnName.get(stringArrayToString(fieldNames));
		fieldNames = RowXMap.getFormatFieldNames(fieldNames);
		// mapFieldName.put(sqlId, fieldNames);

		return fieldNames;
	}

	String charsetDB;
}
