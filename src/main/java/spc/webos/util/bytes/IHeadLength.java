package spc.webos.util.bytes;

/**
 * ͷ��������ģʽ
 * 
 * @author chenjs
 * 
 */
public interface IHeadLength
{
	byte[] lenBytes(int len);

	int length(byte[] lenBytes);

	int getHdrLen();
}
