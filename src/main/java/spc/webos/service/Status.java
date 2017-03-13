package spc.webos.service;

import java.util.HashMap;
import java.util.Map;

import spc.webos.advice.log.LogTrace;

public interface Status
{
	// ����״̬�ӿڣ��ṩ����ѡ���������Ի��������ĵĲ���״̬��Ϣ
	@LogTrace
	Map checkStatus(Map param);

	@LogTrace
	boolean changeStatus(Map param);

	@LogTrace
	void refresh() throws Exception;
	
	boolean needRefresh();

	static Map<String, Status> SERVICES_PROXY = new HashMap<>(); // spring��������

	static Map<String, Status> SERVICES = new HashMap<>(); // ϵͳ���з���
}
