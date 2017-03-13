package spc.webos.endpoint;

import java.io.Serializable;
import java.util.Map;

import spc.webos.exception.Status;
import spc.webos.util.StringX;

public class Executable implements Serializable
{
	private static final long serialVersionUID = 1L;
	public byte[] correlationID;
	public byte[] messageId;
	public int timeout = 60; // seconds
	public byte[] request;
	public Object reqExt; // ������չ��Ϣ��for AS400����
	public byte[] response;
	public Object repExt; // ��Ӧ��չ��Ϣ��for AS400����
	public String cicsProgram; // for CICS ��ʽ����
	public long reqTime;
	public long resTime;
	public Status status;
	public Object reqmsg;
	public Object resmsg;
	public boolean withoutReturn;
	// 2012-07-05 ��ʾ�����Ƿ��ͣ���������Ѿ������ٳ����쳣����ʹ�ü�Ⱥģʽ���������󣬷�ֹ�ظ�����
	public boolean cnnSnd;
	public boolean query; // 2014-08-08 ��ѯ�ཻ�ף��ɼ�Ⱥ�ظ�����
	public String reqQName; // ���������
	public String repQName; // Ӧ�������
	public String location; // 2012-05-01 ���ӷ���λ�ò��������ڶ�̬TCP/HTTP
	public Map<String, String> reqHttpHeaders;
	public Map<String, String> repHttpHeaders;
	public int httpStatus; // httpӦ���״̬��
	// public ITCPCallback callback; // 700 2013-08-25

	public Executable()
	{
		this.reqTime = System.currentTimeMillis();
	}

	public Executable(String correlationID, byte[] request)
	{
		this(correlationID, request, -1, null);
	}

	public Executable(String correlationID, byte[] request, int timeout)
	{
		this(correlationID, request, timeout, null);
	}

	public Executable(String correlationID, byte[] request, int timeout, String repQName)
	{
		this.correlationID = correlationID.getBytes();
		this.timeout = timeout;
		this.request = request;
		this.repQName = repQName;
		this.reqTime = System.currentTimeMillis();
	}

	public Executable(String correlationID, byte[] request, int timeout, String reqQName,
			String repQName)
	{
		this.correlationID = correlationID.getBytes();
		this.timeout = timeout;
		this.request = request;
		this.reqQName = reqQName;
		this.repQName = repQName;
		this.reqTime = System.currentTimeMillis();
	}

	public Executable(byte[] request, int timeout)
	{
		this.timeout = timeout;
		this.request = request;
		this.reqTime = System.currentTimeMillis();
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getCorrelationID()
	{
		return correlationID == null ? StringX.EMPTY_STRING : new String(correlationID);
	}

	public void setCorrelationID(String correlationID)
	{
		if (StringX.nullity(correlationID)) return;
		this.correlationID = correlationID.getBytes();
	}
}
