package spc.webos.tcc.service;

/**
 * TCCԭ�ӷ������
 * 
 * @author chenjs
 *
 */
public interface TccAtomService
{
	int doTring(TCC tcc);

	int doTried(TCC tcc);

	int doConfirm(TCC tcc);

	int doCancel(TCC tcc);

	int insertCancel(TCC tcc);
}
