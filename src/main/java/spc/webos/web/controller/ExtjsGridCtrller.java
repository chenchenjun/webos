package spc.webos.web.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import spc.webos.constant.Web;
import spc.webos.persistence.IPersistence;
import spc.webos.persistence.SQLItem;
import spc.webos.service.common.PersistenceService;
import spc.webos.util.StringX;
import spc.webos.web.util.WebUtil;

public class ExtjsGridCtrller extends JSCallCtrller
{
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		ModelAndView mv = new ModelAndView(extGridView);
		Map params = mv.getModel();
		params.putAll(StringX.uri2params(request.getRequestURI(), 2));
		WebUtil.request2map(request, params);

		// 1. �����ext����ѯ����
		String selSql = StringX.null2emptystr((String) params.get(Web.REQ_KEY_SQL_ID),
				(String) params.get(Web.REQ_KEY_SQL));
		// json ��ʽ�ı�����ݣ�ǰ��Ҫ�����sql���
		if (!StringX.nullity(selSql))
		{
			// Ϊ��������ܣ�ֱ���ò�ѯ�����json�ַ�����ʾ, ��ĳЩ������ܲ���Ҫǿ��ת�����ڲ�ѯ������溬�ж���������
			if (!params.containsKey(Web.REQ_KEY_SQL_CLASS))
				params.put(IPersistence.RESULT_CLASS_PREFIX + selSql, SQLItem.RESULT_CLASS_JSON);
			String sizeSql = StringX.null2emptystr((String) params.get(Web.REQ_KEY_SIZE_SQL_ID),
					(String) params.get(Web.REQ_KEY_SIZE_SQL));
			params.put(Web.EXTGRID_DS_KEY, persistenceService.queryPage(selSql, sizeSql, params));
		}
		else
		{ // 2. ִ�з���, ��������д��з�ҳ��־�����滻����ҳ��Ϣ
			String args = StringX.null2emptystr(params.get(argsName));
			String start = (String) params.get(Web.REQ_EXTJS_PAGING_START);
			String page = (String) params.get(Web.REQ_EXTJS_PAGING_PAGE);
			String limit = (String) params.get(Web.REQ_EXTJS_PAGING_LIMIT);
			if (!StringX.nullity(start)) args = args.replaceAll(Web.REQ_PAGING_ARGS_START, start);
			if (!StringX.nullity(page)) args = args.replaceAll(Web.REQ_PAGING_ARGS_PAGE, page);
			if (!StringX.nullity(limit)) args = args.replaceAll(Web.REQ_PAGING_ARGS_LIMIT, limit);
			mv.getModel().put(Web.EXTGRID_DS_KEY, call(request, response, args));
		}
		return mv;
	}

	@Resource
	protected PersistenceService persistenceService;
	protected String extGridView = "extjsGridView";

	public void setPersistenceService(PersistenceService persistenceService)
	{
		this.persistenceService = persistenceService;
	}

	public void setExtGridView(String extGridView)
	{
		this.extGridView = extGridView;
	}
}
