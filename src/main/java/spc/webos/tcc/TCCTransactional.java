package spc.webos.tcc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ע��ʹ����*Service.tcc*, *Service.atcc*������ ����TCC��������
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TCCTransactional
{
	// ����״̬�洢����
	String value() default "";

	// �Ƿ��첽tcc������
	boolean asyn() default false;

	// ����ִ���������ִ��(����confirm all������cancel all)�����Ƿ���
	boolean retention() default false;

	// for TCCԭ�ӷ���ע��
	boolean doTry() default true;

	boolean doConfirm() default true;
	
	boolean tranlog() default true; // ʹ��ԭ��TCC������Ҫtcc_tranlog��֧��
	
	/**
	 * *TCCService.XXX or *Service.tryXXX������ĳһ��������ĳ��·���ǵ�ǰ�����һ��Ψһ��š�
	 * �˱����Ϊ����״̬������Ϣ���д洢(�൱���ⲿΨһ��ˮ�� �� �ڲ�������ˮ�Ź���)
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface XidParamPath
	{
		String value() default ""; // Xid�����ľ���·������
	}
}
