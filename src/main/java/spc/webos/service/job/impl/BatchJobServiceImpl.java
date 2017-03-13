package spc.webos.service.job.impl;

import java.util.List;
import java.util.Map;

import spc.webos.exception.AppException;
import spc.webos.service.job.JobService;

/**
 * ��������ִ�����񣬽����С��������ִ�У���ֹ��������
 * 
 * @author chenjs
 *
 */
public class BatchJobServiceImpl extends LeaderLatchMSJobServiceImpl
{
	protected List<JobService> batchJobs;

	@Override
	public void execute() throws AppException
	{
		batchJobs.forEach((job) -> {
			try
			{
				log.info("start job:{}", job.getName());
				job.execute();
			}
			catch (Throwable t)
			{
				log.warn("fail to run job:" + job.getName(), t);
			}
		});
	}

	@Override
	public void execute(boolean force, Map<String, Object> params) throws AppException
	{
		batchJobs.forEach((job) -> {
			try
			{
				log.info("start job:{}", job.getName());
				job.execute(force, params);
			}
			catch (Throwable t)
			{
				log.warn("fail to run job:" + job.getName(), t);
			}
		});
	}

	public void setBatchJobs(List<JobService> batchJobs)
	{
		this.batchJobs = batchJobs;
	}
}
