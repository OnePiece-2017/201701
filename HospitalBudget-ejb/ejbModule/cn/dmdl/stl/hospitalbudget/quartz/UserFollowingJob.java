package cn.dmdl.stl.hospitalbudget.quartz;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.util.Assit;

public class UserFollowingJob implements Job {

	private static Logger logger = Logger.getLogger(UserFollowingJob.class);

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("execute");
		if (!ConfigureCache.isExecuting(getClass())) {
			logger.info("加锁");
			ConfigureCache.setExecuting(getClass(), true);

			// TODO: 业务处理
			try {
				if (Assit.getResultSetSize("select version_info_id from version_info") > 0) {
					ConfigureCache.versionMap.put(1, "ultimate");

				} else {
					ConfigureCache.versionMap.put(1, "trial");
				}
				Thread.sleep(2 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			logger.info("解锁");
			ConfigureCache.setExecuting(getClass(), false);

		} else {
			logger.warn("本次操作被忽略。");
		}
		// TODO: 输出耗时 年月日时分秒毫秒
	}

}
