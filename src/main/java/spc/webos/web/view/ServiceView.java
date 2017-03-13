package spc.webos.web.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

import spc.webos.constant.Common;
import spc.webos.constant.Web;
import spc.webos.util.StringX;
import spc.webos.util.tree.TreeNode;

/**
 * Ĭ�ϵ��÷���󷵻ؽ��view
 * 
 * @author chenjs
 *
 */
public class ServiceView implements View
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	public String getContentType()
	{
		return Common.FILE_HTML_CONTENTTYPE;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		handleData(model, request, response, model.get(Web.SERVICE_RET_KEY));
	}

	protected void handleData(Map model, HttpServletRequest request, HttpServletResponse response,
			Object data) throws Exception
	{
		// c. ��ȡext��ʽ��json������, ֻ��Ҫ���ظ��ĺ�����Ϣ������Ҫ���ظ�����
		if (data instanceof TreeNode)
			response.getWriter().print(StringX.treeNode2ExtJson((TreeNode) data,
					"true".equalsIgnoreCase((String) model.get("root"))));
		else if (data instanceof List) response.getWriter().print(StringX.table2extgrid((List) data,
				0, 0, 0, null, "true".equalsIgnoreCase((String) model.get(Web.REQ_KEY_UTF8))));
		else response.getWriter().print(data); // d. δ֪ģʽ��ֱ����Ϊ���ص����ַ���
	}
}
