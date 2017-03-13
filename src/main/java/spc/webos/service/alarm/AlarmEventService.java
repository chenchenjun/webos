package spc.webos.service.alarm;

import java.util.Map;

import spc.webos.model.AlarmPO;

public interface AlarmEventService
{
	// ����һ���¼�
	AlarmPO proccess(AlarmPO event) throws Exception;

	// ͨ��alarm_event���id����һ���¼�
	String proccessEvent(String id) throws Exception;
	
	Map<String, AlarmPO> proccessEvents(String[] ids) throws Exception;

	// ��ȡһ��������Ϣ����
	String getEventMsg(String id) throws Exception;

	// ����һ��������Ϣ
	String sendEventMsg(String id);
}
