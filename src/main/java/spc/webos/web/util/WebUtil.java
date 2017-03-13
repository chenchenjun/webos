package spc.webos.web.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.ThreadLocalRandom;
import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.exception.AppException;
import spc.webos.util.FTLUtil;
import spc.webos.util.FileUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.MethodUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.common.SUI;

public class WebUtil
{
	static Logger log = LoggerFactory.getLogger(WebUtil.class);
	public final static ThreadLocal<Boolean> WEB = new ThreadLocal<>();
	static List<String> DEF_PUBLIC_SERVICE = Arrays.asList("login.*", "extjs.*",
			"persistence.query*");
	static List<String> DEF_PUBLIC_SQL = new ArrayList<>(); // Arrays.asList("class_spc_*");
	static List<String> DEF_LOGIN_SERVICE = new ArrayList<>();
	static List<String> DEF_LOGIN_SQL = new ArrayList<>();

	public static boolean isWeb()
	{ // ��ǰ�߳��Ƿ���Web����
		return WEB.get() != null && WEB.get();
	}

	public static boolean isAuth(List<String> auth, String s)
	{ // �Ƿ���Ȩ��Դ:service & sqlId
		s = s.toLowerCase();
		for (String ss : auth)
		{
			if (ss.equals(s)
					|| (ss.endsWith("*") && s.startsWith(ss.substring(0, ss.length() - 1))))
				return true;
		}
		return false;
	}

	public static boolean containSqlId(String sqlId, Map<String, Object> param)
	{
		if (!isWeb()) return true; // 1. ��Web����������SQL����
		// 2. �Ƿ����ڹ����ɷ���SQL
		if (isAuth(AppConfig.getInstance().getProperty(Config.app_web_auth_public_sql, false,
				DEF_PUBLIC_SQL), sqlId))
			return true;
		// 3. �Ƿ����ڵ�ǰ�û���ȨSQL
		SUI sui = SUI.SUI.get();
		if (sui != null)
		{
			if (isAuth(AppConfig.getInstance().getProperty(Config.app_web_auth_login_sql, false,
					DEF_LOGIN_SQL), sqlId))
				return true;
			return sui.containSqlId(sqlId, param);
		}
		return false;
	}

	public static void containService(String s, String m, int argNum)
	{
		String service = s + '.' + m + '$' + argNum;
		List<String> publicService = AppConfig.getInstance()
				.getProperty(Config.app_web_auth_public_service, false, DEF_PUBLIC_SERVICE);
		if (isAuth(publicService, service)) return;
		SUI sui = SUI.SUI.get();
		if (sui != null)
		{ // ����Ѿ���¼��������鵱ǰ��½��ִ�з����Ȩ��
			log.info("unauthorized service:({}.{}), user:{}", s, m, sui.getUserCode());
			boolean auth = false;
			if (isAuth(AppConfig.getInstance().getProperty(Config.app_web_auth_login_service, false,
					DEF_LOGIN_SERVICE), service))
				auth = true;
			if (!auth) auth = sui.containService(service);
			if (!auth) throw new AppException(AppRetCode.SERVICE_UNAUTH, new Object[] { service });
		}
	}

	public static String generateSessionId(int sessionLen)
	{
		byte[] id = new byte[sessionLen];
		ThreadLocalRandom.current().nextBytes(id);
		return SpringUtil.APPCODE + SpringUtil.JVM + "." + StringX.md5(id);
	}

	/**
	 * ֧��ǰ�˷���json��ʽ���ݣ���http header������json��ʽ
	 * 
	 * @param req
	 * @param params
	 * @return
	 */
	public static Map<String, Object> request2map(HttpServletRequest req,
			Map<String, Object> params) throws Exception
	{
		params = FTLUtil.model(params);
		String contentType = req.getContentType();
		if (!StringX.nullity(contentType) && contentType.indexOf("json") >= 0)
		{
			String json = new String(FileUtil.is2bytes(req.getInputStream(), false),
					Common.CHARSET_UTF8);
			if (!StringX.nullity(json)) params.putAll((Map) JsonUtil.json2obj(json));
		}
		else
		{
			Enumeration names = req.getParameterNames();
			while (names.hasMoreElements())
			{
				String paramName = names.nextElement().toString();
				String value = req.getParameter(paramName);
				if (value != null && value.length() > 0)
					params.put(paramName, StringX.utf82str(value));
			}
		}

		params.put(Common.MODEL_REQUEST_KEY, req);
		params.put(Common.MODEL_APP_PATH_KEY, req.getContextPath());
		return params;
	}

	// �������еõ����и����Ĳ�����
	public static List<String> getUploadFileNames(HttpServletRequest req)
	{
		List<String> names = new ArrayList<>();
		Enumeration enu = req.getParameterNames();
		while (enu.hasMoreElements())
		{
			String name = (String) enu.nextElement();
			if (name.startsWith("file.")) names.add(name);
		}
		return names;
	}

	// ִ��һ��json������ʽ�ķ�����������BATCH_SQL��ѯ�����ظ�page or ָ��view������model
	// ִ�еķ��������S.��ͷ
	public static void invokeJsonService(HttpServletRequest req, Map params, String servicePostfix)
			throws Exception
	{
		for (Object k : params.keySet())
		{
			String name = k.toString();
			if (!name.startsWith("S.")) continue;
			int idx = name.lastIndexOf('.');
			if (idx < 2) continue;
			String s = name.substring(2, idx);
			String m = name.substring(idx + 1);
			String p = (String) params.get(name);
			log.info("S. jscall:{}.{}", s, m);
			log.debug("args:{}", p);
			// �Զ����Service��׺
			Object args = StringX.nullity(p) ? null : JsonUtil.json2obj(p);
			int argNum = getMethodArgNum(s, m, args);
			Object ret = SpringUtil.jsonCall(s + servicePostfix, m, args, argNum);
			if (ret != null) params.put(s + '_' + m, ret);
		}
	}

	public static int getMethodArgNum(String s, String m, Object requestArgs)
	{
		int argNum = -1;
		if (requestArgs == null) argNum = 0;
		else if (requestArgs instanceof List) argNum = ((List) requestArgs).size();
		else
		{ // restful style
			int idx = m.indexOf('$');
			if (idx > 0)
			{
				argNum = Integer.parseInt(m.substring(idx + 1));
				m = m.substring(0, idx);
			}
			else
			{
				Method me = MethodUtil
						.findMethod(SpringUtil.getInstance().getBean(s, null).getClass(), m, -1);
				argNum = me.getParameterCount();
			}
		}
		containService(s, m, argNum);
		return argNum;
	}

	public static final String sgetSalt = SpringUtil.random(32); // ����sget���Ӱ�ȫ��

	public static String sget(String queryString)
	{
		SUI sui = SUI.SUI.get();
		if (sui != null) return sui.sget(queryString);
		return URLEncoder.sget(queryString, sgetSalt);
	}

	public static boolean isSGet(String queryString)
	{
		SUI sui = SUI.SUI.get();
		if (sui != null) return sui.isSGet(queryString);
		return URLEncoder.isSGet(queryString, sgetSalt);
	}
}
