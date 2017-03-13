package spc.webos.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/* 
 * ���ܣ�ѹ���ļ���tar.gz��ʽ 
 */
public class TarUtil
{
	public static List unzip(File zf, File parent) throws Exception
	{
		List target = new ArrayList();
		try (ZipFile zipFile = new ZipFile(zf))
		{
			Enumeration<ZipEntry> e = zipFile.getEntries();
			ZipEntry zipEntry = null;
			while (e.hasMoreElements())
			{
				zipEntry = e.nextElement();
				if (zipEntry.isDirectory())
				{
					String name = zipEntry.getName();
					name = name.substring(0, name.length() - 1);
					File f = new File(parent, name);
					f.mkdir();
				}
				else
				{
					File f = new File(parent, zipEntry.getName());
					f.createNewFile();
					target.add(f);
					FileUtil.is2os(zipFile.getInputStream(zipEntry), new FileOutputStream(f), true,
							true);
				}
			}
		}
		return target;
	}

	public static void zip(OutputStream target, InputStream is, String fileName) throws IOException
	{
		zip(target, new InputStream[] { is }, new String[] { fileName });
	}

	public static void zip(File[] files, File target) throws IOException
	{
		zip(files, target, null);
	}

	public static void zip(File[] files, File target, String[] fileNames) throws IOException
	{
		zip(files, new FileOutputStream(target), fileNames);
	}

	/**
	 * ������ļ�ѹ��Ϊһ���ļ�, �����ļ������������ĵģ�������Ҫ����apache�����ZipEntry
	 * 
	 * @param files
	 * @param target
	 * @param fileNames
	 *            ���������ļ���
	 * @throws IOException
	 */
	public static void zip(File[] files, OutputStream target, String[] fileNames) throws IOException
	{
		try (ZipOutputStream zos = new ZipOutputStream(target))
		{
			for (int i = 0; i < files.length; i++)
			{
				File f = files[i];
				String fileName = fileNames == null ? f.getName() : fileNames[i];
				zos.putNextEntry(new org.apache.tools.zip.ZipEntry(fileName));
				FileUtil.is2os(new FileInputStream(f), zos, true, false);
				zos.flush();
				zos.closeEntry();
			}
		}
	}

	public static void zip(OutputStream target, InputStream[] is, String[] fileNames)
			throws IOException
	{
		try (ZipOutputStream zos = new ZipOutputStream(target))
		{
			zos.setLevel(9);
			for (int i = 0; i < is.length; i++)
			{
				zos.putNextEntry(new org.apache.tools.zip.ZipEntry(fileNames[i]));
				FileUtil.is2os(is[i], zos, true, false);
				zos.flush();
				zos.closeEntry();
			}
		}
	}

	public static byte[] zip(byte[] buf, int offset, int len) throws IOException
	{
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zip = new ZipOutputStream(baos))
		{
			zip.write(buf, offset, len);
			zip.close();
			return baos.toByteArray();
		}
	}

	/*
	 * �������ܣ������ļ����µ����ݣ���������ļ��У��͵���tarPack���� ������out ����������ļ����� inputFile Ҫѹ�����ļ��л��ļ�
	 * base ����ļ��е�·��
	 */
	public static void tar(TarOutputStream tos, File dir, String path) throws Exception
	{
		File[] files = dir.listFiles();
		// �ڴ���ļ������·��
		tos.putNextEntry(new TarEntry(path + "/"));
		path = StringX.nullity(path) ? StringX.EMPTY_STRING : path + "/";
		for (File file : files)
			tar(tos, file, path + file.getName());
	}

	/*
	 * �������ܣ�����ļ� ������out ѹ���������ļ����� inputFile Ҫѹ�����ļ��л��ļ� base ����ļ��е�·��
	 */
	public static void tar(TarOutputStream tos, File file) throws Exception
	{
		tar(tos, new FileInputStream(file), file.length(), file.getName());
	}

	public static void tar(TarOutputStream tos, InputStream is, long length, String entry)
			throws Exception
	{
		TarEntry tarEntry = new TarEntry(entry);
		tarEntry.setSize(length); // ���ô���ļ��Ĵ�С����������ã���������ݵ��ļ�ʱ���ᱨ��
		try
		{
			tos.putNextEntry(tarEntry);
			FileUtil.is2os(is, tos, false, false);
		}
		finally
		{
			try
			{
				if (null != tos) tos.closeEntry();
			}
			catch (IOException e)
			{
			}
			try
			{
				if (null != is) is.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	/*
	 * �������ܣ���ô�����ļ����� ������targetFileName ������ļ���·��
	 */
	public static TarOutputStream getTarOutputStream(OutputStream os)
	{
		TarOutputStream out = new TarOutputStream(os);
		// �������������Σ���ѹ�����е�·���ֽ�������100 byteʱ���ͻᱨ��
		out.setLongFileMode(TarOutputStream.LONGFILE_GNU);
		return out;
	}
}
