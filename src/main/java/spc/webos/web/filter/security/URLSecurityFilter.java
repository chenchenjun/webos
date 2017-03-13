package spc.webos.web.filter.security;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.constant.Web;
import spc.webos.exception.AppException;
import spc.webos.service.common.LoginService;
import spc.webos.service.seq.UUID;
import spc.webos.service.seq.impl.TimeMillisUUID;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.common.SUI;
import spc.webos.web.filter.AbstractURLFilter;
import spc.webos.web.util.WebUtil;
import spc.webos.web.view.ExceptionView;

public class URLSecurityFilter extends AbstractURLFilter
{
	protected AntPathBasedFilterDefinitionMap source = new AntPathBasedFilterDefinitionMap();
	protected AntPathBasedFilterDefinitionMap errPageSource = new AntPathBasedFilterDefinitionMap();
	@Value("${app.web.session.suiClazz?spc.webos.web.common.SessionUserInfo}")
	protected String suiClazz = "spc.webos.web.common.SessionUserInfo";
	@Value("${app.web.session.uri.logout?/js/login/logout}")
	protected String logoutUri = "/js/login/logout";
	@Value("${app.web.session.uri.login?/js/login/login}")
	protected String loginUri = "/js/login/login";
	@Value("${app.web.session.uri.create?/v}")
	protected String[] createUri = { "/v" }; // ��Ҫ����session��uri, ��֤��
	@Value("${app.web.session.err.status?555}")
	protected int jsonErrStatus = 555;

	@Value("${app.web.session.token.info?_token}")
	protected String tokenKey = "_token";
	@Value("${app.web.session.token.app?_token}")
	protected String tokenAppKey = "_tokenapp";

	// ��־׷����Ϣ��ʽ��0: userCode+":"+counts, mvc
	// 1��session id+counts,mvc:+userCode, 2: uuid
	@Value("${app.web.session.logMode?2}")
	protected int logMode = 2;
	@Autowired(required = false)
	protected UUID uuid;
	protected LoginService loginService;
	public final static String AUTH_ANONYMOUS = "public"; // ����Ȩ��
	public final static String MUST_LOGIN = "login";
	public final static String GET_SECRET = "sget"; // url�Ƿ��������ܵ�
	public final static String XSS = "xss";
	public final static String EX_ACTION_PAGE = "page";
	public final static String EX_ACTION_JSON = "json";
	public final static String EX_ACTION_REDIRECT = "redirect";

	public void filter(ServletRequest request, ServletResponse response, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String uri = getUri(req);
		// ������־׷����Ϣ
		boolean set = false;
		if (uuid != null) set = LogUtil.setTraceNo(SpringUtil.APPCODE + uuid.format(uuid.uuid()),
				LogUtil.shortUri(uri), false);
		SUI.SUI.set(null); // ��յ�ǰ�̵߳ĵĵ�¼��������ֹ�ϴ��̻߳����Ĳ���
		boolean login = loginUri.equals(uri);
		Map<String, String> params = StringX.uri2params(uri, 0);
		if (!StringX.nullity(params.get(Web.REQ_KEY_LOGOUT)))
		{
			log.info("logout session uri:{}", uri);
			logout(req, res);
		}
		String token = params.get(tokenKey); // put token in uri only
		String tokenApp = params.get(tokenAppKey); // put token app in uri
		boolean createSession = login || !StringX.nullity(token)
				|| StringX.contain(createUri, uri, true);

		SUI sui = createSUI(req, res, uri, login, createSession); // ��õ�ǰ�߳��û�������Ϣ
		if (sui != null)
		{ // ���״ε�½���޸ĵ�ǰsession��Ϣ
			sui.removeExpiredTransient(); // ɾ��������ʱ��Ϣ
			sui.setLastVisitDt(new Date());
			SUI.SUI.set(sui); // ���SessionUserInfo����ThreadLocal����ȥ
			// ��ֹƵ������
			int tps = config.getProperty(Config.app_web_session_tps, true, -1);
			if (tps > 0 && sui.tps() > tps)
				throw new AppException(AppRetCode.FREQUENT_VISITS, new Object[] { sui.tps() });
		}
		try
		{ // ��ȫ��鵱ǰ�û��Ƿ���Է���uri
			// �����ǰurl����token, ��check token
			if (!StringX.nullity(token) && loginService != null)
				loginService.token(tokenApp, token, false);
			security(req, response, chain, uri, sui, createSession);
			// ��ǰuri��ַҪ�˳�
			if (uri.equalsIgnoreCase(logoutUri)) logout(req, res);
			else saveSession(sui); // ����session
		}
		catch (Exception e)
		{
			errorPage(req, res, uri, e);
		}
		finally
		{
			if (set) LogUtil.removeTraceNo();
			SUI.SUI.set(null); // ��յ�ǰ�̵߳ĵĵ�¼��������ֹ�̻߳����Ĳ���
		}
	}

	protected void logout(HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			HttpSession session = req.getSession();
			log.info("logout session:{}", session != null ? session.getId() : "");
			if (session != null) session.invalidate();
		}
		catch (Exception e)
		{
		}
	}

	protected void security(ServletRequest request, ServletResponse response, FilterChain chain,
			String uri, SUI sui, boolean createSession) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String remoteHost = req.getRemoteHost();
		if (sui != null && !remoteHost.equals(sui.getLoginIP()))
		{ // ��ǰ�����IP���ǵ�¼IP
			log.info("{} is not login IP {}", remoteHost, sui.getLoginIP());
			errorPage(req, res, uri, new AppException(AppRetCode.UN_LOGIN_IP));
			return;
		}

		List<String> validRoles = source.lookupAttributes(uri); // ����ܺϷ������URI�Ľ�ɫ,����
		boolean xss = validRoles.contains(XSS);
		log.debug("validRoles:{}, by:{}, xss:{}", validRoles,
				(sui == null ? "" : sui.getUserCode()), xss);
		// ����Դ��Ҫ��ֹxss����
		if (xss) req = new XssHttpServletRequestWrapper(req);
		String method = req.getMethod().toLowerCase(); // ��ǰ��Դ�Ƿ����˺Ϸ�����ʽget/post
		if (!validRoles.contains(method))
		{
			log.info("method:{} unvalid for uri:{}", method, uri);
			errorPage(req, res, uri,
					new AppException(AppRetCode.URL_SECURIY, new Object[] { method }));
			return;
		}
		if (validRoles.contains(AUTH_ANONYMOUS) || createSession)
		{ // ���õ�½���ܷ��ʵ���Դ
			chain.doFilter(request, response);
			return;
		}
		if (sui == null || sui.isEmpty())
		{
			log.info("sui empty:{}", uri);
			errorPage(req, res, uri, new AppException(AppRetCode.UN_LOGIN, new Object[] { uri }));
			return;
		}
		if (validRoles.contains(GET_SECRET) && !WebUtil.isSGet(req.getQueryString()))
		{ // �Ƿ��Ƿ����������ɵļ��ܵ�get url
			log.info("not secret get queryString:{}", req.getQueryString());
			errorPage(req, res, uri, new AppException(AppRetCode.URL_SECURIY,
					new Object[] { req.getQueryString() }));
			return;
		}

		if (validRoles.contains(MUST_LOGIN))
		{ // ֻ��Ҫ��½���ܷ��ʵ���Դ
			chain.doFilter(req, response);
			return;
		}
		// �ж�Ȩ��, �Ƿ����������URL
		// 1. ���ǰ�õķǷ����ʵ���Դ�Ľ�ɫ
		List<String> unValidRoles = source.lookupAttributes('-' + uri); // ��ò��ܺϷ������URI�Ľ�ɫ,����
		if (unValidRoles.size() > 0)
		{
			int unValidTimes = 0;
			for (int i = 0; i < unValidRoles.size(); i++)
			{// ˵������Դ�ǵ�ǰ��ɫ���ܷ��ʵ�, ����һ���˿���ӵ�ж����ɫ, ֻ������ӵ�еĽ�ɫ�����ܷ��ʲ���˵�����ܷ���
				if (sui.containRole(unValidRoles.get(i))) unValidTimes++;
			}
			if (unValidTimes > 0)
			{ // ������ӵ�е����н�ɫ�����ܷ��ʴ�ģ��
				request.setAttribute(Web.RESP_ATTR_ERROR_KEY, Boolean.TRUE);
				log.info("no right for:{}", uri);
				errorPage(req, res, uri, new AppException(AppRetCode.URL_SECURIY));
				return;
			}
		}

		// 2. ��úϷ���ԴȨ��
		for (int i = 0; i < validRoles.size(); i++)
		{
			if (sui.containRole(validRoles.get(i)))
			{ // ����Դ��Ȩ�ޱ�����ӵ��, ��Ϸ�ͨ��
				chain.doFilter(req, response);
				return;
			}
		}
		// û��ͨ���� 2 ������Դ����Ϸ����. �ض�λ����½ҳ��
		log.info("no right to visit {}", uri);
		request.setAttribute(Web.RESP_ATTR_ERROR_KEY, Boolean.TRUE);
		errorPage(req, res, uri, new AppException(AppRetCode.URL_SECURIY));
	}

	protected SUI createSUI(HttpServletRequest req, HttpServletResponse res, String uri,
			boolean loginUri, boolean createSession)
	{
		SUI sui = null;
		HttpSession session = req.getSession(false); // ֻ�е�¼url���ܴ���session
		if (createSession || loginUri)
		{ // ��ǰuri��Ҫ����session, ������ڲ�����������������򴴽�
			if (session != null)
			{ // ����ǵ�ǰsession��Ҫ���µ�¼������ɾ����ǰsession�� ֻ��Ҫ���sui��userCode��Ϣ����
				sui = (SUI) session.getAttribute(SUI.USER_SESSION_INFO_KEY);
				if (sui != null && loginUri) sui.clear(); // �����ǰ��Ϊ��Ҫ��¼����ֱ�����sui��Ϣ
				log.info("session:{}, sui exists:{}, loginUri:{}", session.getId(), sui != null,
						loginUri);
			}
			else
			{ // û��session�򴴽�
				session = req.getSession(true);
				int maxInactiveInterval = config.getProperty(Config.app_web_session_maxInactive,
						false, 1800);
				session.setMaxInactiveInterval(maxInactiveInterval); // session�Ự���ʱ��
				try
				{
					sui = newSUI(req, sui);
					session.setAttribute(SUI.USER_SESSION_INFO_KEY, sui);
					log.info("login create session:{}, max:{}, uri:{}", session.getId(),
							maxInactiveInterval, uri);
				}
				catch (Exception e)
				{
					log.error("create user session fail:: uri: " + uri + ", remote: "
							+ req.getRemoteAddr(), e);
				}
			}
		}
		if (session != null) sui = (SUI) session.getAttribute(SUI.USER_SESSION_INFO_KEY);
		log(req, res, sui, uri); // ��־��ӡ��ǰ��¼��Ϣ
		if (sui != null) sui.request(req, res, session.getId(), session); // �Ǽ�һ�ε�¼��Ϣ
		return sui;
	}

	protected void saveSession(SUI sui) throws Exception
	{
	}

	protected SUI newSUI(HttpServletRequest req, SUI sui)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		log.info("login sui clazz:{}, maxInactiveInterval:{}, exists:{}", suiClazz,
				config.getProperty(Config.app_web_session_maxInactive, false, 1800), sui != null);
		if (sui == null) sui = (SUI) (Class.forName(suiClazz, true,
				Thread.currentThread().getContextClassLoader())).newInstance();
		sui.setLoginDt(new Date()); // ��¼ʱ��
		sui.setLoginIP(req.getRemoteHost()); // ��¼�ͻ���IP
		return sui;
	}

	protected void log(HttpServletRequest req, HttpServletResponse res, SUI sui, String uri)
	{
		String method = req.getMethod();
		String remoteHost = req.getRemoteHost();
		String user = sui == null ? "" : StringX.null2emptystr(sui.getUserCode());
		if ("GET".equalsIgnoreCase(method)) log.info("{} {} rc:{}_{}, {},{} {}?{}", user,
				remoteHost, (sui == null ? -1 : sui.requestCount()), (sui == null ? 0 : sui.tps()),
				req.getContentType(), method, uri, StringX.null2emptystr(req.getQueryString()));
		else log.info("{} {} rc:{}_{}, {} {},{}, len:{}", user, remoteHost,
				(sui == null ? -1 : sui.requestCount()), (sui == null ? 0 : sui.tps()),
				req.getContentType(), method, uri, req.getContentLength());
	}

	protected void errorPage(HttpServletRequest req, HttpServletResponse res, String uri,
			Exception ex) throws IOException
	{
		res.setStatus(500);
		boolean login = !((ex instanceof AppException)
				&& AppRetCode.UN_LOGIN.equalsIgnoreCase(((AppException) ex).getCode()));
		String redirect = req.getContextPath();
		List<String> handler = errPageSource.lookupAttributes(uri);
		log.info("err page:{}, uri:{}, login:{}, ex:{}", handler, uri, login, ex.toString());
		String action = handler != null && !handler.isEmpty() ? handler.get(0) : "";
		if (handler == null || handler.size() == 0) redirect = req.getContextPath();
		else if (action.startsWith("json"))
		{ // json Ӧ��
			Map model = new HashMap();
			model.put(Common.MODEL_EXCEPTION, ex);
			// extjs ds��ʽ����Ӧ��ʱstatus������2xx����״̬, ����json��ʽ����Ӧ��ʱstatus������5xx����״̬
			new ExceptionView(handler.get(1), Common.FILE_JSON_CONTENTTYPE,
					!StringX.nullity(req.getParameter("_extds")) ? 222 : jsonErrStatus)
							.render(model, req, res);
			return;
		}
		else if (login && EX_ACTION_PAGE.equalsIgnoreCase(action))
		{ // err page
			Map model = new HashMap();
			model.put(Common.MODEL_EXCEPTION, ex);
			new ExceptionView(handler.get(1), Common.FILE_HTML_CONTENTTYPE, 0).render(model, req,
					res);
			return;
		}
		else if (EX_ACTION_REDIRECT.equalsIgnoreCase(action)) redirect = handler.get(1);
		log.info("redirect to {}", redirect);
		res.sendRedirect(redirect);
	}

	/**
	 * ������õ�Url��Դ��Ӧ��Ȩ�� or ��ɫ or ����ģ����Ϣ
	 * 
	 * @param s
	 */
	public void setDefinitionSource(String s)
	{
		setDefinitionMap(source, s);
	}

	public void setSuiClazz(String suiClazz)
	{
		this.suiClazz = suiClazz;
	}

	public void setLoginUri(String loginUri)
	{
		this.loginUri = loginUri;
	}

	public void setLogMode(int logMode)
	{
		this.logMode = logMode;
	}

	public void setTokenKey(String tokenKey)
	{
		this.tokenKey = tokenKey;
	}

	public void setTokenAppKey(String tokenAppKey)
	{
		this.tokenAppKey = tokenAppKey;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public void setUuidWorkId(int workId)
	{
		this.uuid = new TimeMillisUUID(workId);
	}

	public void setErrPageSource(String s)
	{
		setDefinitionMap(errPageSource, s);
	}

	public void setLogoutUri(String logoutUri)
	{
		this.logoutUri = logoutUri;
	}

	public void setJsonErrStatus(int errStatus)
	{
		this.jsonErrStatus = errStatus;
	}

	public void setCreateUri(String[] createUri)
	{
		this.createUri = createUri;
	}

	public void setLoginService(LoginService loginService)
	{
		this.loginService = loginService;
	}
}

class XssHttpServletRequestWrapper extends HttpServletRequestWrapper
{
	public XssHttpServletRequestWrapper(HttpServletRequest servletRequest)
	{
		super(servletRequest);
	}

	public String[] getParameterValues(String parameter)
	{
		String[] values = super.getParameterValues(parameter);
		if (values == null) return null;
		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++)
			encodedValues[i] = StringEscapeUtils.escapeHtml4(values[i]);
		return encodedValues;
	}

	public String getParameter(String parameter)
	{
		String value = super.getParameter(parameter);
		if (value == null) return null;
		return StringEscapeUtils.escapeHtml4(value);
	}

	public String getHeader(String name)
	{
		String value = super.getHeader(name);
		if (value == null) return null;
		return StringEscapeUtils.escapeHtml4(value);
	}
}
