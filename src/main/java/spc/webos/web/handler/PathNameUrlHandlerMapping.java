package spc.webos.web.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

public class PathNameUrlHandlerMapping extends BeanNameUrlHandlerMapping
{
	protected Object lookupHandler(String urlPath, HttpServletRequest request) throws Exception
	{
//		System.out.println("urlPath: " + urlPath);
		Object handler = super.lookupHandler(urlPath, request);
		if (handler == null)
		{ // 800, ���uriֱ�Ӳ��Ҳ�������ƥ��ǰ��һ��
			String path = urlPath.substring(0, urlPath.indexOf('/', 2));
			handler = super.lookupHandler(path, request);
//			System.out.println("path: " + path + ":" + handler);
		}
		handler = handler == null ? getRootHandler() : handler;
//		System.out.println("urlPath: " + urlPath + ":" + handler);
		return handler;
	}
}
