package spc.webos.web.filter.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spc.webos.cache.Cache;
import spc.webos.cache.MapCache;
import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.constant.Web;
import spc.webos.util.FileUtil;
import spc.webos.util.StringX;
import spc.webos.web.filter.AbstractURLFilter;
import spc.webos.web.filter.gzip.GZIPResponseStream;

public class CacheFilter extends AbstractURLFilter
{
	Cache cache = new MapCache(100, 12 * 3600); // Ĭ�ϱ���ʱ��Ϊ12Сʱ
	// long defaultValidFileLength = 0; // Ĭ��Ϊ1024��С�����ݲŸ��軺��, ��ֹ���ɴ����ҳ���ʱ�䲻������
	String[] restoreKeys = { Web.REQ_KEY_EXT_DC, Web.REQ_KEY_FORCE_FLUSH };

	public void filter(ServletRequest req, ServletResponse res, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if (!AppConfig.isProductMode() || !Web.GET_METHOD.equalsIgnoreCase(request.getMethod())) // ϵͳ����ģʽ�²�ʹ�÷���˻���
		{ // ֻ��GET������ڷ������˻���֧��
			chain.doFilter(req, res);
			return;
		}
		if (AppConfig.getInstance().getProperty(Config.app_web_cache_disable, true, false))
			cache.removeAll();
		Map paramMap = queryStringToMap(patternURL);
		boolean zip = false;
		String zipValue = (String) paramMap.get("zip"); // ��url�������Ƿ���Ҫzip,Ĭ�϶���Ҫ.���������ص�һ���Ѿ�zip������Ϣ����Ҫzip
		if (zipValue != null) zip = new Boolean(zipValue).booleanValue();

		String cacheName = (String) paramMap.get("cache"); // �Զ���cache
		Cache cache = null;
		if (cacheName != null) cache = (Cache) appCxt.getBean(cacheName, Cache.class);
		if (cache == null) cache = this.cache; // ����Ĭ�ϻ���

		// customize to match parameters
		String querySring = request.getQueryString();
		String query = querySring;
		// ����в�ѯ���������ò�ѯ������Ϊ�ļ���
		for (String key : restoreKeys)
			query = restoreURL(query, key);

		String uri = getUri(request);
		String key = (query != null) ? uri + '?' + query : uri;
		boolean flush = req.getParameter(Web.REQ_KEY_FORCE_FLUSH) != null;
		log.info("key:{}, cache:{}, flush:{}", key, cacheName, flush);
		if (flush) cache.remove(key); // ǰ��������Ҫǿ�Ƶ�ˢ�»���
		byte[] content = null;
		try
		{
			content = (byte[]) cache.get(key);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		if (content != null)
		{
			String view = req.getParameter(Web.REQ_KEY_VIEW_TYPE);
			log.info("size:{}, zip:{}, view:{}", content.length, zip, view);
			if (view != null)
			{
				String fileName = req.getParameter(Web.REQ_KEY_DOWNLOAD_FILE_NAME);
				if (fileName == null || fileName.length() == 0)
					fileName = "data." + Common.OBJECT_TYPE_EXCEL;
				response.setContentType(Common.getContentType(fileName));
				response.setHeader(Common.REQ_HEADER_KEY_1, "attachment; filename="
						+ new String(StringX.utf82str(fileName).getBytes(), Common.CHARSET_ISO));
			}

			OutputStream os = response.getOutputStream();
			if (os instanceof GZIPResponseStream)
			{
				if (zip) ((GZIPResponseStream) os).writeGZIP(content);
				else
				{ // �����һ�㲻���ܷ���, ���治��zip��ʽ���һ���� ���� ģ��ʹ��.
					// ��Ϊ���صĶ����Ѿ�����zip,���Բ���Ҫ��zip��.�����ص�url����GzipfilterʱӦ��
					// Ҳ�ǲ��÷�Gzipfilter������.
					InputStream is = new ByteArrayInputStream(content);
					FileUtil.is2os(is, os, true, false);
				}
			}
			else
			{ // ������ܵĲ���gzip��, ����Ҫunzip content
				InputStream is = null;
				if (zip) is = new GZIPInputStream(new ByteArrayInputStream(content));
				else is = new ByteArrayInputStream(content);

				FileUtil.is2os(is, os, true, false);
			}
			return;
		}
		// ��ǰ�ļ������� or ʱ����� or ǿ���������� ����Servlet chain�������ݵ�ָ�����ļ�
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStream os = baos;
		if (zip) os = new GZIPOutputStream(baos);
		CacheResponseWrapper wrappedResponse = new CacheResponseWrapper(response, os);
		try
		{
			chain.doFilter(req, wrappedResponse);
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
		try
		{
			Boolean err = (Boolean) request.getAttribute(Web.RESP_ATTR_ERROR_KEY);
			if ((err == null || !err.booleanValue()))
			{
				byte[] b = baos.toByteArray();
				log.info("put cache len:{}", b.length);
				cache.put(key, b);
			}
		}
		catch (Exception e)
		{
			log.error("put2cache", e);
		}
	}

	public void setCache(Cache cache)
	{
		this.cache = cache;
	}
}