package spc.webos.service.seq.impl;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import spc.webos.service.seq.UUID;

public class TimeMillisUUID implements UUID
{
	public TimeMillisUUID()
	{
	}

	public TimeMillisUUID(int workerId)
	{
		this.workerId = workerId;
		if (workerId > MAX_WORKER_ID)
			throw new RuntimeException("workerId:" + workerId + " >" + MAX_WORKER_ID + " !!!");
	}

	public synchronized long uuid()
	{
		long millis = System.currentTimeMillis();
		// ���������һ��������0
		if (millis > lastMillis) return uuid(lastMillis = millis, seq = 0);

		if (seq < MAX_SEQ_ID) return uuid(lastMillis, ++seq); // �����ͬ������������
		// ����λ�Ѿ��ﵽ9����Ҫ������һ�����룬 ����seq��0
		return uuid(++lastMillis, seq = 0);
	}

	public String format(long uuid)
	{
		String dt = FastDateFormat.getInstance(format).format(new Date(uuid / 100000));
		String str = String.valueOf(uuid);
		return dt + str.substring(str.length() - 5);
	}

	// workId + ʱ��� + ������
	protected long uuid(long millis, int seq)
	{
		return millis * 100000 + seq * 1000 + workerId; // �����ͬ������������
	}

	public final static int MAX_SEQ_ID = 99; // ���seq id, ���ǵ�long�Ĵ�С���Ϊ19λ
	public final static int MAX_WORKER_ID = 999; // ���worker id
	protected int workerId;
	private int seq; // ͬһ�����µ�����, synchronizedģʽ��cpu����һ�������벻�ᳬ��100��һ��20��
	private long lastMillis = 0; // ��Ϊ�����������ˮ�����ɵĻ�����ÿ������һ��Ψһ��ʱ�䣬���һ�������������������δ��һ����
	protected String format = "yyMMddHHmmssSSS"; // 0:����ģʽ��1:yyMMddHHmmssSSS

	public void setWorkerId(int workerId)
	{
		if (workerId > MAX_WORKER_ID)
			throw new RuntimeException("workerId:" + workerId + " >" + MAX_WORKER_ID + " !!!");
		this.workerId = workerId;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}
}
