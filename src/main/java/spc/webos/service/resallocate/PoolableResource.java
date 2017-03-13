package spc.webos.service.resallocate;

import spc.webos.util.JsonUtil;

/**
 * ���Է���ط������Դ
 * 
 * @author spc
 * 
 */
public class PoolableResource
{
	public Resource resource; // ������Դ����
	public int holdTm; // ��λ��
	public long assignTm; // ����ʱ��
	public boolean available = true; // �Ƿ���ã����Ϊtrue ��ʾ���ã� false��ʾ�Ѿ�����
	public String sn; // ռ�ô���Դ�ķ�����ˮ��
	public String group; // ��Դ����
	public String id; // ��ԴID

	public PoolableResource()
	{
	}

	public PoolableResource(Resource resource)
	{
		this.resource = resource;
		group = resource.group();
		id = resource.id();
	}

	public boolean match(String key)
	{
		return resource.match(key);
	}

	public String group()
	{
		return resource.group();
	}

	/**
	 * �������Դ
	 * 
	 * @param sn
	 * @param holdTime
	 * @return
	 */
	public synchronized boolean hold(String sn, int holdTime)
	{
		if (!available) return false;
		available = false;
		this.sn = sn;
		this.holdTm = holdTime;
		assignTm = System.currentTimeMillis();
		return true;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	public void release()
	{
		available = true;
		sn = null;
	}

	public String toString()
	{
		return JsonUtil.obj2json(this);
	}

	public Resource getResource()
	{
		return resource;
	}

	public void setResource(Resource resource)
	{
		this.resource = resource;
	}

	public int getHoldTime()
	{
		return holdTm;
	}

	public void setHoldTime(int holdTime)
	{
		this.holdTm = holdTime;
	}

	public long getAssignTime()
	{
		return assignTm;
	}

	public void setAssignTime(long assignTime)
	{
		this.assignTm = assignTime;
	}

	public String getHoldSn()
	{
		return sn;
	}

	public void setHoldSn(String holdSn)
	{
		this.sn = holdSn;
	}
}
