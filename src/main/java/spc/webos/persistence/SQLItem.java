package spc.webos.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import freemarker.template.Template;

/**
 * SQL������
 * 
 * @author spc
 * 
 */
public class SQLItem implements Serializable
{
	public String sql; // SQL����freemarker�﷨ģ��
	public Template t; // SQLģ��
	// Web�������ʰ�ȫ����:0:��Ҫ������Ȩprivate, 1:��¼��Ȩlogin��9����������public
	public int auth;
	public boolean sequnce = false; // 900, �Ƿ��ֹ������Ӽ�¼ʱ�ɳ־ò����Ψһ��¼
	public boolean pretty; // 900, �Ƿ�����sql ȥ��where 1=1
//	public String cache; // application�ж���Ļ�������(BeanName), ���ڻ�������, һ�㲻�����û���,
	// ��Ϊû�������������»ع����µĻ���ʧ������...
	public short type; // Sql �������͡�select, delete, update, insert...
	public String resultClass; // �������
	public String rowIndex; // ������messageformat, Ϊ�������ÿ�����ݽ���������,����̶�����
	public String[] dependence; // ִ�е�ǰSQLǰ���Ⱦ�SQL
	public String[] injection; // 938_20170226 ��ֹSQLע�빥��
	public String jt; // �൱���ƶ�����ԴBeanName
	public String slave; // �����ݿ⼯Ⱥ��
	public boolean firstRowOnly; // �Ƿ�ֻ��ȡ������ϵĵ�һ��
	public String delim; // ��delete,update����¿���ִ�ж���sql���� �ָ�����. Ĭ��;
//	public Class[] flushClazzArray; // ��SQLִ�к���Ӱ��ı��ࣩ�Ļ�����Ϣ����Ҫ�����Щ���棬flushCaches
//	public String[] preFnodes; // ǰ��ִ�еĺ�������, flow nodes��
//	public String[] postFnodes; // ����ִ�еĺ�������, flow nodes��
	public Script preScript; // ǰ��ִ�еĽű�
	public List postScripts; // ����ִ�еĽű�
	public Map dict; // ������ݿ��в�ѯ���ֶ���spring ApplicationCxt��ת��
	// public Script postFn;
	// public Script postExp; // �ڲ����ʽ,r0 = r1+r2;
	public boolean prepared; // �Ƿ���Ҫprepared����
	public boolean procedure; // �Ƿ�����صĴ洢����

	public Map columnConverters; // ���õ���ת��
	public Map column; // ��������Map������ģʽ�¶��ֶ����Ĵ�Сд�����У�������ESB��message����

	private static ThreadLocal CURRENT_ITEM = new ThreadLocal(); // ��ǰ����ִ�е�sqlitem
	public final static int AUTH_private = 0;
	public final static int AUTH_login = 1;
	public final static int AUTH_public = 9;

	public static void setCurrentItem(SQLItem item)
	{
		CURRENT_ITEM.set(item);
	}

	public static SQLItem getCurrentItem()
	{
		return (SQLItem) CURRENT_ITEM.get();
	}

	public boolean isSelect()
	{
		return type == SELECT;
	}

	public static final short SELECT = 0;
	public static final short DELETE = 1;
	public static final short UPDATE = 2;
	public static final short INSERT = 3;
	// public static final short PREPARED = 4;
	public static final short CALL = 4;
	// public static final String DEFAULT_DELIM = ";";
	// public static final String IS_EMPTY_SQL = "_IS_EMPTY_SQL_"; // ��sql���,
	// ��ʱ��ִ��SQL
	public static final String RESULT_CLASS_LIST = "list";
	public static final String RESULT_CLASS_XMAP = "xmap";
	public static final String RESULT_CLASS_MAP = "map";
	public static final String RESULT_CLASS_JSON = "json";
	public static final String DEFAULT_RESULT_CLASS = RESULT_CLASS_LIST; // Ĭ�ϵ�����������
	private static final long serialVersionUID = 1L;

	public String toString()
	{
		return "class=" + resultClass + ", jt=" + jt + ", type=" + type;
	}
}
