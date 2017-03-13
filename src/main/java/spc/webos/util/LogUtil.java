package spc.webos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ������־Ψһ��ˮ��׷��. �޸�ch.qos.logback.classic.spi.LoggingEvent,
 * ����threadlocal��Ϣ�����ڴ��Ψһ���ٺţ��ο�eslf4j��Ŀ
 * 
 * @author chenjs
 *
 */
public class LogUtil
{
	protected static Logger log = LoggerFactory.getLogger(LogUtil.class);
	public static String MDC_KEY_TRACE = "trace"; // ��־������Ϣ
	public static String MDC_KEY_LOCATION = "location"; // ������־���ٵĸ���λ����Ϣ
	public static String MDC_APP_CD = "appCd"; // �����ϵͳ���
	static ThreadLocal<String> LOG_TRACE_NO = new ThreadLocal<String>();
	static ThreadLocal<String> LOG_LOCATION = new ThreadLocal<String>();
	static ThreadLocal<String> LOG_APP_CD = new ThreadLocal<String>();
	public static int MAX_LOCATION_LEN = 20;

	public static String shortUri(String uri)
	{
		if (uri.length() <= MAX_LOCATION_LEN) return uri;
		return uri.substring(0, MAX_LOCATION_LEN) + "..";
	}

	public static String getTraceNo()
	{
		return LOG_TRACE_NO.get();
	}

	public static String getAppCd()
	{
		return LOG_APP_CD.get();
	}

	public static void setAppCd(String appCd)
	{
		LOG_APP_CD.set(appCd);
	}

	/**
	 * 
	 * @param traceNo
	 *            ��־���ٺ�
	 * @param replace
	 *            �����ǰ�����Ѿ������Ƿ񸲸�
	 * @return
	 */
	public static boolean setTraceNo(String traceNo, String location, boolean replace)
	{
		if (traceNo == null || traceNo.length() == 0) return false;
		// ����Ѿ�����traceNo, �򲻸���
		if (!replace && getTraceNo() != null) return false;

		// ���򸲸�
		LOG_TRACE_NO.set(traceNo);
		LOG_LOCATION.set(location);

		// ����Log MDC ����
		putMDC(MDC_KEY_TRACE, traceNo);
		putMDC(MDC_KEY_LOCATION, location);

		log.debug("set trace:{}", traceNo);
		return true;
	}

	public static void putMDC(String key, String value)
	{
		org.slf4j.MDC.put(key, value);
	}

	public static void removeTraceNo()
	{
		log.debug("remove trace:{}", getTraceNo());
		LOG_TRACE_NO.set(null);
		LOG_LOCATION.set(null);

		// ȡ��Log MDC ����
		removeMDC(MDC_KEY_TRACE);
		removeMDC(MDC_KEY_LOCATION);
	}

	public static void removeMDC(String key)
	{
		org.slf4j.MDC.remove(key);
	}
}
