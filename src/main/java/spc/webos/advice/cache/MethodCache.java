package spc.webos.advice.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ���淽������ֵ
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MethodCache
{
	String value() default ""; // keyֵ

	boolean read() default true; // ������

	boolean write() default false; // д����

	String delim() default "|"; // ��keys�ָ���

	int expire() default 24 * 3600; // ������Чʱ��, Ĭ��24Сʱ

	boolean md5() default false; // key�Ƿ���Ҫmd5

	/**
	 * ������ĳһ��������ĳ��·���ǵ�ǰ����key��ɲ���
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface MCPath
	{
		String value() default ""; // �����ľ���·������

		String nullValue() default ""; // �������Ϊnull��Ĭ��ֵ
	}
}
