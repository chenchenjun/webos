package spc.webos.service.timeout;

import java.util.List;

/**
 * ��ʱ��Ϣ�����࣬����ʵ�ֿ�����MQ��DB���ڴ��ģʽ
 * 
 * @author spc
 * 
 */
public interface TimeoutService
{
	// �ҵ���ʱ����
	List find() throws Exception;

	// �Ƴ���ʱ��Ϣ������Ϊtrue��ʾ�Ƴ��ɹ���������ʱ����false��ʾ�Ƴ�ʧ�ܣ��������Ѿ����Ƴ�
	boolean remove(Timeout timeout) throws Exception;

	// ���ӳ�ʱ��Ϣ
	boolean add(Timeout timeout) throws Exception;
}
