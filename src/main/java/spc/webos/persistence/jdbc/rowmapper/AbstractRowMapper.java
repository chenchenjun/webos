package spc.webos.persistence.jdbc.rowmapper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import spc.webos.persistence.jdbc.IColumnConverter;

public abstract class AbstractRowMapper implements RowMapper
{
	public final static ThreadLocal COLUMN_CONVERTER = new ThreadLocal(); // ��ѯ�������ֶ��Ƿ���Ҫ����ת��
	public final static ThreadLocal COLUMN_CASE = new ThreadLocal(); // �ֶ��Ƿ��Сд���У���Ҫ���ض��Ĵ�Сд����Map����
	protected Logger log = LoggerFactory.getLogger(getClass());

	public abstract String[] getColumnName();

	protected boolean isConverter(String column)
	{
		Map converters = (Map) COLUMN_CONVERTER.get();
		if (converters == null) return false;
		String name = (String) converters.get(column.toLowerCase());
		if (name == null) return false;
		IColumnConverter converter = (IColumnConverter) IColumnConverter.COLUMN_CONVERTER.get(name);
		return converter != null;
	}

	protected Object converter(String column, Object value)
	{
		Map converters = (Map) COLUMN_CONVERTER.get();
		if (converters == null) return value;
		String name = (String) converters.get(column.toLowerCase());
		if (name == null) return value;
		IColumnConverter converter = (IColumnConverter) IColumnConverter.COLUMN_CONVERTER.get(name);
		if (converter == null)
		{
			log.warn("can not find converter by name: " + name);
			return value;
		}
		return converter.convert(column, value);
	}
}
