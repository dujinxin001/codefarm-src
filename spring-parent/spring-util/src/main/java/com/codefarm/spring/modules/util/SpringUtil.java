package com.codefarm.spring.modules.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class SpringUtil {

	private static ApplicationContext applicationContext = null;
	// 非@import显式注入，@Component是必须的，且该类必须与main同包或子包
	// 若非同包或子包，则需手动import 注入，有没有@Component都一样
	// 可复制到Test同包测试
	public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (SpringUtil.applicationContext == null) {
			SpringUtil.applicationContext = applicationContext;
		}
//		// 查询所有预警任务,开启的预警任务自动开启
//		RiskWarningTaskService riskWarningTaskServiceStatic = applicationContext.getBean(RiskWarningTaskService.class);
//		List<EarlyWarningTask> earlyWarningTask = riskWarningTaskServiceStatic.findEarlyWarningTaskAll();
//		for (EarlyWarningTask warningTask : earlyWarningTask) {
//			// 如果任务是打开的,可就开启任务
//			if ("1".equals(warningTask.getStat())) {
//				riskWarningTaskServiceStatic.editState("1", warningTask.getId().toString(), warningTask.getTitle());
//				riskWarningTaskServiceStatic.editState("0", warningTask.getId().toString(), warningTask.getTitle());
//			}
//		}
	}

	// 获取applicationContext
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	// 通过name获取 Bean.
	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);

	}

	// 通过class获取Bean.
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	// 通过name,以及Clazz返回指定的Bean
	public static <T> T getBean(String name, Class<T> clazz) {
		return getApplicationContext().getBean(name, clazz);
	}

}
