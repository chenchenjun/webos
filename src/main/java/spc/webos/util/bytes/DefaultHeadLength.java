package spc.webos.util.bytes;

import spc.webos.util.NumberX;
import spc.webos.util.StringX;
import spc.webos.util.charset.EBCDUtil;

public class DefaultHeadLength implements IHeadLength
{
	public int offset(int hlen)
	{
		return containHdrLenSelf ? hlen : hdrLen + hlen;
	}

	public int remain(int len, int hLen)
	{
		if (!containHdrLenSelf) return hLen + hdrLen - len;
		return hLen - len;
	}

	public byte[] lenBytes(int len)
	{
		if (hdrLen <= 0) return null; // 710_20140606 ���û�г���ͷ�򷵻ؿ�
		len += (containHdrLenSelf ? hdrLen : 0); // �Ƿ񳤶�ͷ��Ϣ�ĳ��Ȱ�������ͷ�ֽ���
		if (hdrLenBinary) return NumberX.int2bytes(len, hdrLen); // ���ö����Ʊ�ʾģʽ
		String strlen = StringX.int2str(String.valueOf(len), hdrLen); // ���ȹ̶�Ϊ10���Ƶ�8���ֽڣ�����ǰ�油0
		return len2bcd ? EBCDUtil.gbk2bcd(strlen) : strlen.getBytes();
	}

	public int length(byte[] lenBytes)
	{
		if (hdrLenBinary) return NumberX.bytes2int(lenBytes) - (containHdrLenSelf ? hdrLen : 0);
		return new Integer(len2bcd ? EBCDUtil.bcd2gbk(lenBytes) : new String(lenBytes).trim())
				.intValue() - (containHdrLenSelf ? hdrLen : 0);
	}

	public DefaultHeadLength()
	{
	}

	public DefaultHeadLength(int hdrLen, boolean hdrLenBinary)
	{
		this.hdrLenBinary = hdrLenBinary;
		this.hdrLen = hdrLen;
	}

	public DefaultHeadLength(int hdrLen, boolean hdrLenBinary, boolean containHdrLenSelf,
			boolean len2bcd)
	{
		this.hdrLenBinary = hdrLenBinary;
		this.containHdrLenSelf = containHdrLenSelf;
		this.len2bcd = len2bcd;
		this.hdrLen = hdrLen;
	}

	public int getHdrLen()
	{
		return hdrLen;
	}

	public int hdrLen = 8; // ÿ�η��͵�ͷ����, ���Ϊ<=0��ʾ����Ҫ���ͳ��ȱ�ʶ
	public boolean containHdrLenSelf; // chenjs 2012-11-22 ����ͷ�����Ƿ����ͷ���ȱ���,
	// ���糤��ͷ8���ֽڣ�����200���ֽڣ�����ͷ��ϢΪ208�ֽ�
	public boolean len2bcd; // ���͵ĳ����ֶ��Ƿ���ҪBCDת��
	public boolean hdrLenBinary; // ͷ�ֽڵĳ�����Ϣ�ö����Ʊ�ʾ������asc��

	public String toString()
	{
		return "DHL:" + hdrLen + ":" + hdrLenBinary + ":" + len2bcd + ":" + containHdrLenSelf;
	}
}
