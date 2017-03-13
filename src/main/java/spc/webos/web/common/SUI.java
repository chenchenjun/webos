package spc.webos.web.common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import spc.webos.web.filter.multipart.MultipartEntry;

/**
 * @author spc
 */
public interface SUI
{
	void clear(); // �����ǰ�û���Ϣ

	boolean isEmpty(); // �Ƿ�Ϊ����Ϣ

	int tps();

	String getSessionId();

	void removeInPersist(String key);

	void setInPersist(String key, Serializable value);

	<T> T getInPersist(String key, T def);

	void removeInTransient(String key);

	// void setInTransient(String key, TransientInSession<?> tis);

	<T> void setInTransient(String key, T value, int cacheSeconds);

	<T> T getInTransient(String key, T def);

	// ����һ��request���������ڲ������� Ҳ����ͳ�Ƶ�ǰ�û����������
	void request(HttpServletRequest request, HttpServletResponse response, String sessionId,
			HttpSession session);

	int requestCount(); // ��õ�ǰ�������

	// �ϴ��ļ�
	Map<String, List<MultipartEntry>> upload();

	/**
	 * �ѵ�ǰsessionID���浽SessionUserInfo�У���web remote, web service�е�Ȩ�޼��ʱʹ�ô����ԣ�
	 * ��Ϊͨ���ͻ��˵���login(user,pwd)�󷵻�һ��sessionID���Ժ��http���ûỰʹ�á�
	 * 
	 */
	// 2016-12-12 ����������Աֱ�Ӳ���session
	HttpSession session();

	HttpServletRequest request();

	HttpServletResponse response();

	void setVerifyCode(String verifyCode);

	String getVerifyCode();

	// �û������Ϣ
	void setLoginIP(String loginIP);

	String getLoginIP(); // ��½��IP

	Date getLastVisitDt(); // ��ȡ��һ�η��ʷ�����ʱ��

	void setLastVisitDt(Date lastVisitDt);

	void setLoginDt(Date loginDt);

	Date getLoginDt(); // ��½ʱ��

	/**
	 * ����û�����
	 * 
	 * @return
	 */
	String getUserCode();

	// ��ǰ�û��Ƿ�session�û�
	boolean rejectUserCode(String userCode);

	/**
	 * ����û�����
	 * 
	 * @return
	 */
	String getUserName();

	// ��ǰ�û��Ƿ�session�û�
	boolean rejectUserName(String userName);

	/**
	 * ֧��URLSecurityFilter, �û�����Ȩ����Ϣ
	 * 
	 * @return
	 */
	boolean containRole(String role);

	List<String> getRoles();

	// �Ƿ���ִ�д�sqlid��Ȩ��
	boolean containSqlId(String sqlId, Map<String, Object> param);

	// �Ƿ���ִ�д˷����Ȩ��
	boolean containService(String s);

	/**
	 * ɾ��Session����ʱ�洢��Ϣ�Ľӿ�, ��URLSecurityFilter�е���
	 */
	void removeExpiredTransient();

	/**
	 * GetMethodSecurityFilter����, �û���õ�ǰSession�еĶ�URL���ܵ�Key. ÿ����½���û����һ�������Key
	 * 
	 * @return
	 */
	String sget(String queryString);

	boolean isSGet(String queryString);

	/**
	 * һ��TheadLocal�����������SessionUserInfo
	 */
	final static ThreadLocal<SUI> SUI = new ThreadLocal<SUI>();

	/**
	 * ��Session�д���û���Ϣ������
	 */
	final static String USER_SESSION_INFO_KEY = "_SUI_";

	// final static String ALL_ONLINE_USER_KEY = "_ALL_ONLINE_USER_KEY_";
}
