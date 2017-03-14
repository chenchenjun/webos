package spc.webos.util;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.google.gson.Gson;

import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.exception.AppException;
import spc.webos.exception.Status;
import spc.webos.message.AppMessageSource;
import spc.webos.util.netinfo.NetworkInfo;

/**
 * ϵͳ������
 * 
 * @author spc
 * 
 */
public final class SpringUtil implements ResourceLoaderAware, ApplicationContextAware
{
	public static ApplicationContext APPCXT; // ��ǰspring����
	public static String LOCAL_HOST_IP = "127.0.0.1"; // ����ip��ַ
	public static String MAC = null; // ��ǰ������mac��ַ
	public static String RETCD_PATH = "RETCD/"; // ��������Ϣ·��, 900 ��MSG �޸�ΪRETCD
	public static String DICT_PATH = "DICT/"; // �����ֵ���Ϣ·��
	public final static Date JVM_START_TIME = new Date(); // ��ǰjvm����������ʱ��
	public static String DATA_DIR; // ��ʱ�ļ���

	public static volatile String PID; // ��ǰ���̺�
	// public static String FTS_AGENT; // �������FTS�ļ�����ڵ�����
	public static String APPCODE = "ESB";// ϵͳӦ��ID,���������ڵ�ϵͳ���, ���ܳ���3���ַ�
	public static String JVM = "SPC"; // jvm��ʾ���е�����������൱��ģ��, ���ܳ���3���ַ�
	public static String JVMDESC = "sturdypine@icloud.com"; // JVM����
	// public static String NODE; // �ڵ�� = "0000", modified by spc

	final static Logger log = LoggerFactory.getLogger(SpringUtil.class);

	@PostConstruct
	public void init() throws Exception
	{
		if (StringX.nullity(DATA_DIR)) DATA_DIR = "classpath:data";
		log.info(
				"  #---  webos:{}, app:{}/{}, jvm:{}, ip:{}, pid:{}, data.dir:{}, user.dir:{}   ---#",
				Common.VER(), APPCODE, JVM, System.getProperty(Config.APP_WORKERID),
				getLocalHostIP(), pid(), getDataDir().getAbsolutePath(),
				System.getProperty("user.dir"));
	}

	public static String pid()
	{
		if (PID != null) return PID;
		String name = ManagementFactory.getRuntimeMXBean().getName();
		return PID = name.split("@")[0];
	}

	public static String random(int num)
	{
		StringBuilder str = new StringBuilder();
		str.append(String.valueOf(Math.random()).substring(2)
				+ String.valueOf(Math.random()).substring(2)
				+ String.valueOf(Math.random()).substring(2));
		return str.substring(0, num);
	}

	public static String getLocalHostIP()
	{
		return LOCAL_HOST_IP;
	}

	public void setApplicationContext(ApplicationContext appCxt)
	{
		APPCXT = appCxt;
	}

	public static String getMessage(String code, String defaultMessage)
	{
		return getMessage(code, null, defaultMessage, null);
	}

	public static String getMessage(String code, Object[] args, String defaultMessage)
	{
		return getMessage(code, args, defaultMessage, null);
	}

	public static String getMessage(String code, List args, String defaultMessage)
	{
		return getMessage(code, (args != null ? args.toArray() : null), defaultMessage, null);
	}

	public static String getMessage(String code, Object[] args, Locale locale)
	{
		return getMessage(code, args, null, locale);
	}

	public static String getMessage(String code, Object[] args, String def, Locale locale)
	{
		if (StringX.nullity(def)) def = StringX.getMessageFormat(args == null ? 0 : args.length);
		try
		{
			return APPCXT.getMessage(code, args, def, locale);
		}
		catch (Exception e)
		{
			log.warn("Fail to get error desc for " + code, e);
		}
		return new MessageFormat(def).format(args);
	}

	public static String getMessage(String path, String code, Object[] args, Locale locale)
	{
		String sep = StringX.COMMA;
		if (code.indexOf(sep) < 0)
			return APPCXT.getMessage(path + AppMessageSource.SEPARATOR + code, args, locale);
		// ���ڶ�ѡ
		StringBuffer buf = new StringBuffer();
		String[] ccode = StringX.split(code, sep);
		for (int i = 0; i < ccode.length; i++)
		{
			if (buf.length() > 0) buf.append(sep);
			buf.append(
					APPCXT.getMessage(path + AppMessageSource.SEPARATOR + ccode[i], args, locale));
		}
		// System.out.println(buf);
		return buf.toString();
	}

	/**
	 * ͨ���쳣����һ����Ϣ״̬
	 * 
	 * @param prefix
	 *            ��Ϣ����ǰ׺
	 * @param ex
	 *            �쳣
	 * @return
	 */
	public static Status ex2status(String prefix, Throwable ex)
	{
		Status status = new Status();
		// if (!StringX.nullity(NODE)) status.setMbrCd(NODE);
		status.setAppCd(APPCODE);
		status.setIp(SpringUtil.LOCAL_HOST_IP);
		String retCd = StringX.EMPTY_STRING;
		if (ex instanceof AppException)
		{ // ϵͳ����֪�쳣
			AppException appEx = (AppException) ex;
			if (log.isDebugEnabled())
			{
				Object[] args = appEx.getArgs();
				StringBuffer buf = new StringBuffer();
				for (int i = 0; args != null && i < args.length; i++)
					buf.append(args[i] + ",");
				log.debug("err,args:" + buf, ex);
			}
			if (appEx.getDetail() != null && appEx.getDetail() instanceof Status)
			{ // �쳣���Ѿ����б�׼���쳣��Ϣ
				log.debug("appEx status: {}", appEx.getDetail());
				return (Status) appEx.getDetail();
			}
			retCd = appEx.getCode();
			// status.setRetCd(appEx.getCode());
			if (StringX.nullity(appEx.getCode()) || AppRetCode.CMM_BIZ_ERR.equals(appEx.getCode()))
			{ // ���������ͨ��ҵ�������ʹ��Ĭ����Ϣ
				retCd = AppRetCode.CMM_BIZ_ERR;
				if (StringX.nullity(appEx.getDesc()) && appEx.getArgs() != null)
					appEx.setDesc(StringX.join(appEx.getArgs(), " "));
				// status.setRetCd(AppRetCode.CMM_BIZ_ERR);
				// status.setDesc(appEx.getDefErrMsg());
			}
			else if (StringX.nullity(appEx.getMsgFormat()))
			{ // ����쳣û������Ϣ����ģ���ʽʹ�ø��ݷ����뵽���ݿ���Ѱ�Ҹ�ʽ
				status.setDesc(prefix + getMessage(SpringUtil.RETCD_PATH + retCd, appEx.getArgs(),
						appEx.getDesc(), null));
			}
			status.setLocation(JVM + "::" + appEx.getLocation());
		}
		else
		{ // δ֪�쳣
			log.warn("undef ex, prefix:" + prefix, ex);
			retCd = (ex instanceof NullPointerException) ? AppRetCode.CMMN_NULLPOINT_EX
					: AppRetCode.CMMN_UNDEF_EX;
			String desc = ex.toString() + StringX.stackTrace(ex.getStackTrace(), 5);
			if (desc.length() > 200) desc = desc.substring(0, 200);
			if (desc.startsWith("java.lang.Exception")) desc = desc.substring(20);
			if (StringX.isContainCN(desc)) retCd = AppRetCode.CMM_BIZ_ERR; // ����쳣�к�������,����Ϊ��ͨ��ҵ�����
			status.setDesc(prefix + desc);
			status.setLocation(JVM + "::" + AppException.getLocation(ex.getStackTrace()));
		}
		status.setRetCd(retCd);
		if (StringX.nullity(status.traceNo))
			status.traceNo = SpringUtil.APPCODE + LogUtil.getTraceNo();
		return status;
	}

	// ����spring����
	public static synchronized ApplicationContext load(String... files) throws Exception
	{
		if (APPCXT != null)
		{
			log.info("spring context has been loaded...");
			return APPCXT;
		}
		log.info("start to load spring context in classpath:" + Thread.currentThread()
				.getContextClassLoader().getResource(StringX.EMPTY_STRING).getFile());
		APPCXT = new ClassPathXmlApplicationContext(files);
		Runtime.getRuntime().addShutdownHook(hook = new Thread()
		{
			public void run()
			{
				log.warn("ShutdownHook: jvm will halt...");
				if (SpringUtil.getInstance().APPCXT == null) return;
				try
				{
					SpringUtil.unload();
				}
				catch (Exception e)
				{
					log.warn("unload context for jvm halt...", e);
				}
			}
		});
		return APPCXT;
	}

	static Thread hook;

	// ж��spring����
	public static synchronized void unload() throws Exception
	{
		if (APPCXT == null)
		{
			log.debug("app spring context is null!!!");
			return;
		}

		log.warn(FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + " JVM("
				+ SpringUtil.JVM + ") App(" + SpringUtil.APPCODE + ") spring context unload...");
		try
		{
			APPCXT.publishEvent(new ContextStoppedEvent(APPCXT));
		}
		finally
		{
			APPCXT = null;
		}
	}

	// ����jvm��Ϣ
	public static Map<String, Object> jvm() throws Exception
	{
		Map<String, Object> jvm = new HashMap<>();
		jvm.put("app", SpringUtil.APPCODE);
		jvm.put("jvm", SpringUtil.JVM);
		jvm.put("pid", pid());
		jvm.put("ip", SpringUtil.LOCAL_HOST_IP);
		jvm.put("webos", Common.VER());
		Map<String, Object> cfg = new HashMap<>();
		AppConfig.getInstance().getStaticCfg().forEach((k, v) -> {
			if (k.toString().indexOf("jdbc") < 0) cfg.put(k.toString(), v);
		});
		jvm.put("cfg", JsonUtil.obj2json(cfg));
		jvm.put("dataDir", getDataDir().getAbsolutePath());
		jvm.put("bizjar", System.getProperty("bizjar"));
		jvm.put("cpu", Runtime.getRuntime().availableProcessors());
		jvm.put("maxMemory", Runtime.getRuntime().maxMemory());
		jvm.put("startDt", FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
				.format(SpringUtil.JVM_START_TIME));
		return jvm;
	}

	public static void debugJvm()
	{
		log.info("Booting JVM...webos version:" + Common.VER() + ", webapp.root: "
				+ System.getProperty(Common.WEBAPP_ROOT_PATH_KEY));
		if (log.isDebugEnabled())
		{
			Iterator keys = System.getProperties().keySet().iterator();
			while (keys.hasNext())
			{
				String key = keys.next().toString();
				if (key.startsWith("java"))
					log.debug(key + ":" + System.getProperties().getProperty(key));
			}
		}
	}

	public static void invoke(String... beanMethods)
	{
		log.debug("start to invoke: {}", beanMethods);
		for (int i = 0; i < beanMethods.length; i++)
		{
			int index = beanMethods[i].indexOf('.');
			String beanId = beanMethods[i].substring(0, index);
			Object target = SpringUtil.getInstance().getBean(beanId, null);
			if (target == null) continue;
			Method method = findMethod(beanMethods[i]);
			try
			{
				log.info("invoke:{}", beanMethods[i]);
				method.invoke(target, null);
			}
			catch (Exception e)
			{
				log.warn("fail to invoke: " + beanMethods[i], e);
			}
		}
	}

	public static Method findMethod(String beanMethod)
	{
		int index = beanMethod.indexOf('.');
		String beanId = beanMethod.substring(0, index);
		return MethodUtil.findMethod(SpringUtil.getInstance().getBean(beanId, null),
				beanMethod.substring(index + 1), 0);
	}

	public static SpringUtil getInstance()
	{
		return SYSTEM_UTIL;
	}

	public static String SPRING_BEAN_PREFIX = "<beans xmlns=\"http://www.springframework.org/schema/beans\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:p=\"http://www.springframework.org/schema/p\" "
			+ "xmlns:context=\"http://www.springframework.org/schema/context\" xmlns:util=\"http://www.springframework.org/schema/util\" "
			+ "xmlns:amq=\"http://activemq.apache.org/schema/core\" xmlns:jms=\"http://www.springframework.org/schema/jms\" "
			+ "xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd "
			+ "http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd "
			+ "http://www.springframework.org/schema/jms http://www.springframework.org/schema/jms/spring-jms-4.0.xsd "
			+ "http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd "
			+ "http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd\">";

	public static void registerXMLBean(String beanXML, boolean segment) throws Exception
	{
		if (StringX.nullity(beanXML)) return;
		XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(
				(BeanDefinitionRegistry) ((ConfigurableApplicationContext) APPCXT)
						.getBeanFactory());
		xbdr.setResourceLoader(APPCXT);
		xbdr.setEntityResolver(new ResourceEntityResolver(APPCXT));
		if (segment) beanXML = SPRING_BEAN_PREFIX + beanXML + "</beans>";
		xbdr.loadBeanDefinitions(new ByteArrayResource(beanXML.getBytes(Common.CHARSET_UTF8)));
		log.debug("register segment:{}, xml:{}", segment, beanXML);
	}

	// ���̬��
	public static void activeRegisterBeans(String... beans)
	{
		if (beans == null)
			beans = ((ConfigurableApplicationContext) APPCXT).getBeanDefinitionNames();
		for (int i = 0; i < beans.length; i++)
			APPCXT.getBean(beans[i]);
	}

	public static String relativePath2AbsolutePath(String dir) throws Exception
	{
		ResourceLoader resourceLoader = SYSTEM_UTIL.getResourceLoader();
		if (resourceLoader != null) return resourceLoader.getResource(StringX.null2emptystr(dir))
				.getFile().getAbsolutePath();
		return new File(Thread.currentThread().getContextClassLoader().getResource(dir).toURI())
				.getAbsolutePath();
	}

	protected Map springBeanCache = new Hashtable(200, 0.6f);

	public Object getBean(String beanId)
	{
		return APPCXT.getBean(beanId);
	}

	public Object getBean(String beanId, Class clazz)
	{
		if (StringX.nullity(beanId)) return null;
		Object obj = springBeanCache.get(beanId);
		if (obj != null && (clazz == null || obj.getClass() == clazz)) return obj;
		if (APPCXT == null)
		{
			log.error("appCxt is null!!!");
			return null;
		}
		try
		{
			obj = clazz == null ? APPCXT.getBean(beanId) : APPCXT.getBean(beanId, clazz);
		}
		catch (NoSuchBeanDefinitionException nsbde)
		{
			if (log.isDebugEnabled())
				log.debug("cannot load bean in spring cxt for bean id: " + beanId
						+ (clazz != null ? ", class: " + clazz.getName() : StringX.EMPTY_STRING));
		}
		if (obj != null) springBeanCache.put(beanId, obj);
		return obj;
	}

	public void setResourceLoader(ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

	protected ResourceLoader resourceLoader;
	final static SpringUtil SYSTEM_UTIL = new SpringUtil();

	public ResourceLoader getResourceLoader()
	{
		return resourceLoader;
	}

	static
	{
		try
		{
			LOCAL_HOST_IP = NetworkInfo.getLocalHost();
			if (LOCAL_HOST_IP == null) LOCAL_HOST_IP = StringX.EMPTY_STRING;
			// ��ֹIP��ַ����
			if (LOCAL_HOST_IP.length() > 15) LOCAL_HOST_IP = LOCAL_HOST_IP.substring(0, 15);
			// MAC = NetworkInfo.getMacAddress();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setIp(String localhost)
	{
		if (!StringX.nullity(localhost)) LOCAL_HOST_IP = localhost;
	}

	public String getApp()
	{
		return APPCODE;
	}

	public void setApp(String app)
	{
		APPCODE = app;
	}

	// public void setFtsAgent(String ftsAgent)
	// {
	// FTS_AGENT = ftsAgent;
	// }

	public String getJvm()
	{
		return JVM;
	}

	public void setJvm(String jvm)
	{
		JVM = jvm;
	}

	public void setJvmDesc(String jvmDesc)
	{
		JVMDESC = jvmDesc;
	}

	public void setDataDir(String dataDir)
	{
		DATA_DIR = dataDir;
	}

	public static File getDataDir() throws IOException
	{
		try
		{
			File f = DATA_DIR.startsWith("WEB-INF/")
					? SYSTEM_UTIL.getResourceLoader().getResource(DATA_DIR).getFile()
					: new File(SYSTEM_UTIL.getResourceLoader().getResource(DATA_DIR).getFile(),
							JVM);
			if (!f.exists()) f.mkdirs();
			return f;
		}
		catch (Exception e)
		{
			log.warn("cannot find data dir:" + DATA_DIR);
		}
		return new File(System.getProperty("java.io.tmpdir"));
	}

	// ------------ for json call-------------
	public static Class getBeanClass(String beanId) throws Exception
	{
		BeanDefinition bean = null;
		if (APPCXT instanceof ClassPathXmlApplicationContext)
			bean = ((ClassPathXmlApplicationContext) APPCXT).getBeanFactory()
					.getBeanDefinition(beanId);
		else if (APPCXT instanceof XmlWebApplicationContext)
			bean = ((XmlWebApplicationContext) APPCXT).getBeanFactory().getBeanDefinition(beanId);
		else if (APPCXT instanceof GenericApplicationContext)
			bean = ((GenericApplicationContext) APPCXT).getBeanFactory().getBeanDefinition(beanId);
		if (bean == null)
		{
			log.info("spring context is :{}", APPCXT.getClass());
			return null;
		}
		Class c = null;
		if (bean.getBeanClassName().startsWith("com.alibaba.dubbo"))
		{ // ���������ص��Ǵ���config.spring.ReferenceBean
			try
			{
				if (!StringX.nullity(bean.getDescription()))
					c = Class.forName(bean.getDescription(), false,
							Thread.currentThread().getContextClassLoader());
				log.info("dubbo:{}, {}", bean.getDescription(), beanId);
			}
			catch (Exception e)
			{
				log.info("fail dubbo:{}, {}", bean.getDescription(), beanId);
			}
		}
		else
		{
			c = Class.forName(bean.getBeanClassName(), false,
					Thread.currentThread().getContextClassLoader());
			log.info("bean:{}, {}", bean.getBeanClassName(), beanId);
		}
		return c;
	}

	// argNum:����������-1����δ֪����, request must be String, Map or List
	public static Object jsonCall(String service, String m, Object request, int argNum)
			throws Exception
	{
		boolean flat = m.indexOf("$$") > 0; // ��Ҫ����flat
		int idx = m.lastIndexOf('$');
		if (idx > 0)
		{
			argNum = Integer.parseInt(m.substring(idx + 1));
			m = m.substring(0, m.indexOf('$'));
		}
		// is a json string
		if (request instanceof String) request = JsonUtil.json2obj((String) request);
		// is rest api
		if (request != null && request instanceof Map)
			request = restfulRequest((Map) request, service, m, null, argNum, flat);
		Class s = getBeanClass(service);
		log.info("jsc: {}.{}, argNum:{}", service, m, argNum);
		return jsonCall(s, service, m, (List) request);
	}

	// ֱ��ͨ������ӿ�����λ�����ģʽ����
	public static Object jsonCall(Class service, Object target, String m, List request)
			throws Exception
	{
		if (target instanceof String) target = APPCXT.getBean((String) target);
		else if (target instanceof Class) target = APPCXT.getBean((Class) target);
		if (target == null)
		{
			log.info("target null for: {}", service);
			throw new AppException(AppRetCode.SERVICE_NOTEXIST, new Object[] { service });
		}
		Method method = null;
		Object[] args = null;
		if (service == null) service = target.getClass();
		if (request == null || request.isEmpty())
		{ // û�в���
			log.info("find {}.{}, no args", target.getClass(), m);
			method = MethodUtil.findMethod(target.getClass(), m, 0);
		}
		else
		{ // is args array
			int argNum = request.size();
			method = MethodUtil.findMethod(target.getClass(), m, argNum);
			log.info("find {}.{}, args:{}, m:{}", target.getClass(), m, argNum, method.toString());
			args = createArgs(MethodUtil.getGenericParameterTypes(service, m, argNum), request);
		}
		try
		{
			return method.invoke(target, args);
		}
		catch (InvocationTargetException e)
		{
			log.debug("fail to invoke:{}.{} target:{}", target.getClass(), m,
					e.getTargetException());
			throw (Exception) e.getTargetException();
		}
	}

	// ��һ��Map��ʽ��json������������ݷ����Ĳ������������ģʽ
	// argNum: ��������, -1����ͨrest, 2: ���������������һ������Ϊmap/voʱ��ֱ�ӱ�ƽ��
	public static List restfulRequest(Map request, String service, String method, String[] names,
			int argNum, boolean flat) throws Exception
	{
		if (request == null || request.isEmpty()) return null;
		List params = new ArrayList();
		if (flat)
		{
			log.info("flat: {}.{}", service, method);
			params.add(request);
			return params;
		}
		if (names == null) names = restfulParamNames(service, method, argNum);
		log.info("map args: {}.{}, argNum:{}, names:{}", service, method, argNum,
				Arrays.toString(names));
		// �Զ����Բ�����Сд
		Map<String, Object> m = new HashMap<>();
		request.forEach((k, v) -> m.put(k.toString().toLowerCase(), v));

		for (String n : names)
		{
			Object v = request.get(n);
			if (v == null) v = m.get(n.toLowerCase());
			params.add(v);
		}
		return params;
	}

	public static String[] restfulParamNames(String s, String m, int argNum) throws Exception
	{
		String key = s + "." + m + "#" + argNum;
		String[] names = METHOD_PARAM_NAMES.get(key);
		if (names != null) return names;
		Class<?> clazz = SpringUtil.getBeanClass(s);
		Method method = MethodUtil.findMethod(clazz, m, argNum);
		names = MethodUtil.getParameterNames(clazz, method);

		if (names != null) METHOD_PARAM_NAMES.put(key, names);
		else log.info("map args c:{}, m:{}, names:{}", clazz, method, Arrays.asList(names));
		return names;
	}

	static Map<String, String[]> METHOD_PARAM_NAMES = new ConcurrentHashMap<>();

	public static Object[] createArgs(Type[] types, List list)
	{
		if (list == null || list.size() == 0) return null;
		Gson gson = new Gson();
		Object[] args = new Object[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			String json = gson.toJson(list.get(i));
			args[i] = gson.fromJson(json, types[i]);
			if (log.isDebugEnabled())
				log.debug("i:{}, type:{}, json:{}, arg:{}", i, types[i], json, args[i].getClass());
		}
		return args;
	}

	public static Object call(String service, String m, Object[] args) throws Exception
	{
		Object target = SpringUtil.getInstance().getBean(service, null);
		Method method = MethodUtil.findMethod(target.getClass(), m, args == null ? 0 : args.length);
		try
		{
			return method.invoke(target, args);
		}
		catch (InvocationTargetException e)
		{
			log.debug("fail to invoke:{}.{} target:{}", service, m, e.getTargetException());
			throw (Exception) e.getTargetException();
		}
	}
}
