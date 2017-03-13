package spc.webos.advice.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ע��ʹ���ڷ����ϣ���ʾ��ǰ������Ҫ��־׷��
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogTrace
{
	LogTraceNoType value() default LogTraceNoType.AUTO;

	String location() default "";

	String appCd() default "";

	// �Ƿ�ǿ�Ƹ��ǵ�ǰ��־����
	boolean replace() default false;

	public enum LogTraceNoType
	{
		AUTO, PARAM, REQUEST_USER, RPC
	}

	/**
	 * ������ĳһ��������ĳ��·���ǵ�ǰ������Ҫ��־׷�ٵ��ֶ�
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface LTPath
	{
		String value() default ""; // �����ľ���·������

		String nullValue() default "";
	}
}
