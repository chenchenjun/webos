package spc.webos.service.common;

import java.util.List;
import java.util.Map;

import spc.webos.model.MenuPO;
import spc.webos.util.tree.TreeNode;

public interface ExtjsService
{
	// ��ȡ���ݿ����õ�js�ļ�
	String js(String id);
	
	MenuPO getMenu(String id);

	String getMenus();

	TreeNode getMenuTree(String parentId);

	TreeNode getExtTree(String sqlId);

	// ͨ��request���Ext��ʽ��json������
	TreeNode getExtTree(String sqlId, Map param);

	// ˢ�������������Ϣ
	void removeTrees(List treesId);

	// ��ȡϵͳ���е����ṹ
	Map getTrees();

	// ��ȡϵͳ�������ṹ��Ϣ
	// TreeNode getSysFns();
}
