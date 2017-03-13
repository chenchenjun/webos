package spc.webos.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import spc.webos.service.Status;

/**
 * @author spc
 */
public interface Cache<K, V> extends Status
{
	public final static Map<String, Cache<?, ?>> CACHES = new HashMap<>();

	void setName(String name);

	Collection<K> getKeys();

	int size();

	// V getMessage(String key) throws Exception; // for ���ʻ�

	// ��cache�л�ȡ��ɾ��
	V poll(K key) throws Exception;

	V poll(K key, WaitWithTime wwt) throws Exception;

	V poll(K key, long timeout) throws Exception;

	V get(K key) throws Exception;

	/**
	 * @param key
	 * @param o
	 * @return true means sucess.. false means failure..
	 */
	V put(K key, V o);

	/**
	 * ��ջ���������Ϣ
	 */
	void removeAll();

	/**
	 * ���������ĳһ�ؼ��ֵ���Ϣ
	 * 
	 * @param key
	 */
	V remove(K o);

	/**
	 * ��û��������
	 * 
	 * @return
	 */
	String getName();

	V put(K key, V obj, int expireSeconds);

	void evictExpiredElements(); // 700 2012-05-25
}
