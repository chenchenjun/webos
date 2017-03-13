package spc.webos.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.Status;

public class JsonUtil
{
	protected static Logger log = LoggerFactory.getLogger(JsonUtil.class);
	static JsonUtil INSTANCE = new JsonUtil();

	public static JsonUtil getInstance()
	{
		return INSTANCE;
	}

	public static String xml2json(String xml) throws Exception
	{
		if (StringX.nullity(xml)) return "";
		return obj2json(StringX.xml2map(xml));
		// StringWriter w = new StringWriter();
		// try (JsonParser jp = new XmlMapper().getFactory().createParser(xml);
		// JsonGenerator jg = new
		// ObjectMapper().getFactory().createGenerator(w))
		// {
		// while (jp.nextToken() != null)
		// jg.copyCurrentEvent(jp);
		// }
		// return w.toString();
	}

	public static String json2xml(String rootName, String json) throws Exception
	{
		json = StringX.trim(json);
		if (!json.startsWith("[") || !json.startsWith("{"))
		{
			// ȥ��json��ʽԭ������˫����
			if (json.startsWith("\"") && json.endsWith("\""))
				json = json.substring(1, json.length() - 1);
			return "<" + rootName + ">" + json + "</" + rootName + ">";
		}
		Object v = gson2obj(json);
		if (v instanceof Map) return StringX.map2xml(rootName, (Map<String, Object>) v);
		// else if (v instanceof List)
		return StringX.list2xml(rootName, (List<Object>) v);
	}

	// 900, ������ftl�в���json����
	public static String gson(Object src)
	{
		return new Gson().toJson(src);
	}

	public static Object gson2obj(String json, String clazz) throws ClassNotFoundException
	{
		return gson2obj(json,
				Class.forName(clazz, false, Thread.currentThread().getContextClassLoader()));
	}

	public static <T> T gson2obj(String json, Class<T> clazz)
	{
		return (T) new Gson().fromJson(json, clazz);
	}

	public static Object gson2obj(String json)
	{
		if (StringX.nullity(json)) return null;
		json = StringX.trim(json, StringX.TRIM_CHAR);
		if (StringX.nullity(json)) return null;
		if (json.charAt(0) == '[') { return new Gson().fromJson(json, ArrayList.class);
		// List l = new ArrayList();
		// l.addAll(JSONArray.toCollection(JSONArray));
		// return dfsJsonList(l);
		}
		return new Gson().fromJson(json, HashMap.class);
		// JSONObject jo = JSONObject.fromObject(json);
		// if (jo.isEmpty() || jo.isNullObject()) return null;
		// return dfsJsonMap((Map) JSONObject.toBean(jo, HashMap.class));
	}

	public static <T> T json2obj(String json, Class<T> clazz)
	{
		ObjectMapper mapper = new ObjectMapper();
		// �������л�jsonʱ��δ֪���Ի�����ķ����л�����ϣ��������ǽ���δ֪���Դ�Ϸ����л����ܣ�
		// ��Ϊ������json����10�����ԣ������ǵ�bean��ֻ������2�����ԣ�����8�����Խ�������
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// ��jsonӳ�䵽java���󣬵õ�country�����Ϳ��Ա�������,��������������ݣ���˵������Ϳ�����
		try
		{
			return mapper.readValue(json, clazz);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static Object json2obj(String json)
	{
		if (StringX.nullity(json)) return null;
		json = StringX.trim(json, StringX.TRIM_CHAR);
		if (StringX.nullity(json)) return null;
		ObjectMapper m = new ObjectMapper();
		m.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		m.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		try
		{
			if (json.charAt(0) == '[') return m.readValue(json, ArrayList.class);
			return m.readValue(json, HashMap.class);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean isPrimitive(Class c)
	{
		return c.isPrimitive() || CharSequence.class.isAssignableFrom(c)
				|| Number.class.isAssignableFrom(c);
	}

	public static String obj2json(Object o)
	{
		return obj2json(o, null);
	}

	static final List<Class<?>> StrSerializerClazz = new ArrayList<>();
	static
	{ // Ϊ��֧��javascript��long����֧�֣�������longתΪstring
		StrSerializerClazz.add(Long.class);
		StrSerializerClazz.add(long.class);
	}

	public static void registerStrSerializer(Class<?> c)
	{
		if (!StrSerializerClazz.contains(c)) StrSerializerClazz.add(c);
	}

	static JsonSerializer<Object> stringSerializer = new JsonSerializer<Object>()
	{
		public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonProcessingException
		{
			jgen.writeString(value.toString());
		}
	};

	public static String obj2json(Object o, Class<?>... clazz)
	{
		ObjectMapper mapper = new ObjectMapper();
		// �������û��ֵ����ôJson�ǻᴦ��ģ�int����Ϊ0��String����Ϊnull������Ϊ[]������������Կ��Ժ��Կ�ֵ����
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		SimpleModule module = new SimpleModule();
		for (Class<?> c : StrSerializerClazz)
			module.addSerializer(c, stringSerializer);
		if (clazz != null)
		{
			for (Class<?> c : clazz)
				if (!StrSerializerClazz.contains(c)) module.addSerializer(c, stringSerializer);
		}
		mapper.registerModule(module);
		try
		{
			return mapper.writeValueAsString(o);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	// for json call service
	// ����һ������soap/json��ʽ�ı��� �����Ӧ���ĸ�ʽ��esb soapһ��
	// {Header:{sndDt:'20160808', sndTm:'0909009', msgCd:'', seqNb:'',
	// sndAppCd:'',
	// refSndAppCd:'', refSndDt:'', refMsgCd:'', refSeqNb:'', replyToQ:'',
	// status:{retcd:'',...} },
	// Body:[...]}
	public static Map<String, Object> soap(String seqNb, String sndAppCd, String msgCd,
			String replyToQ, String replyMsgCd, Object args)
	{
		Map<String, Object> soap = new HashMap<>();
		soap.put(TAG_BODY, args);
		Map<String, String> header = new HashMap<>();
		soap.put(TAG_HEADER, header);
		// set snd info
		String dt = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date());
		header.put(TAG_HEADER_SNDAPP, StringX.nullity(sndAppCd) ? SpringUtil.APPCODE : sndAppCd);
		header.put(TAG_SNDDT, dt.substring(0, 8));
		header.put(TAG_SNDTM, dt.substring(8));
		header.put(TAG_HEADER_MSGCD, msgCd);

		if (!StringX.nullity(replyToQ)) header.put(TAG_HEADER_REPLYTOQ, replyToQ);
		if (!StringX.nullity(replyMsgCd)) header.put(TAG_HEADER_REPLYMSGCD, replyMsgCd);
		header.put(TAG_HEADER_SN, seqNb);
		return soap;
	}

	public static Map<String, Object> jsonRequest(Map<String, Object> soap, String appCd,
			boolean response) throws Exception
	{
		Map<String, Object> header = (Map<String, Object>) soap.get(TAG_HEADER);
		String sndAppCd = (String) header.get(TAG_HEADER_SNDAPP);
		String sndDt = (String) header.get(TAG_SNDDT);
		String seqNb = (String) header.get(TAG_HEADER_SN);
		String msgCd = (String) header.get(TAG_HEADER_MSGCD);
		Status status;
		log.info("JS request snd:{},{},{}, msgCd:{}", sndAppCd, sndDt, seqNb, msgCd);
		try
		{
			String[] m = StringX.split(msgCd, ".");
			String s = m[0].endsWith("Service") ? m[0] : m[0] + "Service";
			Object ret = SpringUtil.jsonCall(s, m[1], soap.get(TAG_BODY), -1);

			soap.put(TAG_BODY, ret);
			status = new Status(AppRetCode.SUCCESS, null, null, appCd, SpringUtil.LOCAL_HOST_IP);
		}
		catch (Exception e)
		{
			soap.remove(TAG_BODY);
			status = SpringUtil.ex2status("", e);
		}
		// ֪ͨ�౨�ĺ�Ӧ���౨�����践��
		if (!response) return soap;

		// ����Ӧ��refXXX��Ϣ
		header.put(TAG_HEADER_REFMSGCD, msgCd);
		header.put(TAG_HEADER_REFSNDAPP, sndAppCd);
		header.put(TAG_HEADER_REFSNDDT, sndDt);
		header.put(TAG_HEADER_REFSNDSN, seqNb);
		// set status
		header.put(TAG_HEADER_STATUS, status);

		// set snd info
		String dt = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date());
		header.put(TAG_HEADER_SNDAPP, appCd);
		header.put(TAG_SNDDT, dt.substring(0, 8));
		header.put(TAG_SNDTM, dt.substring(8));
		header.remove(TAG_HEADER_SN); // ɾ��ԭ��ˮ����Ϣ

		// String replyMsgCd = (String) header.get(TAG_HEADER_REPLYMSGCD);
		// header.remove(TAG_HEADER_REPLYMSGCD);
		// header.remove(TAG_HEADER_MSGCD);
		// if (!StringX.nullity(replyMsgCd)) header.put(TAG_HEADER_MSGCD,
		// replyMsgCd);

		return soap;
	}

	public static Object jsonResponse(Map<String, Object> soap) throws Exception
	{
		Map<String, Object> header = (Map<String, Object>) soap.get(TAG_HEADER);
		Map<String, String> status = (Map<String, String>) header.get(TAG_HEADER_STATUS);
		String sndAppCd = (String) header.get(TAG_HEADER_SNDAPP);
		String sndDt = (String) header.get(TAG_SNDDT);
		String seqNb = (String) header.get(TAG_HEADER_SN);
		String msgCd = (String) header.get(TAG_HEADER_MSGCD);
		String replyMsgCd = (String) header.get(TAG_HEADER_REPLYMSGCD);
		String refSeqNb = (String) header.get(TAG_HEADER_REFSNDSN);
		log.info("JS response snd:{},{},{}, msgCd:{}/{}, refsn:{},retcd:{}", sndAppCd, sndDt, seqNb,
				msgCd, replyMsgCd, refSeqNb, status == null ? "" : status.get("retCd"));

		String[] m = StringX.split(replyMsgCd, "."); // ʹ������ָ����Ӧ��replyMsgCd����
		String s = m[0].endsWith("Service") ? m[0] : m[0] + "Service";

		Object target = SpringUtil.getInstance().getBean(s, null);
		int idx = m[1].indexOf('$');
		Method method = MethodUtil.findMethod(target, idx > 0 ? m[1].substring(0, idx) : m[1], 1);
		if (method != null)
		{ // ȫ����soap
			return method.invoke(target, new Object[] { soap });
		}
		List<Object> args = new ArrayList<>();
		args.add(soap.get(TAG_BODY));
		args.add(status);
		return SpringUtil.jsonCall(s, m[1], args, -1);
	}

	public static String TAG_HEADER = "Header";

	public static String TAG_SNDDT = "sndDt"; // ��������yyyyMMdd
	public static String TAG_SNDTM = "sndTm"; // ����ʱ��HHmmss
	public static String TAG_HEADER_MSGCD = "msgCd"; // ���ı��
	public static String TAG_HEADER_SN = "seqNb"; // ������ˮ�ţ�����Ψһ��ˮ
	public static String TAG_HEADER_SNDAPP = "sndAppCd"; // ����Ӧ�ñ��
	// public static String TAG_HEADER_RCVAPP = "rcvAppCd"; // ����Ӧ�ñ��

	public static String TAG_HEADER_REFMSGCD = "refMsgCd"; // �ο����ı��
	public static String TAG_HEADER_REFSNDAPP = "refSndAppCd"; // �ο�����Ӧ�ñ��
	public static String TAG_HEADER_REFSNDDT = "refSndDt"; // �ο�����ʱ��
	public static String TAG_HEADER_REFSNDSN = "refSeqNb"; // �ο���ˮ��
	public static String TAG_HEADER_REPLYTOQ = "replyToQ"; // 700 Ӧ�����
	public static String TAG_HEADER_REPLYMSGCD = "replyMsgCd"; // ����msgCd
	public static String TAG_HEADER_STATUS = "status";
	public static String TAG_BODY = "Body";
}
