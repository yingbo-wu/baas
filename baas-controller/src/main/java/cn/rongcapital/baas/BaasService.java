package cn.rongcapital.baas;

import java.time.Duration;

import org.redisson.api.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.rongcapital.baas.entity.FunctionInfo;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BaasService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.redisson.alive.map.name}")
	private String aliveMapName;

	@Autowired
	private RedissonCache redissonCache;

	@Autowired
	private FunctionCache functionCache;

	@Autowired
	private BaasAgent baasAgent;

	@Autowired
	private BaasWatcher baasWatcher;

	public Mono<Integer> find(String tenant, String name, String version) {
		logger.info("请求消息进入BaasService.find");
		Mono<Integer> result = Mono.create(callback -> {
			logger.info("构造Mono实例");
			RMap<String, Object> aliveMap = redissonCache.getMap(aliveMapName);
			logger.info("判断aliveMap是否存在{}", name + ":" + version);
			aliveMap.containsKeyAsync(name + ":" + version).thenAcceptAsync(contain -> {
				logger.info("从缓存中查找functionInfo");
				FunctionInfo functionInfo = functionCache.getFunction(name, version);
				Integer port = functionInfo.getPort();
				if (contain) {
					logger.info("执行aliveMap中存在{}场景下的逻辑", name + ":" + version);
					callback.success(port);
				} else {
					logger.info("执行aliveMap中不存在{}场景下的逻辑", name + ":" + version);
					logger.info("调用k8s代理拉起容器");
					Mono<Boolean> pullResult = baasAgent.pull(name, version);
					pullResult.publishOn(Schedulers.parallel()).subscribe(ok -> {
						if (ok) {
							try {
								logger.info("k8s代理拉起容器成功");
								logger.info("调用k8s代理监视容器状态是否running");
								Mono<Boolean> watchResult = baasWatcher.watch(name, version);
								watchResult.publishOn(Schedulers.parallel()).subscribe(running -> {
									if (running) {
										logger.info("k8s代理监视容器状态为running场景");
										aliveMap.put(name + ":" + version, port);
										callback.success(port);
									} else {
										callback.success(0);
									}
								}, error -> {
									logger.info("k8s代理监视容器状态过程中发生异常");
									callback.success(0);
								});
							} catch (Exception e) {
								logger.info("调用k8s代理监视容器状态是否running过程中发生异常");
								callback.success(0);
								logger.error("k8s watch error is {}", e.getMessage());
							}
						} else {
							logger.info("k8s代理拉起容器失败");
							callback.success(0);
						}
					}, error -> {
						logger.info("k8s代理拉起容器过程中发生异常");
						callback.success(0);
						logger.error("k8s pull error is {}", error);
					});
				}
			});
		});
		result = result.timeout(Duration.ofMillis(DurationDefinition.CONTROLLER_TIMEOUT_MS));
		return result;
	}

}
