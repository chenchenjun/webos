package spc.webos.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil
{
	static Logger log = LoggerFactory.getLogger(FileUtil.class);
	public final static FileUtil INSTANCE = new FileUtil();
	static FSTConfiguration fst = FSTConfiguration.createDefaultConfiguration();

	static
	{
		fst.setClassLoader(Thread.currentThread().getContextClassLoader());
	}

	public static FileUtil getInstance()
	{
		return INSTANCE;
	}

	public static File make(String fileNm)
	{
		return make(null, fileNm);
	}

	// �ڸ�Ŀ¼�д���һ���ļ�
	public static File make(File parent, String fileNm)
	{
		int idx = fileNm.lastIndexOf('/');
		if (idx <= 0) return new File(parent, fileNm);
		// ���fileNm����·���������·�������Ŀ¼������
		File p = new File(parent, fileNm.substring(0, idx - 1));
		if (!p.exists()) p.mkdirs();
		return new File(p, fileNm.substring(idx + 1));
	}

	// public static void main(String[] args) throws Exception
	// {
	// appendToFile("aaa".getBytes(), make(null,
	// "/Users/chenjs/Downloads/a/b/c/a.txt"));
	// }

	// ������׷�ӵ�һ���ļ�ĩβ
	public static void writeToFile(byte[] buf, File file, boolean append) throws IOException
	{
		if (!file.exists()) file.getParentFile().mkdirs();
		else if (!append) file.delete();
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true)))
		{
			bos.write(buf); // ����ǰ���յ���һ������д�뵽�ļ�
		}
	}

	/**
	 * ��һ��Ϊzip��ʽ������������ѹ��parent�ļ�����
	 * 
	 * @param zis
	 * @param parent
	 * @throws IOException
	 */
	public static List unzip(ZipInputStream zis, File parent) throws IOException
	{
		if (!parent.exists()) parent.mkdirs();
		List targetFiles = new ArrayList();
		ZipEntry zentry = null; // Zip�ļ��е�ѹ���ļ�ʵ��
		try (ZipInputStream ziss = zis)
		{ // ���ļ�
			byte[] buf = new byte[1024];
			int size = 0;
			while ((zentry = zis.getNextEntry()) != null)
			{
				File target = new File(parent, zentry.getName());
				if (target.exists()) target.delete();
				// ������zip�ļ�����ͬ���ļ�
				try (BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(target)))
				{

					// ��zip�ļ��е�����д��
					while ((size = zis.read(buf, 0, buf.length)) != -1)
						bos.write(buf, 0, size);
					zis.closeEntry();
					targetFiles.add(target);
				}
			}
		}
		return targetFiles;
	}

	public static byte[] unzip(byte[] buf, int offset, int size) throws IOException
	{
		return FileUtil.is2bytes(new ZipInputStream(new ByteArrayInputStream(buf, offset, size)));
	}

	public static byte[] gzip(byte[] buf, int offset, int len) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(baos);
		gzip.write(buf, offset, len);
		gzip.close();
		return baos.toByteArray();
	}

	public static byte[] ungzip(byte[] buf, int offset, int size) throws IOException
	{
		return FileUtil.is2bytes(new GZIPInputStream(new ByteArrayInputStream(buf, offset, size)));
	}

	public static void ungzip2os(byte[] buf, int offset, int size, OutputStream os)
			throws IOException
	{
		is2os(new GZIPInputStream(new ByteArrayInputStream(buf, offset, size)), os, true, false);
	}

	public static void ungzip2os(InputStream is, OutputStream os) throws IOException
	{
		is2os(new GZIPInputStream(new BufferedInputStream(is)), os, true, false);
	}

	public static byte[] readMsgWithLen(InputStream is, byte[] bufLen) throws Exception
	{
		return readMsgWithLen(is, bufLen, false);
	}

	/**
	 * ��һ�����ж�ȡ�����ĳ�����Ϣ�ı��ģ� ǰ���ֽ�Ϊ�������ĵĳ���˵��
	 * 
	 * @param is
	 * @param bufLen
	 * @param bcd
	 *            ���س����Ƿ�bcd����
	 * @return
	 * @throws Exception
	 */
	public static byte[] readMsgWithLen(InputStream is, byte[] bufLen, boolean bcd) throws Exception
	{
		if (log.isDebugEnabled()) log.debug("start to recieve bytes...bufLen is null: "
				+ (bufLen == null || bufLen.length == 0));
		// added by spc 2011-05-03 ���û���ṩͷ��������Ϊû��ͷ����.
		if (bufLen == null || bufLen.length == 0) return is2bytes(true, is, false);
		int l = is.read(bufLen);
		if (l == -1)
		{
			log.warn("read length fail. inputstream has closed!!!");
			return null;
		}
		int len = new Integer(new String(bufLen)).intValue();
		// if (log.isInfoEnabled()) log.info("current msg len=" + len);
		// byte[] buf = new byte[len];
		// int read;
		// for (int remained = len; remained != 0; remained -= read)
		// {
		// read = is.read(buf, len - remained, remained);
		// if (read == -1 && remained != 0) throw new
		// IllegalStateException("size=" + len
		// + ", but remained=" + remained);
		// }
		// if (log.isDebugEnabled()) log.debug("read buf.len: " + buf.length +
		// ", recieved msg is :"
		// + (bcd ? EBCDUtil.bcd2gbk(buf) : new String(buf)));
		return readMsgWithLen(is, len);
	}

	public static byte[] readMsgWithLen(InputStream is, int len) throws Exception
	{
		if (log.isInfoEnabled()) log.info("read:" + len);
		if (len <= 0)
		{ // 2012-07-10 �������С��0�����ȡ���п����ֽ�
			return is2bytes(true, is, false);
		}
		byte[] buf = new byte[len];
		int read;
		for (int remained = len; remained != 0; remained -= read)
		{
			read = is.read(buf, len - remained, remained);
			if (read == -1 && remained != 0)
				throw new IllegalStateException("size=" + len + ", but remained=" + remained);
		}
		if (log.isTraceEnabled()) log.trace(
				"buf.len: " + buf.length + ", base64 :" + new String(StringX.encodeBase64(buf)));
		return buf;
	}

	public static byte[] is2bytes(InputStream is) throws IOException
	{
		return is2bytes(is, true);
	}

	public static byte[] is2bytes(InputStream is, boolean autocloseis) throws IOException
	{
		return is2bytes(false, is, autocloseis);
	}

	public static byte[] is2bytes(boolean socket, InputStream is, boolean autocloseis)
			throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		is2os(socket, is, baos, autocloseis, true);
		return baos.toByteArray();
	}

	public static void is2os(InputStream is, OutputStream os, boolean autocloseis,
			boolean autocloseos) throws IOException
	{
		is2os(false, is, os, autocloseis, autocloseos);
	}

	/**
	 * ��һ����������ȡ��һ���������
	 * 
	 * @param is
	 * @param os
	 * @param autocloseis
	 *            �Ƿ��Զ��ر�������
	 * @param autocloseos
	 *            �Ƿ��Զ��ر������
	 * @throws IOException
	 */
	public static void is2os(boolean socket, InputStream is, OutputStream os, boolean autocloseis,
			boolean autocloseos) throws IOException
	{
		byte[] buf = new byte[1024];
		try
		{
			int len = is.read(buf);
			// if (log.isDebugEnabled()) log.debug("first is2os: len: " + len);
			while (len != -1)
			{
				os.write(buf, 0, len);
				// 701_20140115 ע�͵�socket�����is availableΪ�յ��µĶ�ȡʧ��
				// if (socket && is.available() <= 0) break;
				len = is.read(buf);
				// if (log.isDebugEnabled()) log.debug("is2os: len: " + len);
			}
		}
		finally
		{
			try
			{
				if (autocloseis && is != null) is.close();
			}
			catch (Exception e)
			{
			}
			try
			{
				if (os != null) os.flush();
				if (autocloseos && os != null) os.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	// �������л��ͷ����л�
	public static byte[] serialize(Serializable o, boolean gzip) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = baos;
		if (gzip) os = new GZIPOutputStream(os);
		serialize(o, os);
		os.close();
		return baos.toByteArray();
	}

	public static Object deserialize(byte[] buf, boolean gzip)
			throws IOException, ClassNotFoundException
	{
		return deserialize(buf, 0, buf.length, gzip);
	}

	public static Object deserialize(byte[] buf, int offset, int len, boolean gzip)
			throws IOException, ClassNotFoundException
	{
		InputStream is = new ByteArrayInputStream(buf, offset, len);
		if (gzip) is = new GZIPInputStream(is);
		return deserialize(is);
	}

	public static void serialize(Serializable o, OutputStream os) throws IOException
	{
		fst(o, os);
		// ObjectOutputStream oos = new ObjectOutputStream(os);
		// oos.writeObject(o);
		// oos.flush();
	}

	public static byte[] fst(Object o)
	{
		return fst.asByteArray(o);
	}

	public static void fst(Object o, OutputStream os) throws IOException
	{
		FSTObjectOutput out = fst.getObjectOutput(os);
		out.writeObject(o);
		out.flush();
	}

	public static Object unfst(byte[] arr) throws Exception
	{
		return fst.getObjectInput(arr).readObject();
	}

	public static Object unfst(InputStream is) throws Exception
	{
		return fst.getObjectInput(is).readObject();
	}

	public static Object deserialize(InputStream is) throws IOException, ClassNotFoundException
	{
		return fst.getObjectInput(is).readObject();
		// return new ObjectInputStream(is).readObject();
	}

	// �������л��ͷ����л� end

	public static void convertUTF8(File target, File src) throws IOException
	{
		try (BufferedReader br = new BufferedReader(new FileReader(target));
				BufferedWriter bw = new BufferedWriter(new FileWriter(src)))
		{
			StringBuffer sb = new StringBuffer();
			while (true)
			{
				String line = null;
				line = br.readLine();
				if (line == null) break;
				bw.write('\n');
				if (line.length() == 0) continue;
				sb.setLength(0);
				sb.append(line);
				bw.write(StringX.str2utf8(sb.toString()));
			}
		}
	}

	public static void download(String url, OutputStream os) throws Exception
	{
		URLConnection cnn = new URL(url).openConnection();
		cnn.connect();
		is2os(new BufferedInputStream(cnn.getInputStream()), os, true, true);
	}

	/**
	 * д�ļ���ָ���ط����ϴ��ļ���ת��
	 * 
	 * @param src
	 * @param targetPath
	 *            Ŀ���ļ�·��
	 * @param fileName
	 *            Ŀ���ļ���
	 * @throws Exception
	 */
	public File renameFile(File src, String uploadFilePathPrefix, String targetPath,
			String fileName) throws Exception
	{
		if (src == null || src.length() <= 0) return null;
		File targetDir = new File(SpringUtil.getInstance().getResourceLoader()
				.getResource(uploadFilePathPrefix).getFile(), targetPath);
		if (!targetDir.exists()) targetDir.mkdirs();
		File dest = new File(targetDir, fileName);
		if (dest.exists()) dest.delete();
		src.renameTo(dest);
		return dest;
	}

	/**
	 * ɾ��WEB�����е��ļ�
	 * 
	 * @param request
	 * @throws IOException
	 */
	public boolean deleteFiles(List files) throws IOException
	{
		if (files == null) return false;
		for (int i = 0; i < files.size(); i++)
		{
			String fileName = (String) files.get(i);
			File file = SpringUtil.getInstance().getResourceLoader().getResource(fileName)
					.getFile();
			if (file.exists()) delete(file);
		}
		return true;
	}

	/**
	 * �ļ�����ȫ������ΪBase64�ַ���
	 * 
	 * @param absolutePah
	 * @return
	 */
	public static String file2base64(String absolutePah) throws Exception
	{
		return file2base64(new File(absolutePah));
	}

	public static String file2base64(File file) throws Exception
	{
		if (file == null || !file.exists()) return StringX.EMPTY_STRING;
		return new String(StringX.encodeBase64(file2bytes(file)));
	}

	public static byte[] file2bytes(File file) throws Exception
	{
		if (file == null || !file.exists()) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		is2os(new FileInputStream(file), baos, true, true);
		return baos.toByteArray();
	}

	public static boolean deleteByAbsolutePath(String absolutePah)
	{
		return delete(new File(absolutePah));
	}

	public static boolean delete(File file)
	{
		if (file == null) return true;
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			for (int i = 0; files != null && i < files.length; i++)
				delete(files[i]);
		}
		file.delete();
		return true;
	}

	// ���ص�ǰclasspath�µ�ĳ���ļ�
	public static InputStream loadClasspathFile(String fileName)
	{
		StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path", "."),
				System.getProperty("path.separator"));
		// windows����·���ķָ�����;��unix������:
		InputStream is = null;
		while (is == null && st.hasMoreTokens())
		{
			String strClassPath = (st.nextToken()).replace('\\', '/');
			if (strClassPath.endsWith(".jar") || strClassPath.endsWith(".zip"))
			{ // ����jar����zip���ǲ����ܷ������ļ���
				continue;
			}
			try
			{
				is = new FileInputStream(strClassPath + '/' + fileName);
				break;
			}
			catch (Exception e)
			{
				continue;
			}
		}
		return is;
	}

	/**
	 * ��ϵͳ����ʱ����������һ����ʱ�ļ�
	 * 
	 * @return
	 * @throws IOException
	 */
	public static File makeTempFile() throws IOException
	{
		String fileName = String.valueOf(Math.random()).substring(2);
		File f = new File(SpringUtil.getDataDir(), fileName);
		if (f.exists()) f.delete();
		return f;
	}

	// ͨ������·����ȡ�ļ�����
	public static long fileLen(String absolutePah)
	{
		File file = new File(absolutePah);
		if (!file.exists()) return -1;
		return file.length();
	}

	// added by spc 2011-04-15 ����ftp������
	/*
	 * public static FTPClient ftpCnn(String host, int port, String user, String
	 * pwd) throws Exception { try { FTPClient ftpClient = new FTPClient();
	 * FTPClientConfig ftpClientConfig = new FTPClientConfig();
	 * ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
	 * ftpClient.setControlEncoding("GBK");
	 * ftpClient.configure(ftpClientConfig); if (!StringX.nullity(user))
	 * ftpClient.login(user, pwd); return ftpClient; } catch (Exception ex) {
	 * String errMsg = "err to cnn ftp, host:" + host + ", port:" + port +
	 * ", user:" + user; log.error(errMsg, ex); throw new Exception(errMsg +
	 * ex.getMessage()); } }
	 * 
	 * public static void ftpDiscnn(FTPClient client) { try { if (client !=
	 * null) client.disconnect(); } catch (Exception e) { log.warn(
	 * "fail to discnn ftp:" + client.getRemoteAddress(), e); } }
	 * 
	 * // ���Ͷ���ļ���Զ�� public static void ftpPut(FTPClient client, String dir,
	 * String[] fileName, InputStream[] is) throws Exception { if
	 * (log.isDebugEnabled()) log.debug("cd ftp dir: " + dir); if
	 * (!StringX.nullity(dir)) client.changeWorkingDirectory(dir);
	 * client.setFileType(FTPClient.BINARY_FILE_TYPE); for (int i = 0; i <
	 * fileName.length; i++) { if (log.isDebugEnabled()) log.debug(
	 * "start to ftp put file: " + fileName[i]); client.storeFile(fileName[i],
	 * is[i]); // FileUtil.is2os(is[i], client.put(fileName[i]), true, true); if
	 * (log.isDebugEnabled()) log.debug("success to ftp put file: " +
	 * fileName[i]); } }
	 * 
	 * // ����һ��Զ���ļ� public static void ftpGet(FTPClient client, String dir,
	 * String fileName, OutputStream os) throws Exception { if
	 * (log.isDebugEnabled()) log.debug("start to ftp get file: " + fileName +
	 * ", dir: " + dir); if (!StringX.nullity(dir))
	 * client.changeWorkingDirectory(dir);
	 * client.setFileType(FTPClient.BINARY_FILE_TYPE);
	 * client.retrieveFile(fileName, os); //
	 * FileUtil.is2os(client.get(fileName), os, true, true); if
	 * (log.isDebugEnabled()) log.debug("success to ftp get file: " + fileName);
	 * }
	 */
	// added by spc 2011-04-15 end
}
