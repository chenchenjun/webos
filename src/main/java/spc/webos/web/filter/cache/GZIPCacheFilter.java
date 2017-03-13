package spc.webos.web.filter.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import spc.webos.constant.Web;
import spc.webos.util.StringX;
import spc.webos.web.filter.AbstractURLFilter;

/**
 * �������˻��������URL����, ��һ�η��ʵ�ʱ����Ҫִ���������, �˹��ܽ���ʹ��
 * 
 * @author Hate
 */
public class GZIPCacheFilter extends AbstractURLFilter implements ResourceLoaderAware
{
	long defaultCacheTimeout = 12 * 60; // Ĭ��Ϊ12Сʱ
	ResourceLoader resourceLoader; // �����ļ�����Դ������
	String tempFileDir = "WEB-INF/env/cachedir"; // ������ɵ���ʱ�ļ�Ŀ¼
	String fileName = "MAIN"; // ��queryStringΪ���ַ�����ʱ����õ��ļ���
	String forceFlushParam = "_FORCE_FLUSH_=";
	// long defaultValidFileLength = 0; // Ĭ��Ϊ1024��С�����ݲŸ��軺��, ��ֹ���ɴ����ҳ���ʱ�䲻������

	// public void setDefaultValidFileLength(long defaultValidFileLength)
	// {
	// this.defaultValidFileLength = defaultValidFileLength;
	// }

	public void setTempFileDir(String tempFileDir)
	{
		this.tempFileDir = tempFileDir;
	}

	public void setDefaultCacheTimeout(long defaultCacheTimeout)
	{
		this.defaultCacheTimeout = defaultCacheTimeout;
	}

	public void setResourceLoader(ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

	public void filter(ServletRequest req, ServletResponse res, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if (!Web.GET_METHOD.equalsIgnoreCase(request.getMethod()))
		{ // ֻ��GET������ڷ������˻���֧��
			chain.doFilter(req, res);
			return;
		}
		// System.out.println("clazz: " +
		// response.getOutputStream().getClass());
		Map paramMap = queryStringToMap(patternURL);
		String strCacheTimeout = (String) paramMap.get("timeoutMinute");
		long cacheTimeout = defaultCacheTimeout;
		if (strCacheTimeout != null)
			cacheTimeout = 1000 * 60 * Long.parseLong(strCacheTimeout.trim()); // ���������λ�Ƿ���

		// String strValidFileLength = (String) paramMap.get("validFileLength");
		// long validFileLength = defaultValidFileLength;
		// if (strValidFileLength != null) validFileLength = Long
		// .parseLong(strValidFileLength);

		// customize to match parameters
		String fileName = this.fileName;
		String querySring = request.getQueryString();
		String query = restoreURL(restoreURL(querySring, Web.REQ_KEY_EXT_DC), forceFlushParam); // ����в�ѯ���������ò�ѯ������Ϊ�ļ���
		if (query != null && query.length() > 0)
		{
			// �Ƿ�ѹ��QueryString��Ϊ������ļ���
			if ("true".equalsIgnoreCase((String) paramMap.get("compressQueryString")))
				fileName = StringX.md5(query.getBytes());
			else fileName = query;
		}
		String childPath = getUri(request);
		// get possible cache
		File targetDir = new File(resourceLoader.getResource(tempFileDir).getFile(), childPath);
		if (!targetDir.exists()) targetDir.mkdirs();
		File targetFile = new File(targetDir, fileName);
		if ((querySring != null && querySring.indexOf(forceFlushParam) >= 0) || !targetFile.exists()
				|| cacheTimeout < Calendar.getInstance().getTimeInMillis()
						- targetFile.lastModified()
				|| targetFile.length() < 3)
		{ // ��ǰ�ļ������� or ʱ����� or ǿ���������� ����Servlet chain�������ݵ�ָ�����ļ�
			OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile));
			CacheResponseWrapper wrappedResponse = new CacheResponseWrapper(response, os);
			try
			{
				chain.doFilter(req, wrappedResponse);
			}
			catch (ServletException e)
			{
				targetFile.delete();
				throw e;
			}
			finally
			{
				try
				{
					os.close();
				}
				catch (Exception e)
				{
				}
			}
		}

		/*
		 * // ���ļ�ϵͳ�л�����ɺõ����� InputStream is = new BufferedInputStream( new
		 * FileInputStream(targetFile));
		 * response.setContentType(request.getContentType());
		 * ServletOutputStream sos = res.getOutputStream(); try { if
		 * (targetFile.length() > 3) { int i = is.read(); sos.write((byte) i);
		 * for (i = is.read(); i != -1; i = is.read()) sos.write((byte) i); } }
		 * finally { try { is.close(); } catch (Exception e) { } }
		 */
		// ��ǰ������������ɾ�����ɵĻ����ļ�
		Boolean err = (Boolean) request.getAttribute(Web.RESP_ATTR_ERROR_KEY);
		if (err != null && err.booleanValue()) targetFile.delete();
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getForceFlushParam()
	{
		return forceFlushParam;
	}

	public void setForceFlushParam(String forceFlushParam)
	{
		this.forceFlushParam = forceFlushParam;
	}
}
