package spc.webos.service.timeout;

/**
 * ��ʱ��Ϣ�ӿ�
 * 
 * @author spc
 * 
 */
public interface Timeout
{
	String getInTime(); // ���볬ʱ�۲��ʱ��: "20110909010101001"

	Integer getTimeout(); // ��ʱʱ��, ��λ����

	String getSn(); // ��ʱ��ϢΨһ��ˮ��
}
