package spc.webos.service.resallocate;

import java.util.List;
import java.util.Map;

import spc.webos.exception.AppException;

public interface ResourcePoolService
{
	// �����Դ�ص�������Դ��Ϣ
	List getResources(Map param);

	// ��ȡָ����Դ
	List apply(String sn, String key, int matchType, int holdTime, int timeout) throws AppException;

	// ��Դ�����Ƿ����ĳ��ָ������Դ
	boolean contain(String key);

	// �ͷŵ�ǰ��ˮ��ռ�õ���Դ
	boolean release(String sn);

	// ��ʱΪÿ����Դ�ػ��ճ�ʱ��Դ
	List recycle();

	// ���������Դ�ص�״̬
	Map checkStatus(Map param);

	void refresh() throws Exception;
}
