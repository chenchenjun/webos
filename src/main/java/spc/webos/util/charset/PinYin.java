package spc.webos.util.charset;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Ascii;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public final class PinYin
{

	/**
	 * A hash table contains <Unicode, HanyuPinyin> pairs
	 */
	private static Map<Object, Object> PINYIN_TABLE = new HashMap<>();

	static
	{
		Properties p = new Properties();
		try
		{
			p.load(PinYin.class.getClassLoader().getResourceAsStream("pinyin.properties"));
			PINYIN_TABLE.putAll(p);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private PinYin()
	{
	}

	/**
	 * ����һ�����ֶ�Ӧ��ƴ������ĸ
	 * 
	 * @param ch
	 * @return
	 */
	public static char getFirstPinYin(char ch)
	{
		if (ch > 128)
		{
			String pinyin = getPinYinFromChar(ch);
			checkNotNull(pinyin, "unknow hanzi %s", ch);
			return Ascii.toUpperCase(pinyin.charAt(0));
		}
		else return ch;
	}

	/**
	 * ����һ�����ֶ�Ӧ������ƴ��
	 * 
	 * @param ch
	 * @return
	 */
	public static String[] getPinYin(char ch)
	{
		int codePointOfChar = ch;
		String codepointHexStr = Integer.toHexString(codePointOfChar).toUpperCase();
		String foundRecord = (String) PINYIN_TABLE.get(codepointHexStr);
		if (foundRecord == null || foundRecord.equals("null")) return null;
		return Iterables.toArray(Splitter.on(',').split(foundRecord), String.class);
	}

	/**
	 * ����һ���ַ�����ƴ������ĸ��,��д��
	 * 
	 * @param str
	 * @return
	 */
	public static String getFirstPinYin(String str)
	{
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			try
			{
				sb.append(getFirstPinYin(ch));
			}
			catch (NullPointerException e)
			{
			}
		}
		return sb.toString();
	}

	/**
	 * ����һ���ַ�����ȫƴ��Сд��
	 * 
	 * @param str
	 * @return
	 */
	public static String getFullPinYin(String str)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (ch > 128)
			{
				String pinyin = getPinYinFromChar(ch);
				if (pinyin != null) sb.append(pinyin);
			}
			else sb.append(ch);
		}
		return sb.toString();
	}

	/**
	 * ����һ�����ֶ�Ӧ��ƴ��
	 * 
	 * @param ch
	 *            Unicode�����ַ�
	 * @return corresponding Hanyu Pinyin Record in Properties file; null if no
	 *         record found
	 */
	private static String getPinYinFromChar(char ch)
	{
		int codePointOfChar = ch;
		String codepointHexStr = Integer.toHexString(codePointOfChar).toUpperCase();
		String foundRecord = (String) PINYIN_TABLE.get(codepointHexStr);
		if (foundRecord == null || foundRecord.equals("null")) return null;
		int index = foundRecord.indexOf(',');
		if (index == -1) return foundRecord;
		return foundRecord.substring(0, index);
	}
}
