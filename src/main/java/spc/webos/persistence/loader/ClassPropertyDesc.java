package spc.webos.persistence.loader;

/**
 * VO�����Ե�������Ϣ
 * 
 * @author Hate
 * 
 */
public class ClassPropertyDesc
{
	// String table; // ���ݿ��������
	String column; // ���ݿ���ֶ���
	String jdbcType; // ���ݿ��ֶ�����CHAR, NUMERIC
	String javaType; // Java����������
	String name; // Java��������
	boolean primary; // �Ƿ�������־
	boolean uuid; // �Ƿ����uuid��ʽ���ɴ��ֶ�
	public boolean version; // �Ƿ�ʹ�ô��ֶ���Ϊupdate�ֹ�����810�汾�Ժ��ṩ
	public boolean updatable = true; // 900, �Ƿ�֧�ֿ��޸�
	String defaultValue; // �ֶ�Ĭ��ֵ
	String nullValue; // ����������͵�����, �������ض���ʱ��Ϊ�ǿ�(NULL)
	String insert; // insert �� update �������ݿ�������ֶ�ת��
	String update;
	String select; // select����������ֶ�ת��
	String remark; // �ֵ����Ľ���
	String sequence; // �Ƿ������������ݿ��ֶ�û����������ʶ, ��Ҫ�ֹ�, auto��ʾ�������ݿ��Զ�, manual��ʾ��ѯ�ֹ�
	boolean prepare; // �Ƿ���Ҫprepareģʽ
	public static String SEQUENCE_AUTO = "AUTO"; // ���ݿ�������Զ����ӻ���
	public static String SEQUENCE_MANUAL = "MANUAL"; // �ֶ�ά�����ӻ���
	// public static String JDBC_TYPE_NUMERIC = "NUMERIC";
	// public static String JDBC_TYPE_CHAR = "CHAR";
	// public static String JDBC_TYPE_DATE = "DATE";
	// public static String JDBC_TYPE_TIME = "TIME";
	// public static String JDBC_TYPE_TIMESTAMP = "TIMESTAMP";

	public String getSequence()
	{
		return sequence;
	}

	public void setSequence(String sequence)
	{
		this.sequence = sequence;
	}

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public String getInsert()
	{
		return insert;
	}

	public void setInsert(String insert)
	{
		this.insert = insert;
	}

	public String getJavaType()
	{
		return javaType;
	}

	public void setJavaType(String javaType)
	{
		this.javaType = javaType;
	}

	public String getJdbcType()
	{
		return jdbcType;
	}

	public void setJdbcType(String jdbcType)
	{
		this.jdbcType = jdbcType;
	}

	public String getNullValue()
	{
		return nullValue;
	}

	public void setNullValue(String nullValue)
	{
		this.nullValue = nullValue;
	}

	public boolean isPrimary()
	{
		return primary;
	}

	public void setPrimary(boolean primary)
	{
		this.primary = primary;
	}

	public boolean getUpdatable()
	{
		return updatable;
	}

	public void setUpdatable(boolean updatable)
	{
		this.updatable = updatable;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getSelect()
	{
		return select;
	}

	public void setSelect(String select)
	{
		this.select = select;
	}

	// public String getTable()
	// {
	// return table;
	// }
	//
	// public void setTable(String table)
	// {
	// this.table = table;
	// }

	public String getUpdate()
	{
		return update;
	}

	public void setUpdate(String update)
	{
		this.update = update;
	}

	public boolean isPrepare()
	{
		return prepare;
	}

	public void setPrepare(boolean prepare)
	{
		this.prepare = prepare;
	}

	public boolean isUuid()
	{
		return uuid;
	}

	public void setUuid(boolean uuid)
	{
		this.uuid = uuid;
	}

	public boolean isVersion()
	{
		return version;
	}

	public void setVersion(boolean version)
	{
		this.version = version;
	}
}
