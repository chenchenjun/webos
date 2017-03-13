package spc.webos.constant;

/**
 * ϵͳ�������ͳһ����ģʽʵ��, ������Ϊ12λ: XX+XXXX ��λ����� ���˵�� ��ע 0X �ɹ�
 * ����ɹ���ķ�����Ϣ�룬ͨ�óɹ���Ϊ000000 1X ϵͳ����� 10 ͨ������� ϵͳͨ�Ų����Ĵ����� 11 �ļ������ 12 ���ݿ������ 13
 * �м������� MQ,tuxedo��MB��WPS�� 14ͨ�÷��������(����, ת����) 19 ����ϵͳ����� 2X ��������� 20 ���ĸ�ʽ����� 29
 * ������������� 3X ҵ������� 30 ҵ����Ȩ���� 31 �˻������ 39 ����δ����ҵ����� 99 ����δ������� 2011-09-27 chenjs
 * 5.2 ���������ڲ�ͬ�淶�������Զ���ʹ��
 * 
 * @author spc
 * 
 */
public class AppRetCode
{
	public static String SUCCESS = "000000"; // ͨ�óɹ�

	public static String CMMN_UNDEF_EX = "999999"; // δ������쳣
	public static String CMMN_NULLPOINT_EX = "999998"; // ��ָ���쳣

	public static String NET_COMMON = "109999"; // ����ͨ���쳣
	public static String PROTOCOL_SOCKET = "100001"; // socketЭ�������ͨ���쳣args=ip:port
	public static String PROTOCOL_HTTP = "100002"; // HTTPЭ�������ͨ���쳣args=url
	public static String PROTOCOL_MQ = "100003"; // MQͨѶ�쳣
	public static String CMMN_BUF_TIMEOUT = "100004"; // ��ȡbuf���ݳ�ʱ
	public static String URL_SECURIY = "100010"; // URL��ȫ
	public static String UN_LOGIN = "100011"; // δ��½
	public static String UN_LOGIN_IP = "100012"; // ��login ip

	public static String DB_EX = "129999"; // ���ݿ�ͨ���쳣
	public static String DB_UNDEFINED_SQLID = "120001"; // Ϊ�����sql�ڵ�args=sqlid
	public static String DB_FREEMARKER = "120002"; // sql�ڵ�ִ��freemarker����args=sqlid
	public static String DB_MULTI_CHANGE_DS = "120003"; // ����л�����Դ��args=m,ds1,ds2

	public static String MSG_ERRS = "200000"; // ��������У�鲻ͨ��
	public static String ENCRYPT_ENCODE = "140001"; // ����ʧ��args=field
	public static String ENCRYPT_DECODE = "140002"; // ����ʧ��args=field
	public static String ENCRYPT_TRANSLATE = "140003"; // ת����ʧ��args=node1,node2��field
	public static String SIG_ENCODE = "140004"; // ǩ��ʧ��args=node
	public static String SIG_DECODE = "140005"; // ��ǩʧ��args=node
	public static String APPLY_PUBKEY = "140006"; // ����ESB��Կʧ��
	public static String RES_ALLOCATE_APPLY_FAIL = "140010"; // ��Դ���д�������Դ��������Դʧ��
	public static String RES_ALLOCATE_APPLY_NO = "140011"; // ��Դ���и���û�д�������Դ

	public static String CMM_BIZ_ERR = "300000"; // ͨ��ҵ�����
	public static String SERVICE_UNAUTH = "300001"; // ִ��δ��Ȩ����
	public static String NO_SN = "300002"; // û�н�����ˮ
	public static String REPEAT_SN = "300003"; // �����ظ���TCC
												// ԭ�ӽ�����ˮ���ظ�������ԭ�ӷ����ܷ���cancel
	public static String SERVICE_NOTEXIST = "300005"; // ���񲻴���
	
	public static String DB_UNAUTH = "309980"; // û��Ȩ�޷��ʴ�sql�ڵ�args=sqlid
	public static String SQL_INJECTION = "309981"; // sqlע����գ�args=sqlid,name
	
	public static String FILTER_UNVALID_URI = "309997"; // ���ʲ��Ϸ���uri��Դ,������û��¼
	public static String CMMN_UNLOGIN = "309990"; // δ��¼
	public static String CMMN_PWD_ERR = "309991"; // �������
	public static String UNAUTH_IP = "309992"; // ����ȨIP
	public static String FREQUENT_VISITS = "309993"; // Ƶ������
	public static String VERIFY_NOT_MATCH = "309994"; // ��֤�벻ƥ��
	public static String TOKEN_FAIL_VALIDATE = "309995"; // token��֤ʧ��
	public static String TOKEN_FAIL_USER = "309996"; // token�û�������

	// 5��ͷ����for tcc
	public static String TCC_XID_REPEAT = "500000"; // TCC �������ظ�
	public static String TCC_XID_NOEXISTS = "500001"; // TCC �����Ų�����
	public static String TCC_STATUS_CHANGE_FAIL = "500002"; // TCC ����״̬�ı�ʧ��
	public static String FAIL_SEQ_NO = "500010"; // ��ˮ��ʧ��
	public static String DS_RULE_NULL = "500020"; // δ����·�ɹ���
	public static String DS_ARG_NULL = "500021"; // ��̬����Դʱ���жϲ���Ϊ��
}
