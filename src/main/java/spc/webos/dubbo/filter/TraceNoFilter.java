package spc.webos.dubbo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

import spc.webos.util.LogUtil;
import spc.webos.util.StringX;

/**
 * ͬʱ֧��dubbo�ͻ��ˣ�dubbo��������õ�ǰtraceNo, ������traceNo���õ�Log�������������־�������ٺ���Ϣ
 * 
 * @author chenjs
 *
 */
public class TraceNoFilter implements Filter
{
	public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException
	{
		RpcContext rpc = RpcContext.getContext();
		if (rpc.isConsumerSide())
		{ // �����ǰ�����ѷ���������LogUtil�е�׷����Ϣ���õ�rpc����
			log.info("dubbo {} to {}, app:{}", rpc.getMethodName(), rpc.getRemoteAddressString(),
					LogUtil.getAppCd());
			if (LogUtil.getAppCd() != null)
				RpcContext.getContext().setAttachment(APP_CD, LogUtil.getAppCd());
			boolean set = setTraceNoIfEmpty(LogUtil.getTraceNo());
			try
			{
				return invoker.invoke(inv);
			}
			finally
			{ // ˭������Ϣ����ִ�н������������dubboĬ�ϻ������ǰ������̻߳���
				if (set) rpc.setAttachment(TRACE_NO, null);
			}
		}

		// �����ǰ�Ƿ��񷽻��������鵱ǰrpc���Ƿ��и��ٺţ������������
		boolean set = LogUtil.setTraceNo(rpc.getAttachment(TRACE_NO),
				"dubbo:" + inv.getMethodName(), true);
		String appCd = rpc.getAttachment(APP_CD);
		LogUtil.setAppCd(appCd);
		log.info("dubbo {} from {}, app:{}", rpc.getMethodName(), rpc.getRemoteAddressString(),
				appCd);
		try
		{
			return invoker.invoke(inv);
		}
		finally
		{ // ����ǵ�ǰ�߳�������logutil.traceno�� ��ǰ�߳�ִ���������̻߳�����˭����˭���
			if (set) LogUtil.removeTraceNo();
		}
	}

	public static boolean setTraceNoIfEmpty(String traceNo)
	{
		if (StringX.nullity(traceNo) || RpcContext.getContext().getAttachment(TRACE_NO) != null)
			return false;
		RpcContext.getContext().setAttachment(TRACE_NO, traceNo);
		return true;
	}

	Logger log = LoggerFactory.getLogger(getClass());
	public static String TRACE_NO = "TRACE_NO";
	public static String APP_CD = "APP_CD";
}
