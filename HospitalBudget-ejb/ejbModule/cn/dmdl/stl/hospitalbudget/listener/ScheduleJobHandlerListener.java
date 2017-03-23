package cn.dmdl.stl.hospitalbudget.listener;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;

/**
 * Application Lifecycle Listener implementation class ScheduleJobHandlerListener
 * 
 */
public class ScheduleJobHandlerListener implements ServletContextListener {

	private static Logger logger = Logger.getLogger(ScheduleJobHandlerListener.class);
	private SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	private Scheduler scheduler;

	/**
	 * Default constructor.
	 */
	public ScheduleJobHandlerListener() {
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			String path = getClass().getResource(".").getPath();
			String pathname = path + "../resources/" + "schedule_job.xml";
			File xmlfile = new File(pathname);
			SAXReader reader = new SAXReader();
			scheduler = schedulerFactory.getScheduler();
			Document document = reader.read(xmlfile);
			Element rootElement = document.getRootElement();
			List<?> leafList = rootElement.elements("quartz");
			if (leafList != null && leafList.size() > 0) {
				for (Object leaf : leafList) {
					Element leafElement = (Element) leaf;
					if ("true".equals(leafElement.attribute("enabled").getText()) && leafElement.attribute("for").getText().equals(ConfigureCache.getProjectValue("quartz"))) {
						Element jobDetailElement = leafElement.element("job_detail");
						JobDetail jobDetail = new JobDetail(jobDetailElement.element("name").getText(), jobDetailElement.element("group").getText(), Class.forName(jobDetailElement.element("job_class").getText()));
						Element cronTriggerElement = leafElement.element("cron_trigger");
						CronTrigger cronTrigger = new CronTrigger(cronTriggerElement.element("name").getText(), cronTriggerElement.element("group").getText(), cronTriggerElement.element("cron_expression").getText());
						scheduler.scheduleJob(jobDetail, cronTrigger);
					}
				}
			}
			scheduler.start();
		} catch (Exception e) {
			logger.error("contextInitialized", e);
		}
		logger.info("系统启动完成");// 因为定时器是最后被执行的，所以理论上在这个时间节点上，应用程序应该可以正常通信了。
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
	}

}
