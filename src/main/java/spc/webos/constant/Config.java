package spc.webos.constant;

/**
 * ����key
 * 
 * @author spc
 * 
 */
public class Config
{
	public final static String APP_WORKERID = "app.workerId";

	public final static String app_pdf_fontpath = "app.pdf.fontpath";

	// 701_20131001 ������־׷�ٵײ����緢�͵Ķ���������
//	public final static String TCP_TRACE = "app.trace.tcp";
	public final static String app_trace_tcp = "app.trace.tcp.";
	public final static String app_trace_mq = "app.trace.mq.";

	public static String app_login_default_services = "app.login.default.services"; // ��¼ʱĬ�Ϸ���Ȩ��
	public static String app_login_default_sqls = "app.login.default.sqls"; // ��¼ʱĬ��sqlȨ��
	public static String app_login_pwd_salt_len = "app.login.pwd.salt.len"; // �����γ���
	public static String app_login_verify = "app.login.verify"; // ��¼ʱ�Ƿ�����ṩ��֤��
	public static String app_login_verify_len = "app.login.verify.len"; // ��֤�볤��

	// �ǵ�¼ģʽ��������Ȩ����
	public static String app_web_auth_public_service = "app.web.auth.public.service"; // ������Ȩ����
	public static String app_web_auth_login_service = "app.web.auth.login.service"; // ��¼����Ȩ����
	public static String app_web_auth_public_sql = "app.web.auth.public.sql"; // ������ȨSQL
	public static String app_web_auth_login_sql = "app.web.auth.login.sql"; // ��¼����ȨSQL

	public static String app_mq_msg_expire = "app.mq.msg.expire"; // mq��Ϣ��ʧЧʱ�䣬��ֹ�յ�������Ϣ
	public static String app_mq_cb_expire = "app.mq.cb.expire"; // �ص�������cache���ʱ��

	public static String app_config_repeatInterval = "app.config.repeatInterval";

	public static String tcc_repeatSeconds = "tcc.repeatSeconds";
	public static String tcc_batchSize = "tcc.batchSize";

	// session
	// token Des key
	public static String app_web_session_token_des = "app.web.session.token.des";
	// ���session����Ծʱ��Ϊ��Сʱ
	public static String app_web_session_maxInactive = "app.web.session.maxInactive";
	public static String app_web_session_tps = "app.web.session.tps"; // ÿ10�����߷�����
	public static String app_web_cache_disable = "app.web.cache.disable"; // ǰ��ҳ�滺��ʧЧ
	public static String app_web_token_expire = "app.web.token.expire"; // tokenʧЧʱ����
}
