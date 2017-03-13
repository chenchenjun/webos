package spc.webos.service.resallocate;

import java.util.ArrayList;
import java.util.List;

/**
 * ��Դ�飬������һ������Դ
 * 
 * @author chenjs 2012-01-01
 * 
 */
public class ResourceGroup
{
	public PoolableResource pres; // ������Դ����, ����ƥ���Ƿ���ϵ�ǰ��Դ��
	public List<PoolableResource> group = new ArrayList<PoolableResource>();

	public ResourceGroup()
	{
	}

	public ResourceGroup(PoolableResource pres)
	{
		this.pres = pres;
	}

	public boolean match(String key)
	{
		return pres.match(key);
	}

	public String group()
	{
		return pres.group();
	}

	public void add(PoolableResource pres)
	{
		group.add(pres);
	}

	public int size()
	{
		return group.size();
	}

	public PoolableResource get(int index)
	{
		return group.get(index);
	}
}
