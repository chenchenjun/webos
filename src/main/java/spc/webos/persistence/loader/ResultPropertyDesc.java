package spc.webos.persistence.loader;

/**
 * VO�й���������VO�����Ե�������Ϣ
 * 
 * @author Hate
 * 
 */
public class ResultPropertyDesc
{
	public String name; // ������
	public String javaType; // VO������
	public String[] getter; // getter��������
	public String[] setter; // setter��������
	public Class[] clazzArray; // �������Ե���������
	public boolean manyToOne; // true��ʾmanyToOne. false��ʾoneToMany
	public String select; // ��������Բ��Ǻ�ĳvo������ ����һ����ѯ���Ľ����select���ʾ�˲�ѯ����Id
	public String remark; // ע��

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String[] getGetter()
	{
		return getter;
	}

	public void setGetter(String[] getter)
	{
		this.getter = getter;
	}

	public String getJavaType()
	{
		return javaType;
	}

	public void setJavaType(String javaType)
	{
		this.javaType = javaType;
	}

	public boolean isManyToOne()
	{
		return manyToOne;
	}

	public void setManyToOne(boolean manyToOne)
	{
		this.manyToOne = manyToOne;
	}

	public String[] getSetter()
	{
		return setter;
	}

	public void setSetter(String[] setter)
	{
		this.setter = setter;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Class[] getClazzArray()
	{
		return clazzArray;
	}

	public void setClazzArray(Class[] clazzArray)
	{
		this.clazzArray = clazzArray;
	}

	public String getSelect()
	{
		return select;
	}

	public void setSelect(String select)
	{
		this.select = select;
	}
}
