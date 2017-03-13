package spc.webos.tcc.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.service.BaseService;
import spc.webos.tcc.Transaction;
import spc.webos.tcc.service.TCC;
import spc.webos.tcc.service.TccAtomService;

public class TccAtomServiceImpl extends BaseService implements TccAtomService
{
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public int doTring(TCC tcc)
	{
		// ���н��׵�һ�ν���������״̬ʧ�ܣ�ҵ��ִ�гɹ������޸�״̬Ϊ�ɹ�
		tcc.setStatus(Transaction.STATUS_TRY_FAIL);
		try
		{
			return persistence.insert(tcc);
		}
		catch (DuplicateKeyException dke)
		{ // try��Ϊ��ˮ���ظ�ʧ�ܣ��ǲ���cancel��
			log.warn("tcc sn repeat: " + tcc.getSn(), dke);
			throw new AppException(AppRetCode.REPEAT_SN, new Object[] { tcc.getSn() },
					dke.toString());
		}
	}

	public int doTried(TCC tcc)
	{
		tcc.setStatus(Transaction.STATUS_TRIED);
		return persistence.update(tcc);
	}

	protected <T> List<T> findTried(T po)
	{
		// ͨ��ҵ��Ψһ��ˮ�ţ��ҵ���ǰ״̬Ϊtried����ˮ��¼
		((TCC) po).setStatus(Transaction.STATUS_TRIED);
		List<T> tried = persistence.get(po);
		if (tried == null || tried.isEmpty())
		{
			log.info("No tried tcc by:{}", ((TCC) po).getSn());
			throw new AppException(AppRetCode.TCC_XID_NOEXISTS,
					new Object[] { ((TCC) po).getSn() });
		}
		return tried;
	}

	public int doConfirm(TCC tcc)
	{
		// ��Ҫupdate ״̬Ϊȷ��
		tcc.setConfirmTm(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date()));
		tcc.setStatus(Transaction.STATUS_CONFIRMED);
		return persistence.update(tcc);
	}

	// 1. ���ĳtcc���׷���ԭ��������Ϊ��ˮ�Ŵ��ڣ���ʱ�ٷ���cancel�������cancel�����˵�ԭ����
	// 2. ���ԭ��������cancel���׵����ҪԤ��վλ
	public int doCancel(TCC tcc)
	{
		tcc.setConfirmTm(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date()));
		tcc.setStatus(Transaction.STATUS_CANCELED); // tried/canceled״̬�����Է���cancel
		int rows = persistence.update(tcc, (String[]) null,
				"and " + statusColumn + " in(" + Transaction.STATUS_TRIED + ","
						+ Transaction.STATUS_TRY_FAIL + "," + Transaction.STATUS_CANCELED + ","
						+ Transaction.STATUS_CANCEL_FAIL + ")",
				false, null);
		if (rows == 1) return 1; // ��������
		if (rows > 1)
		{ // ״̬��תʱ���������ԭsnֻ�ܸı�����һ����¼��0���߶�����˵��������
			log.info("tcc sn:{}, status:{}, rows:{}", tcc.getSn(), Transaction.STATUS_CANCELED,
					rows);
			throw new AppException(AppRetCode.TCC_STATUS_CHANGE_FAIL,
					new Object[] { tcc.getSn(), Transaction.STATUS_CANCELED, rows });
		}
		// 2. ���û�м�¼�� �����һ����¼ռλ����ֹԭ���׹���������ԭ���׹���ֱ��ʧ��
		return ((TccAtomService) self).insertCancel(tcc);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public int insertCancel(TCC tcc)
	{
		log.info("no rows to cancel, insert a blank recode, sn:{}", tcc.getSn());
		return persistence.insert(tcc);
	}

	protected String statusColumn = "tccStatus";

	public void setStatusColumn(String statusColumn)
	{
		this.statusColumn = statusColumn;
	}
}
