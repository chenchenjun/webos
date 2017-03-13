package spc.webos.service.resallocate.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.service.BaseService;
import spc.webos.service.resallocate.PoolableResource;
import spc.webos.service.resallocate.Resource;
import spc.webos.service.resallocate.ResourceGroup;
import spc.webos.service.resallocate.ResourcePoolService;
import spc.webos.util.StringX;

public abstract class AbstractResourcePoolServiceImpl extends BaseService
		implements ResourcePoolService
{
	protected List resources = new ArrayList(); // ��ǰ������Դ�����б�, �޷�����ʽ
	protected Map resourcePool = new HashMap(); // ��ǰ��Դ��������
	// ��ǰ�Ѿ�ռ����Դ�������� ����ˮ��Ϊ����, ���ǵ����ղ�����ͬ���ԣ�����hashtable
	protected Map assignedPool = new Hashtable();
	public final static int INTERVAL = 20; // �߳���Ϣʱ��

	public List apply(String sn, String key, int holdTime, int timeout, int matchType)
			throws AppException
	{
		if (log.isDebugEnabled()) log.debug("sn:" + sn + ",key:" + key + ",holdTm:" + holdTime
				+ ",timeout:" + timeout + ",matchType:" + matchType);
		if (!contain(key)) throw new AppException(AppRetCode.RES_ALLOCATE_APPLY_NO);
		List preses = apply(sn, key, holdTime);
		if (timeout < INTERVAL || preses.size() > 0) return preses;
		long start = System.currentTimeMillis();
		while (preses.size() <= 0 && (System.currentTimeMillis() - start < timeout))
		{
			try
			{
				Thread.sleep(INTERVAL);
			}
			catch (Exception e)
			{
				return null;
			}
			preses = apply(sn, key, holdTime);
		}
		return preses;
	}

	public boolean contain(String key)
	{
		if (resourcePool.size() == 0) return false;
		Iterator groups = resourcePool.values().iterator();
		while (groups.hasNext())
		{
			ResourceGroup group = (ResourceGroup) groups.next();
			if (group.match(key)) return true;
		}
		return false;
	}

	protected List apply(String sn, String key, int holdTime)
	{
		ArrayList preses = new ArrayList();
		Iterator groups = resourcePool.values().iterator();
		while (groups.hasNext())
		{
			ResourceGroup group = (ResourceGroup) groups.next();
			// PoolableResource pres = (PoolableResource) group.get(0);
			if (group.match(key))
			{
				if (log.isDebugEnabled())
					log.debug("match key:" + key + ", group: " + group.group());
				PoolableResource npres = apply(group, sn, holdTime);
				if (npres != null) preses.add(npres);
				return preses;
			}
			else if (log.isDebugEnabled())
				log.debug("unmatch key:" + key + ", group: " + group.group());
		}
		return preses;
	}

	protected PoolableResource apply(ResourceGroup group, String sn, int holdTime)
	{
		if (group.size() <= 0) return null; // �����Դ������û����Դ����ֱ�ӷ���
		int index = ((int) Math.random() * 10000) % group.size(); // �����������㣬����˳�����ʱ���ڷ���ԭ���²������ܱ��
		int times = 0;
		while (times < group.size())
		{
			PoolableResource pres = (PoolableResource) group.get(index++);
			if (pres.isAvailable() && pres.hold(sn, holdTime))
			{
				if (register(pres)) return pres; // �Ǽǳɹ��򷵻أ������������Ϊͬһ��ˮ�������ε���ʧ�ܣ�
				pres.release();
				return null;
			}
			if (index >= group.size()) index = 0;
			times++;
		}
		return null;
	}

	public List getResources(Map param)
	{
		return resources;
	}

	protected synchronized boolean register(PoolableResource pres)
	{
		// �������һ�㲻���׷�������ͬһ����ˮ�Ŷ�����룬���׵�����Դ����й©
		if (assignedPool.containsKey(pres.sn))
		{
			log.warn("apply sn: " + pres.sn + " applied & not release!!!");
			return false;
		}
		List preses = (List) assignedPool.get(pres.sn);
		if (preses == null)
		{
			preses = new ArrayList();
			assignedPool.put(pres.sn, preses);
		}
		preses.add(pres);
		return true;
	}

	/**
	 * ��ռ����ˮ�����ͷ��������Դ
	 */
	public boolean release(String sn)
	{
		if (StringX.nullity(sn)) return false;
		List preses = (List) assignedPool.get(sn);
		if (preses == null)
		{
			if (log.isInfoEnabled()) log.info("No sn:" + sn + ", perhaps auto release...");
			return false;
		}
		assignedPool.remove(sn);
		for (int i = 0; i < preses.size(); i++)
		{
			PoolableResource pres = (PoolableResource) preses.get(i);
			if (pres.isAvailable() || !pres.sn.equals(sn))
			{ // ��ǰ��Դ�Ѿ����ͷţ����ߵ�ǰ��Դ��ռ����ˮ�ŷǵ�ǰ����/�ͷ���ˮ�ţ����ͷ�
				if (log.isInfoEnabled()) log.info("current res has been allocated, release sn: "
						+ sn + ", hold sn: " + pres.sn);
				return false;
			}
			log.debug("release...");
			pres.release();
		}
		return true;
	}

	/**
	 * ͨ��Job������õ��Զ�������Դ
	 */
	public List recycle()
	{
		List recycleSn = new ArrayList();
		Object[] sns = assignedPool.keySet().toArray();
		if (sns == null || sns.length == 0)
		{
			log.info("no hold sn...");
			return recycleSn;
		}
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < sns.length; i++)
		{
			String sn = (String) sns[i];
			List preses = (List) assignedPool.get(sn);
			PoolableResource pres = (PoolableResource) preses.get(0);
			if (currentTimeMillis - pres.assignTm > pres.holdTm * 1000)
			{ // 452, holdTm��λ�Ӻ����Ϊ��
				assignedPool.remove(sn);
				for (int j = 0; j < preses.size(); j++)
					((PoolableResource) preses.get(j)).release(); // �ͷŵ�ǰ��ԴΪ����
				recycleSn.add(sn);
			}
		}
		if (log.isInfoEnabled())
			log.info("total num of recycle is : " + recycleSn.size() + " of " + sns.length);
		if (recycleSn.size() > 0) log.warn("timeout sn:" + recycleSn);
		return recycleSn;
	}

	public Map checkStatus(Map param)
	{
		String group = StringX.null2emptystr(param.get("group")); // �鿴ָ�������Դ���
		log.info("check group:" + group);
		Map status = new HashMap();
		Iterator res = resourcePool.values().iterator();
		List resList = new ArrayList();
		while (res.hasNext())
		{
			ResourceGroup resGroup = (ResourceGroup) res.next();
			if (StringX.nullity(group) || group.equals(resGroup.group())) resList.add(resGroup);
		}
		if (resList.size() > 0)
		{
			log.info("res size:" + resList.size());
			status.put("resourcePool", resList);
		}
		else log.info("res is null for " + group);

		Iterator assign = assignedPool.values().iterator();
		List assignList = new ArrayList();
		while (assign.hasNext())
		{
			assignList.add(assign.next());
		}
		if (assignList.size() > 0)
		{
			log.info("assigned pool size:" + assignList.size());
			status.put("assignedPool", assignList);
		}
		else log.info("assigned pool is null for " + group);
		return status;
	}

	public void refresh() throws Exception
	{
		log.info("refresh...");
		if (!needRefresh()) return;

		List resources = loadResource();
		if (resources == null) resources = new ArrayList();
		Map resourcePool = initResPool(resources);

		this.resources = resources;
		this.resourcePool = resourcePool;
		assignedPool = new Hashtable(); // ��������ѵǼ���Ϣ

		if (log.isInfoEnabled())
			log.info("resources: " + resources.size() + ", resourcePool: " + resourcePool.size());
	}

	protected Map initResPool(List resources) throws Exception
	{
		log.debug("initResPool...");
		if (resources.size() <= 0) log.warn("resources is empty!!!");

		Map resourcePool = new HashMap();
		for (int i = 0; i < resources.size(); i++)
		{
			Resource res = (Resource) resources.get(i);
			PoolableResource pres = new PoolableResource(res);
			ResourceGroup group = (ResourceGroup) resourcePool.get(res.group());
			if (group == null)
			{
				group = new ResourceGroup(pres);
				resourcePool.put(res.group(), group);
			}
			group.add(pres);
		}
		return resourcePool;
	}

	public abstract List loadResource() throws Exception;
}
