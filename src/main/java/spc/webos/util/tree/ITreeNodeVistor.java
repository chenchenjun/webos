package spc.webos.util.tree;

public interface ITreeNodeVistor
{
	// �������false ��ʾ����Ҫ��������, �˷�������������ָ�����͵�Ԫ��
	boolean start(TreeNode treeNode, TreeNode parent, int index);
	
	// �������false ��ʾ����Ҫ��������, �˷�������������ָ�����͵�Ԫ��
	boolean end(TreeNode treeNode, TreeNode parent, int index);
}
