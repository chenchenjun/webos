package spc.webos.persistence.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VO��������Ϣ
 * 
 * @author Hate
 * 
 */
public class ClassDesc
{
	public String name; // ����: ����package��
	public String table; // ��Ӧ�����ݿ��������
	public String parent; // ����
	boolean sequence = false; // 900, �˱��Ƿ���Ҫ���ֹ�Ψһ��ֵ
	public String ds; // ��Ӧ������Դ����spring context�о���һ��jdbcTemplate������
	public String slave; //
//	public boolean cache; // �Ƿ񻺴�˱��¼
	public boolean prepare; // �Ƿ���ҪPrepare��ʽִ��
	public String remark; // ����������Ϣ
	public List properties; // ֱ��������������б�
	public List voProperties; // ������VO��������
	public String declare; // Ԥ��������
	public int insertMode; // 0 ��Ȼģʽ, 1 ��selectģʽ
	public String deletePreFn;
	public String deletePostFn;
	public String insertPreFn;
	public String insertPostFn;
	public String selectPostFn;
	public String selectPreFn;
	public String updatePreFn;
	public String updatePostFn;
	public List staticFields = new ArrayList();
	public Map columnConverter; // �ֶ�ת��

	public List getStaticFields()
	{
		return staticFields;
	}

	public void setStaticFields(List staticFields)
	{
		this.staticFields = staticFields;
	}

	public String getInsertPreFn()
	{
		return insertPreFn;
	}

	public void setInsertPreFn(String insertPreFn)
	{
		this.insertPreFn = insertPreFn;
	}

	public String getInsertPostFn()
	{
		return insertPostFn;
	}

	public void setInsertPostFn(String insertPostFn)
	{
		this.insertPostFn = insertPostFn;
	}

	public String getSelectPostFn()
	{
		return selectPostFn;
	}

	public void setSelectPostFn(String selectPostFn)
	{
		this.selectPostFn = selectPostFn;
	}

	public String getSelectPreFn()
	{
		return selectPreFn;
	}

	public void setSelectPreFn(String selectPreFn)
	{
		this.selectPreFn = selectPreFn;
	}

	public String getUpdatePreFn()
	{
		return updatePreFn;
	}

	public void setUpdatePreFn(String updatePreFn)
	{
		this.updatePreFn = updatePreFn;
	}

	public String getUpdatePostFn()
	{
		return updatePostFn;
	}

	public void setUpdatePostFn(String updatePostFn)
	{
		this.updatePostFn = updatePostFn;
	}

	public int getInsertMode()
	{
		return insertMode;
	}

	public void setInsertMode(int insertMode)
	{
		this.insertMode = insertMode;
	}

	public String getDeclare()
	{
		return declare;
	}

	public void setDeclare(String declare)
	{
		this.declare = declare;
	}

	public List getVoProperties()
	{
		return voProperties;
	}

	public void setVoProperties(List voProperties)
	{
		this.voProperties = voProperties;
	}

	public List getProperties()
	{
		return properties;
	}

	public void setProperties(List properties)
	{
		this.properties = properties;
	}

	public String getDataSource()
	{
		return ds;
	}

	public void setDataSource(String dataSource)
	{
		this.ds = dataSource;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	public String getParent()
	{
		return parent;
	}

	public void setParent(String parent)
	{
		this.parent = parent;
	}

//	public boolean isCache()
//	{
//		return cache;
//	}
//
//	public void setCache(boolean cache)
//	{
//		this.cache = cache;
//	}

	public boolean isPrepare()
	{
		return prepare;
	}

	public void setPrepare(boolean prepare)
	{
		this.prepare = prepare;
	}

	public String getDeletePreFn()
	{
		return deletePreFn;
	}

	public void setDeletePreFn(String deletePreFn)
	{
		this.deletePreFn = deletePreFn;
	}

	public String getDeletePostFn()
	{
		return deletePostFn;
	}

	public void setDeletePostFn(String deletePostFn)
	{
		this.deletePostFn = deletePostFn;
	}

	public Map getColumnConverter()
	{
		return columnConverter;
	}

	public void setColumnConverter(Map columnConverter)
	{
		this.columnConverter = columnConverter;
	}
}
