package spc.webos.persistence;

import java.util.List;

public class ReportItem
{
	public String[] preFnodes; // ǰ��ִ�еĺ�������, flow node��
	public String[] postFnodes; // ����ִ�еĺ�������, flow node��
	public List preScripts;
	public Script main;
	public List postScripts;
	public String[] dependence;
	public String rowIndex;
}
