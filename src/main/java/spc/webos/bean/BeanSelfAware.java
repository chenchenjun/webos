package spc.webos.bean;

/**
 * ����ע�뵱ǰspring���󵽷�����
 * 
 * @author spc
 * 
 */
public interface BeanSelfAware
{
	void self(Object proxyBean);

	Object self();
}
