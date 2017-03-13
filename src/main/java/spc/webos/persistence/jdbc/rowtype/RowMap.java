package spc.webos.persistence.jdbc.rowtype;

import java.util.HashMap;

import spc.webos.util.JsonUtil;

/**
 * ��Ҫ���ų��ؼ��ֵĴ�Сд, �ڴ�Map�е�key�Ǵ�Сд�����е�...
 * 
 * @author Hate
 */
public class RowMap extends HashMap
{
	private static final long serialVersionUID = 1L;
	String[] columnName;

	public String[] getColumnName()
	{
		return columnName;
	}

	public RowMap()
	{
		super();
	}

	public RowMap(int count, String[] columnName)
	{
		super(count);
		this.columnName = columnName;
	}

	public StringBuffer toJson()
	{
		StringBuffer row = new StringBuffer();
		row.append(JsonUtil.obj2json(this));
		// row.append('{');
		// Iterator keys = super.keySet().iterator();
		// while (keys.hasNext())
		// {
		// String key = (String) keys.next();
		// Object v = get(key);
		// if (v == null) continue;
		// if (row.length() > 2) row.append(',');
		// row.append(key);
		// row.append(":'");
		// String str = v.toString();
		// if (str.indexOf('\'') >= 0) str = str.replace("'", "\\'");
		// if (str.indexOf('\n') >= 0) str = str.replace("\n", "\\n");
		// row.append(str);
		// row.append('\'');
		// }
		// row.append('}');
		return row;
	}

	public Object put(Object key, Object value)
	{
		return super.put(key.toString().toLowerCase(), value);
	}

	public Object get(Object key)
	{
		return super.get(key.toString().toLowerCase());
	}
}
