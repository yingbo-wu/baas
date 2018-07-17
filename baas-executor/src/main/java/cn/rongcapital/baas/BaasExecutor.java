package cn.rongcapital.baas;

import java.util.concurrent.locks.LockSupport;

import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import cn.rongcapital.baas.utils.GsonUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BaasExecutor {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.redisson.task.queue.name}")
	private String taskQueueName;

	@Value("${baas.redisson.result.queue.name}")
	private String resultQueueName;

	@Value("${baas.redisson.result.map.name}")
	private String resultMapName;

	@Autowired
	private RedissonCache redissonCache;

	@Autowired
	private BaasInvoker baasInvoker;

	@Async
	public void startup() {
		logger.info("启动BaasExecutor");
		RQueue<Object> taskQueue = redissonCache.getQueue(taskQueueName);
		RQueue<Object> resultQueue = redissonCache.getQueue(resultQueueName);
		RMap<String, Object> resultMap = redissonCache.getMap(resultMapName);
		while (true) {
			try {
				taskQueue.pollAsync().thenAcceptAsync(value -> {
					if (null != value) {
						logger.info("taskQueue存在任务");
						logger.info("解析taskQueue中的任务");
						BaasBean bean = GsonUtils.create().fromJson((String) value, BaasBean.class);
						String pinCode = bean.getPinCode();
						Integer port = bean.getPort();
						try {
							if (null == port || 0 == port) {
								logger.info("执行port值不合法场景");
								BaasResult baasResult = new BaasResult(500, null, "Port is 0");
								String json = GsonUtils.create().toJson(baasResult);
								resultMap.put(pinCode, json);
							} else {
								logger.info("执行port值合法场景");
								logger.info("调用baasInvoker与baas组件通信");
								Mono<String> result = baasInvoker.invoke(bean);
								logger.info("订阅baasInvoker与baas组件通信内容");
								result.publishOn(Schedulers.parallel()).subscribe(payload -> {
									BaasResult baasResult = null;
									if (null != payload) {
										logger.info("成功取出resultQueue中的通信内容");
										baasResult = new BaasResult(200, payload);
									} else {
										logger.info("取出resultQueue中的通信内容失败");
										baasResult = new BaasResult(500, null, "error");
									}
									logger.info("解析resultQueue中的通信内容");
									String json = GsonUtils.create().toJson(baasResult);
									logger.info("保存解析结果");
									resultMap.putAsync(pinCode, json).thenAcceptAsync(ok -> {
										logger.info("存放通信内容到resultQueue中");
										resultQueue.offerAsync(pinCode);
									});
								}, error -> {
									logger.info("调用baasInvoker与baas组件通信过程中发生异常");
									BaasResult baasResult = new BaasResult(500, null, error.toString());
									String json = GsonUtils.create().toJson(baasResult);
									resultMap.put(pinCode, json);
									logger.error("invoker error is {}", error);
								});
							}
						} catch (Exception e) {
							logger.info("任务整体过程发生异常");
							BaasResult baasResult = new BaasResult(500, null, e.getMessage());
							String json = GsonUtils.create().toJson(baasResult);
							resultMap.put(pinCode, json);
							logger.error("execute error is {}", e);
						}
					}
				});
			} catch (Exception e) {
				logger.error("execute error is {}", e);
			}
			LockSupport.parkNanos(1);
		}
	}

}
