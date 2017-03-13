package spc.webos.persistence.jdbc.datasource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ����ÿ��������ָ̬������Դ
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DataSource
{
	String value() default ""; // Ĭ������Դ����

	String ds() default ""; // Ĭ������Դ����

	String dsPostfix() default "DS"; // ds spring bean id��׺

	String jtPostfix() default "JT"; // ds spring bean id��׺

	boolean jt() default false; // ����ds()ֵ��������datasource����jdbctemplate, Ĭ����ds

	// ����ǻ��ڲ������ݶ�̬����������Ҫ�ṩ������, ��ͳһ���������ģ����ݹ����ţ��Լ�������̬�ṩ��ֵ���������Դ
	String rule() default "";

	// rule���ô���ʱ���Ƿ���Բ���������
	boolean canNull() default false;

	/**
	 * �ɾ���������ݶ�̬��������Դ������·����.�ָ�
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface ColumnPath
	{
		String value() default ""; // �����ľ���·������
	}
}
