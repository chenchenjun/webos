package spc.webos.persistence.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * �־ò�������ȡ�����Ľ���ٽ����û�ָ������ת��������ʵ��ֵ����ת��
 * 
 * @author spc
 * 
 */
public interface IColumnConverter
{
	// ������ ��ֵ
	Object convert(String column, Object value);

	public final static Map COLUMN_CONVERTER = new HashMap();
}
