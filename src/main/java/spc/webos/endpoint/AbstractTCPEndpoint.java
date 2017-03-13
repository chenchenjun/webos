package spc.webos.endpoint;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.constant.Config;
import spc.webos.exception.AppException;
import spc.webos.util.FileUtil;
import spc.webos.util.StringX;
import spc.webos.util.bytes.BytesUtil;
import spc.webos.util.bytes.DefaultHeadLength;

public abstract class AbstractTCPEndpoint implements Endpoint
{
	protected List readBytesByLength(InputStream is) throws Exception
	{
		List result = new ArrayList();
		do
		{
			int len = dhl.length(FileUtil.readMsgWithLen(is, dhl.hdrLen));
			byte[] buf = FileUtil.readMsgWithLen(is, len);
			if (buf == null) break;
			result.add(buf);
		}
		while (true);
		return result;
	}

	protected byte[] readBytesByHdrLength(InputStream is) throws Exception
	{
		byte[] lenbytes = FileUtil.readMsgWithLen(is, dhl.hdrLen);
		if (lenbytes == null)
		{
			log.warn("fail to read hdrLen!!!");
			return null;
		}
		int len = dhl.length(lenbytes);
		byte[] buf = FileUtil.readMsgWithLen(is, len);
		if (log.isInfoEnabled())
			log.info("len:" + buf.length + ", hdrlen:" + len + "," + new String(lenbytes));
		return buf;
	}

	public List readBytesByLength(byte[] buf) throws Exception
	{
		List result = new ArrayList();
		int offset = 0;
		do
		{
			byte[] nbuf = readBytesByHdrLength(buf, offset);
			if (nbuf == null) break;
			result.add(nbuf);
			offset += dhl.hdrLen + nbuf.length;
		}
		while (true);
		return result;
	}

	protected byte[] readBytesByHdrLength(byte[] buf, int offset) throws Exception
	{
		if (buf == null || offset >= buf.length) return null;
		byte[] lenBytes = BytesUtil.arraycopy(buf, offset, dhl.hdrLen);
		int len = dhl.length(lenBytes);
		if (log.isInfoEnabled()) log.info("len:" + (buf.length - len - offset) + ",hdrlen:" + len
				+ "," + new String(lenBytes));
		return BytesUtil.arraycopy(buf, offset + len, len);
	}

	protected synchronized void ask4cnn() throws Exception
	{
		if (maxCnn <= 0)
		{ // 808 ������������û���������¼��Ϣ��ֱ�ӷ���
			currentCnnNum++;
			return;
		}
		if (currentCnnNum >= maxCnn && wait4cnnTimeout <= 0)
			throw new AppException(AppRetCode.PROTOCOL_SOCKET,
					new Object[] { ip, ports[0], String.valueOf(maxCnn) },
					"maxCnn:" + maxCnn + ", ip:" + ip);
		long deadTm = System.currentTimeMillis() + wait4cnnTimeout * 1000;
		while (currentCnnNum >= maxCnn && deadTm > System.currentTimeMillis())
		{
			long remain = deadTm - System.currentTimeMillis() + 2;
			log.warn("reach maxCnn:" + maxCnn + ", remain mills:" + remain);
			try
			{
				wait(remain);
			}
			catch (Throwable t)
			{
			}
		}
		if (System.currentTimeMillis() > deadTm) throw new AppException(AppRetCode.PROTOCOL_SOCKET,
				new Object[] { "wait for cnn timeout:" + wait4cnnTimeout });
		currentCnnNum++;
		return;
	}

	protected synchronized void releaseCnn()
	{
		if (log.isDebugEnabled()) log.debug("release cnnNum:" + currentCnnNum);
		if (currentCnnNum > 0) currentCnnNum--; // 808, ����Ϊ����
		try
		{
			notifyAll();
		}
		catch (Throwable t)
		{
		}
		return;
	}

	protected String getCurrentIP(Executable exe)
	{
		return ip;
	}

	protected int getCurrentPort(Executable exe)
	{
		return getCurrentPort(ports, randomPort, cursor);
	}

	protected static int getCurrentPort(int[] ports, boolean randomPort, int cursor)
	{
		if (ports.length == 1) return ports[0];
		if (randomPort)
		{ // ʹ������˿�
			int idx = ((int) (Math.random() * 10000000)) % ports.length;
			return ports[idx];
		}
		// ʹ��˳����ѵ�˿�
		if (cursor >= ports.length) cursor = 0;
		return ports[cursor++];
	}

	protected byte[] lenBytes(byte[] buf)
	{
		return dhl.lenBytes(buf.length);
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public void setPort(int port)
	{
		this.ports = new int[] { port };
	}

	public void setPorts(int[] ports)
	{
		this.ports = ports;
	}

	public void setPortRange(String portRange)
	{
		this.ports = createPorts(portRange);
	}

	public Endpoint clone() throws CloneNotSupportedException
	{
		return null;
	}

	public String toString()
	{
		return toString(ip, ports[0]);
	}

	public String toString(String ip, int port)
	{
		return "TCP://" + ip + ":" + port + ", simplex:" + simplex + ", longCnn:" + longCnn + ":"
				+ dhl.hdrLen + ":" + dhl.len2bcd + ":" + dhl.hdrLenBinary;
	}

	protected int[] createPorts(String strPort)
	{
		if (strPort.indexOf('-') > 0)
		{ // port range
			String[] ps = StringX.split(strPort, "-");
			int startPort = Integer.parseInt(ps[0]);
			int endPort = Integer.parseInt(ps[1]);
			int[] ports = new int[endPort - startPort + 1];
			for (int i = 0; i < ports.length; i++)
				ports[i] = startPort + i;
			return ports;
		}
		// only one port
		if (strPort.indexOf(',') < 0) return new int[] { Integer.parseInt(strPort) };
		// port enumation
		String[] ps = StringX.split(strPort, StringX.COMMA);
		int[] ports = new int[ps.length];
		for (int i = 0; i < ports.length; i++)
			ports[i] = Integer.parseInt(ps[i]);
		return ports;
	}

	public void setLocation(String location) throws Exception
	{
		createEndpoint(location);
	}

	protected Map createEndpoint(String location)
	{
		String TRUE = "true";
		this.location = location; // format is:
		// tcp://192.168.0.1:8080/FA/jvm?retryTimes=1&simplex=true&len=8&len2bcd=true&hdrLenBinary=false&timeout=30&sndLenWithBuf=false
		if (log.isDebugEnabled()) log.debug("location:" + location);
		StringTokenizer st = new StringTokenizer(location, ":?");
		List params = new ArrayList();
		while (st.hasMoreTokens())
			params.add(st.nextToken());
		ip = StringX.trim((String) params.get(1), "/");

		String strPorts = (String) params.get(2);
		ports = createPorts(strPorts);

		// ? �������չ����
		String strExtParams = (params.size() > 3 ? (String) params.get(3) : null);
		if (StringX.nullity(strExtParams)) return null;
		Map extParams = StringX.str2map(strExtParams, "&");
		String val = (String) extParams.get("retryTimes");
		if (!StringX.nullity(val)) retryTimes = Integer.parseInt(val);

		val = (String) extParams.get("len");
		if (!StringX.nullity(val)) dhl.hdrLen = Integer.parseInt(val);

		val = (String) extParams.get("hdrLen");
		if (!StringX.nullity(val)) dhl.hdrLen = Integer.parseInt(val);

		val = (String) extParams.get("containHdrLenSelf");
		if (!StringX.nullity(val)) dhl.containHdrLenSelf = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("readAsOverall");
		if (!StringX.nullity(val)) readAsOverall = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("cnnTimeout");
		if (!StringX.nullity(val)) cnnTimeout = Integer.parseInt(val);

		val = (String) extParams.get("soTimeout");
		if (!StringX.nullity(val)) soTimeout = Integer.parseInt(val);

		val = (String) extParams.get("simplex");
		if (!StringX.nullity(val)) simplex = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("longCnn");
		if (!StringX.nullity(val)) longCnn = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("len2bcd");
		if (!StringX.nullity(val)) dhl.len2bcd = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("tcpNoDelay");
		if (!StringX.nullity(val)) tcpNoDelay = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("hdrLenBinary");
		if (!StringX.nullity(val)) dhl.hdrLenBinary = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("trace");
		if (!StringX.nullity(val)) trace = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("randomPort");
		if (!StringX.nullity(val)) randomPort = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("maxCnn");
		if (!StringX.nullity(val)) maxCnn = Integer.parseInt(val);

		val = (String) extParams.get("wait4cnnTimeout");
		if (!StringX.nullity(val)) wait4cnnTimeout = Integer.parseInt(val);

		val = (String) extParams.get("sleepMillis");
		if (!StringX.nullity(val)) sleepMillis = Integer.parseInt(val);

		return extParams;
	}

	public void init() throws Exception
	{
	}

	public void destory()
	{
	}

	protected String location;

	protected String ip;
	protected int[] ports;
	protected int cursor; // ��˿�����£���ǰ�˿ڣ� chenjs 2013-03-28
	protected boolean randomPort; // ����˿�

	protected int maxCnn = 0; // ��������������Ĭ��Ϊ0�� 808�汾���޸�ΪĬ�ϲ�����
	protected long currentCnnNum = 0; // ��ǰ�Ѿ�������������
	protected int wait4cnnTimeout = 0; // �ȴ����ӵ����ʱ��0��, ��ʱ�쳣����

	protected boolean trace; // ��info��־��������£����Եײ����緢���ֽ�
	protected int retryTimes = 0; // ���Դ���Ĭ��Ϊ����һ��
	protected boolean simplex; // �Ƿ񵥹��� ���������ֻ�ܷ������ܽ���
	protected boolean longCnn; // �Ƿ�����
	protected long sleepMillis;

	protected DefaultHeadLength dhl = new DefaultHeadLength();
	// protected int hdrLen = 8; // ÿ�η��͵�ͷ����, ���Ϊ<=0��ʾ����Ҫ���ͳ��ȱ�ʶ
	// protected boolean containHdrLenSelf; // chenjs 2012-11-22 ����ͷ�����Ƿ����ͷ���ȱ���,
	// // ���糤��ͷ8���ֽڣ�����200���ֽڣ�����ͷ��ϢΪ208�ֽ�
	// protected boolean len2bcd; // ���͵ĳ����ֶ��Ƿ���ҪBCDת��
	// protected boolean hdrLenBinary; // ͷ�ֽڵĳ�����Ϣ�ö����Ʊ�ʾ������asc��

	protected boolean readAsOverall = false; // ����Ӧ��ʱһ���Զ�ȡ������Ϣ��������Ϣ

	public static int DEF_CNN_TIMEOUT = 500; // Ĭ�ϴ������ӳ�ʱʱ��Ϊ500
	protected int cnnTimeout = DEF_CNN_TIMEOUT; // ����socket���ӵĳ�ʱʱ��
	public static int DEF_SO_TIMEOUT = 60000; // Ĭ�ϵȴ�Ӧ��ʱ��
	protected int soTimeout = DEF_SO_TIMEOUT; // ��ȡ��Ϣ��ʱʱ��
	public static int DEF_REC_BUF_SIZE = 8192; // Ĭ�Ͻ�������С
	protected int receiveBufferSize = DEF_REC_BUF_SIZE;
	protected boolean tcpNoDelay = true;
	// protected boolean sndLenWithBuf = true; // added by chenjs 2011-11-03
	// ���Ⱥ�����һ������ͨѶ���з��ͣ� true����ߺܶ�����
	protected Logger log = LoggerFactory.getLogger(getClass());

	public long getSleepMillis()
	{
		return sleepMillis;
	}

	public void setSleepMillis(long sleepMillis)
	{
		this.sleepMillis = sleepMillis;
	}

	public void setSimplex(boolean simplex)
	{
		this.simplex = simplex;
	}

	public void setLongCnn(boolean longCnn)
	{
		this.longCnn = longCnn;
	}

	public void setRetryTimes(int retryTimes)
	{
		this.retryTimes = retryTimes;
	}

	public void setLen2bcd(boolean len2bcd)
	{
		dhl.len2bcd = len2bcd;
	}

	public int getHdrLen()
	{
		return dhl.hdrLen;
	}

	public void setHdrLen(int hdrLen)
	{
		dhl.hdrLen = hdrLen;
	}

	public int getLen()
	{
		return dhl.hdrLen;
	}

	public void setLen(int len)
	{
		dhl.hdrLen = len;
	}

	public boolean isReadAsOverall()
	{
		return readAsOverall;
	}

	public void setReadAsOverall(boolean readAsOverall)
	{
		this.readAsOverall = readAsOverall;
	}

	public int getWait4cnnTimeout()
	{
		return wait4cnnTimeout;
	}

	public void setWait4cnnTimeout(int wait4cnnTimeout)
	{
		this.wait4cnnTimeout = wait4cnnTimeout;
	}

	public void setCnnTimeout(int cnnTimeout)
	{
		this.cnnTimeout = cnnTimeout;
	}

	public void setSoTimeout(int soTimeout)
	{
		this.soTimeout = soTimeout;
	}

	public boolean isHdrLenBinary()
	{
		return dhl.hdrLenBinary;
	}

	public void setHdrLenBinary(boolean hdrLenBinary)
	{
		dhl.hdrLenBinary = hdrLenBinary;
	}

	public void setReceiveBufferSize(int receiveBufferSize)
	{
		this.receiveBufferSize = receiveBufferSize;
	}

	public void setTcpNoDelay(boolean tcpNoDelay)
	{
		this.tcpNoDelay = tcpNoDelay;
	}

	public boolean isContainHdrLenSelf()
	{
		return dhl.containHdrLenSelf;
	}

	public void setContainHdrLenSelf(boolean containHdrLenSelf)
	{
		dhl.containHdrLenSelf = containHdrLenSelf;
	}

	public void setRandomPort(boolean randomPort)
	{
		this.randomPort = randomPort;
	}

	public int[] getPorts()
	{
		return ports;
	}

	public int getReceiveBufferSize()
	{
		return receiveBufferSize;
	}

	public boolean isTcpNoDelay()
	{
		return tcpNoDelay;
	}

	public String getIp()
	{
		return ip;
	}

	public boolean isRandomPort()
	{
		return randomPort;
	}

	public int getRetryTimes()
	{
		return retryTimes;
	}

	public boolean isSimplex()
	{
		return simplex;
	}

	public boolean isLongCnn()
	{
		return longCnn;
	}

	public boolean isLen2bcd()
	{
		return dhl.len2bcd;
	}

	public int getCnnTimeout()
	{
		return cnnTimeout;
	}

	public int getSoTimeout()
	{
		return soTimeout;
	}

	public int getMaxCnn()
	{
		return maxCnn;
	}

	public void setMaxCnn(int maxCnn)
	{
		this.maxCnn = maxCnn;
	}

	public boolean isTrace()
	{
		return trace
				|| AppConfig.getInstance().getProperty(Config.app_trace_tcp, true, Boolean.FALSE);
	}

	public void setTrace(boolean trace)
	{
		this.trace = trace;
	}
}
