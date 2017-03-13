package spc.webos.service.alarm.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.util.StringUtils;

import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.exception.AppException;
import spc.webos.model.AlarmPO;
import spc.webos.service.BaseService;
import spc.webos.service.alarm.AlarmEventService;
import spc.webos.util.FTLUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public class AlarmEventServiceImpl extends BaseService implements AlarmEventService
{
	@Override
	public AlarmPO proccess(AlarmPO event) throws Exception
	{
		String msg = "";
		if (event == null) return event;
		if (!StringX.nullity(event.getService())) msg = proccessJSC(event);
		else msg = proccessSQL(event);
		event.setLastAlarmMsg(msg);
		update(event);
		return event;
	}

	protected String proccessSQL(AlarmPO event) throws Exception
	{
		// ������ִ��sql��ģ���������
		Map root = StringX.nullity(event.getParam()) ? new HashMap()
				: (Map) JsonUtil.json2obj(event.getParam());
		log.info("params:{}", root);
		// ִ��sql��䲢��������ص�root������
		root.put("event", event);
		String batchSQL = event.getSqlId();
		if (!StringX.nullity(batchSQL))
			persistence.execute(StringUtils.delimitedListToStringArray(batchSQL, StringX.COMMA),
					root, root);
		// ��sql�õ�������Դ��ģ�����ϳ���������
		String msg = "";
		try
		{
			msg = FTLUtil.ftl(event.getFtlId(), root);
		}
		catch (Exception e)
		{
			log.info("please check the ftl:" + event.getFtlId(), e);
		}
		return msg;
	}

	protected String proccessJSC(AlarmPO event) throws Exception
	{
		String[] m = StringX.split(event.getService(), ".");
		return StringX.null2emptystr(
				SpringUtil.jsonCall(m[0].endsWith(servicePostfix) ? m[0] : m[0] + servicePostfix,
						m[1], StringX.null2emptystr(event.getParam()), -1));
	}

	public String getEventMsg(String id) throws Exception
	{
		AlarmPO event = persistence.find(new AlarmPO(id));
		// ������һ�ξ���ʱ������һ�μ��ʱ�䲻���򷵻ؿ�
		if (event == null || StringX.nullity(event.getLastAlarmMsg())
				|| !event.getLastCheckTm().equals(event.getLastAlarmTm()))
			return "";
		Map root = new HashMap();
		if (!StringX.nullity(event.getParam()))
			root.put("params", JsonUtil.json2obj(event.getParam()));
		root.put("event", event);
		root.put("phone", event.getPhone());
		root.put("msg", event.getLastAlarmMsg());
		final String alarmMsg = FTLUtil.ftl(event.getMsgFtlId(), root);// ʹ��sys_ftl����ģ�����ɷ����ŵ�����
		log.info("alarm Q:{}, msg:{}", event.getMsgQ(), alarmMsg);
		return alarmMsg;
	}

	// ����һ��json����
	public String sendEventMsg(String id)
	{
		AlarmPO event = persistence.find(new AlarmPO(id));
		if (event == null || StringX.nullity(event.getLastAlarmMsg())) return "";
		try
		{
			final String alarmMsg = getEventMsg(id);
			log.info("alarm Q:{}, msg:{}", event.getMsgQ(), alarmMsg);
			if (StringX.nullity(alarmMsg)) return alarmMsg;
			mq.execute((jms) -> {
				jms.send(event.getMsgQ(), (s) -> {
					BytesMessage msg = s.createBytesMessage();
					msg.setStringProperty(Common.JMS_TRACE_NO, LogUtil.getTraceNo());
					msg.setJMSExpiration(60 * 1000);
					try
					{
						msg.writeBytes(alarmMsg.getBytes(Common.CHARSET_UTF8));
					}
					catch (Exception e)
					{
						throw new AppException(AppRetCode.CMM_BIZ_ERR, "Charset(utf-8) Err");
					}
					return msg;
				});
			});
		}
		catch (Exception e)
		{
			log.info("please check the ftl:{}, ex:{}", event.getMsgFtlId(), e.toString());
		}
		return "";
	}

	// ����һ���ȴ����͵��¼������ݿ⣬������³ɹ�����Ҫ�����ţ�������²��ɹ������Ⱥ�����߳��Ѿ�����
	protected boolean update(AlarmPO event)
	{
		String tm = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date());
		AlarmPO evt = new AlarmPO(event.getId());
		evt.setLastCheckSec(Long.valueOf(System.currentTimeMillis() / 1000));
		evt.setLastCheckTm(tm);
		if (!StringX.nullity(event.getLastAlarmMsg()))
		{
			evt.setLastAlarmTm(tm);
			evt.setLastAlarmMsg(event.getLastAlarmMsg());
		}
		return persistence.update(evt, new String[] { "id" },
				" and lastchecksec=" + event.getLastCheckSec(), false, null) > 0; // ȷ���ܸ��µ���¼
	}

	@Override
	public String proccessEvent(String id) throws Exception
	{
		AlarmPO event = persistence.find(new AlarmPO(id));
		event = proccess(event);
		String msg = event.getLastCheckTm().equals(event.getLastAlarmTm()) ? event.getLastAlarmMsg()
				: "";
		log.info("alarm:{}, msg: {}", id, msg);
		return msg;
	}

	@Override
	public Map<String, AlarmPO> proccessEvents(String[] ids) throws Exception
	{
		Map<String, AlarmPO> msgs = new HashMap<>();
		for (String id : ids)
			msgs.put(id, proccess(persistence.find(new AlarmPO(id))));
		return msgs;
	}

	protected String servicePostfix = "Service";

	public void setServicePostfix(String servicePostfix)
	{
		this.servicePostfix = servicePostfix;
	}
}
