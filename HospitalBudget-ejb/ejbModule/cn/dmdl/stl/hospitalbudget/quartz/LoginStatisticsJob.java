package cn.dmdl.stl.hospitalbudget.quartz;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LoginStatisticsJob implements Job {

	private static Logger logger = Logger.getLogger(LoginStatisticsJob.class);

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("execute");
	}

}
