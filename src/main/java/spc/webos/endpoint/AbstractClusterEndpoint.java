package spc.webos.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ��Ⱥcluster
 * 
 * @author spc
 * 
 */
public abstract class AbstractClusterEndpoint implements Endpoint
{
	public void init() throws Exception
	{
	}

	public void setLocation(String location) throws Exception
	{
		throw new RuntimeException("No method!!!");
	}

	protected boolean execute(Endpoint endpoint, Executable exe, int failCount) throws Exception
	{
		try
		{
			endpoint.execute(exe);
			return true;
		}
		catch (Exception e)
		{
			if (log.isInfoEnabled()) log.info("query:" + exe.query + ", cnnSnd: " + exe.cnnSnd
					+ ", failCount:" + failCount + ", retryTimes:" + retryTimes + ", e:" + e);
			if (!isClusterRetry(exe, e) || failCount >= retryTimes - 1) throw e; // 802,
																					// �޸�Ϊ-1��
			if (retryInterval > 0) Thread.sleep(retryInterval);
		}
		return false;
	}

	protected boolean isClusterRetry(Executable exe, Exception e)
	{ // ����Ѿ����ͳɹ���ʹ�ü�Ⱥ�������·���
		return exe.query || !exe.cnnSnd;
	}

	public Endpoint clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}

	protected int retryInterval;
	protected int retryTimes = -1;
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public int getRetryInterval()
	{
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval)
	{
		this.retryInterval = retryInterval;
	}

	public void setRetryTimes(int retryTimes)
	{
		this.retryTimes = retryTimes;
	}

	public int getRetryTimes()
	{
		return retryTimes;
	}
}
