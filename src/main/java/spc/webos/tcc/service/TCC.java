package spc.webos.tcc.service;

/**
 * TCCԭ�ӷ�����ˮ��¼
 * 
 * @author chenjs
 *
 */
public interface TCC
{
	String getSn();

	Integer getStatus();

	void setStatus(Integer status);

	void setTryTm(String tm);

	void setConfirmTm(String tm);

	void setCancelTm(String tm);
}
