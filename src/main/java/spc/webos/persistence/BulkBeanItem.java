package spc.webos.persistence;

import java.util.List;
import net.sf.cglib.beans.BulkBean;

/**
 * ����VO���Խ�������VO�����ù�ϵ
 * 
 * @author Hate
 * 
 */
public class BulkBeanItem
{
	BulkBean voProperties; // ��������һ��VO����������й�������
	List properties; // ÿһ�����������Ե�������Ϣ�� ����ÿһ������Ϊһ��BulkBean[]

	public List getProperties()
	{
		return properties;
	}

	public void setProperties(List properties)
	{
		this.properties = properties;
	}

	public BulkBean getVoProperties()
	{
		return voProperties;
	}

	public void setVoProperties(BulkBean voProperties)
	{
		this.voProperties = voProperties;
	}
}
