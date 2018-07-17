package cn.rongcapital.baas;

import org.redisson.api.RQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.rongcapital.baas.utils.GsonUtils;

@Component
public class BaasPubliser {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.redisson.task.queue.name}")
	private String taskQueueName;

	@Autowired
	private RedissonCache redissonCache;

	public void publish(BaasBean bean) {
		logger.info("请求消息进入BaasPubliser.publish");
		RQueue<Object> taskQueue = redissonCache.getQueue(taskQueueName);
		String json = GsonUtils.create().toJson(bean);
		logger.info("publish json is {}", json);
		taskQueue.offerAsync(json);
	}

}
