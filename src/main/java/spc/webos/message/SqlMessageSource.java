package spc.webos.message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import spc.webos.persistence.jdbc.rowtype.RowList;
import spc.webos.service.BaseService;
import spc.webos.util.StringX;

/**
 * ͨ��sql id��ȡ���ݿ��е�code,text��Ӧ��ϵ, ��Ҫ�Զ��ֶν���ת�ƣ�����select code,name,email...from user
 * ������Ҫ����code�ڴ��벻ͬ������ʱ�򣬷��ز�ͬ��textֵ
 * 
 * @author spc
 * 
 */
public class SqlMessageSource extends BaseService implements MessageSource
{
	private String sqlId;
	private Map message;
	private List dict; // �����������ֵ�

	public final static Map<String, SqlMessageSource> SQL_MSG = new ConcurrentHashMap<>();

	public SqlMessageSource()
	{
		versionKey = "status.refresh.common.sqlms";
	}

	public SqlMessageSource(String sqlId)
	{
		this();
		this.sqlId = sqlId;
	}

	public final static SqlMessageSource getMessageSource(String sqlId)
	{
		return SQL_MSG.get(sqlId);
	}

	public final static SqlMessageSource createSqlMessage(String sqlId, Map params)
	{
		SqlMessageSource sqlmsg = new SqlMessageSource();
		sqlmsg.sqlId = sqlId;
		sqlmsg.message = sqlmsg.loadMessage(params);
		return sqlmsg;
	}

	public List getDict()
	{
		return dict;
	}

	public String toJsonDict()
	{
		return toJsonDict(null);
	}

	public String toJsonDict(String mf)
	{
		MessageFormat format = null;
		if (mf != null) format = new MessageFormat(mf);
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		for (int i = 0; i < dict.size(); i++)
		{
			RowList row = (RowList) dict.get(i);
			if (i != 0) buf.append(',');
			buf.append('[');
			for (int j = 0; j < row.size(); j++)
			{
				if (j != 0) buf.append(',');
				buf.append('\'');
				if (j == 1 && format != null)
					buf.append(StringX.str2utf8(format.format(row.toArray()))); // �ѵ�һ�����ָ���ĸ�ʽ
				else buf.append(StringX.str2utf8((String) row.get(j)));
				buf.append('\'');
			}
			buf.append(']');
		}
		buf.append(']');
		return buf.toString();
	}

	public String toHTML(String value)
	{
		return toHTML(value, null);
	}

	/**
	 * 
	 * @param value
	 * @param format
	 *            html�ı���ʾ�ĸ�ʽ
	 * @return
	 */
	public String toHTML(String value, String format)
	{
		MessageFormat mf = null;
		if (format != null) mf = new MessageFormat(format);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < dict.size(); i++)
		{
			RowList row = (RowList) dict.get(i);
			String key = row.get(0).toString();
			buf.append("<option value=\"");
			buf.append(key);
			if (value.equals(key)) buf.append("\" selected>");
			else buf.append("\">");
			if (mf != null) buf.append(mf.format(row.toArray()));
			else buf.append(row.get(1));
			buf.append("</option>\n");
		}
		return buf.toString();
	}

	public void refresh()
	{
		log.info("sqlId:{}", sqlId);
		this.message = loadMessage(null);
		SQL_MSG.put(sqlId, this);
	}

	Map loadMessage(Map params)
	{
		Map message = new HashMap();
		dict = (List) persistence.execute(sqlId, params);
		for (int i = 0; i < dict.size(); i++)
		{
			List row = (List) dict.get(i);
			String key = row.get(0).toString();
			message.put(key, row.toArray());
		}
		return message;
	}

	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale)
	{
		Object[] row = (Object[]) message.get(code);
		row = (Object[]) message.get(code);
		if (row == null) return defaultMessage;
		if (args == null || args.length == 0) return (String) row[1];
		return MessageFormat.format(args[0].toString(), row);
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
