package spc.webos.persistence.jdbc;

import org.springframework.dao.NonTransientDataAccessException;

/**
 * see org.springframework.dao.DataIntegrityViolationException
 * �������ݲ����ظ����ǳ���ҵ���жϵ��쳣����
 * 
 * @author spc
 * 
 */
public class DuplicateRowException extends NonTransientDataAccessException
{
	private static final long serialVersionUID = 1L;

	public DuplicateRowException(String msg)
	{
		super(msg);
	}

	public DuplicateRowException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}