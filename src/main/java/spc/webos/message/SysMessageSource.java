package spc.webos.message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import spc.webos.service.BaseService;
import spc.webos.util.StringX;

/**
 * ���ϵͳsys_msg��Ĺ��ʻ��ֵ���Ϣ ϵͳ��������Ϣ��������retcode��Ҳ���Դ����config��
 * 
 * @author spc
 * 
 */
public class SysMessageSource extends BaseService implements MessageSource
{
	private String sqlId;
	private Map message;
	static SysMessageSource SYS_MSG = new SysMessageSource();

	private SysMessageSource()
	{
		versionKey = "status.refresh.common.sysms";
	}

	public static SysMessageSource getInstance()
	{
		return SYS_MSG;
	}

	public void refresh()
	{
		log.info("sys message sqlId:{}", sqlId);
		if (StringX.nullity(sqlId)) return; // ����app_config��
		Map message = new HashMap();
		List list = (List) persistence.execute(sqlId, null);
		for (int i = 0; i < list.size(); i++)
		{
			List row = (List) list.get(i); // ȡ��Model����
			String key = (String) row.get(0); // code
			String value = (String) row.get(1); // text
			message.put(key, new MessageFormat(value));
		}
		this.message = message;
		log.debug("msg:{}", message);
	}

	public String getMessage(String code, Object[] args, String defmsg, Locale locale)
	{
		// System.out.println("code:"+code);
		int index = code.indexOf(AppMessageSource.SEPARATOR);
		String prefix = index > 0 ? code.substring(0, index) : code;
		Object obj = message.get(prefix);
		if (obj == null)
		{
			if (defmsg == null) return null; // Ĭ����ϢҲ������һ����Ϣģ��
			return new MessageFormat(defmsg).format(args);
		}
		MessageFormat mf = (obj instanceof MessageFormat) ? (MessageFormat) obj
				: new MessageFormat(obj.toString());
		return mf.format(args);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException
	{
		return resolvable.getDefaultMessage();
	}

	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException
	{
		return getMessage(code, args, null, locale);
	}

	public void setSqlId(String sqlId)
	{
		this.sqlId = sqlId;
	}

	public Map getMessage()
	{
		return message;
	}
}
