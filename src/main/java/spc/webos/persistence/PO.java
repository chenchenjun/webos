package spc.webos.persistence;

import java.io.Serializable;

/**
 * @author spc
 */
public interface PO extends Serializable, Cloneable, Comparable
{
	/**
	 * ÿ��VO�����һ����Ӧ���ݶ���NULL������״̬, �����ҪĬ��ֵ, ����Ҫ����init����
	 * 
	 */
	// void init();
	//
	// void close();

//	void destory();

	/**
	 * ���VO�е�����ΪNull(��Ӧ���ݿ��NULL)
	 * 
	 */
//	void setNULL();

	// �־ò����ǰ����
	void beforeLoad();

	// �־ò���غ����
	void afterLoad();

	// ������ֹ�seq�������ӵ��ýӿ�
//	void setManualSeq(Long seq);

	/**
	 * ����������������ȡ�����ַ���
	 * 
	 * @param delim
	 * @return
	 */
//	String primary(); // 900_20160115, JdbcUtil

//	Map getPrimary();

//	String table(); // 900_20160115

//	String[] blobFields();  // 900_20160115, AttachmentCtrller

	/**
	 * �����ⲿ��������ΪNULL�� һ���������л����紫�� ����transient���Ժ󣬽����ᱻ���л�
	 */
	// void setOuterFieldNULL();
	/**
	 * VO��Key,
	 * ����HashMap�����������������ֻ��һ����������ôgetKey�ͷ��ش�������������ж����������������һ��ȫ��sequence�ֶ�
	 * ����ô���ش�sequence
	 * 
	 * @return
	 */
//	Serializable getKey();

//	String getKeyName();
}
