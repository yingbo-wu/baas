package cn.rongcapital.baas;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import reactor.core.publisher.MonoSink;

@Component
public class BaasExecutor {

	private final static Map<String, MonoSink<BaasResult>> BAAS_MAP = new ConcurrentHashMap<String, MonoSink<BaasResult>>();

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.redisson.result.queue.name}")
	private String resultQueueName;

	@Value("${baas.redisson.result.map.name}")
	private String resultMapName;

	@Autowired
	private RedissonCache redissonCache;

	public Mono<BaasResult> bind(String pinCode) {
		logger.info("请求消息进入BaasExecutor.bind");
		Mono<BaasResult> mono = Mono.create(callback -> {
			logger.info("存放pin码与callback");
			BAAS_MAP.put(pinCode, callback);
		});
		mono = mono.timeout(Duration.ofMillis(DurationDefinition.PROACTOR_TIMEOUT_MS));
		return mono;
	}

	@Async
	public void startup() {
		logger.info("启动BaasExecutor");
		RQueue<Object> resultQueue = redissonCache.getQueue(resultQueueName);
		RMap<String, Object> resultMap = redissonCache.getMap(resultMapName);
		while (true) {
			try {
				resultQueue.pollAsync().thenAcceptAsync(key -> {
					if (null != key) {
						logger.info("resultQueue中存在返回结果");
						resultMap.getAsync((String) key).thenAcceptAsync(value -> {
							if (null != value) {
								logger.info("解析resultQueue中的返回结果");
								BaasResult baasResult = GsonUtils.create().fromJson((String) value, BaasResult.class);
								MonoSink<BaasResult> sink = BAAS_MAP.get(key);
								logger.info("响应处理结果");
								sink.success(baasResult);
								resultMap.removeAsync((String) key);
							}
						});
					}
				});
				LockSupport.parkNanos(1);
			} catch (Exception e) {
				logger.error("execute error is {}", e);
			}
		}
	}

}
