package cn.rongcapital.baas;

import java.util.concurrent.locks.LockSupport;

import org.redisson.api.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BaasMonitor {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.redisson.alive.map.name}")
	private String aliveMapName;

	@Autowired
	private BaasSelector baasSelector;

	@Autowired
	private BaasWatcher baasWatcher;

	@Autowired
	private BaasAgent baasAgent;

	@Autowired
	private RedissonCache redissonCache;

	@Async
	public void startup() {
		while (true) {
			Mono<String> result = baasSelector.select();
			result.subscribe(value -> {
				if (null != value && !"NONE".equals(value)) {
					String[] array = value.split(":");
					if (null != array) {
						String name = array[0];
						String version = array[1];
						RMap<String, Object> aliveMap = redissonCache.getMap(aliveMapName);
						logger.info("k8s代理kill容器成功");
						Mono<Boolean> killResult = baasAgent.kill(name, version);
						killResult.publishOn(Schedulers.parallel()).subscribe(ok -> {
							if (ok) {
								try {
									logger.info("k8s代理kill容器成功");
									logger.info("调用k8s代理监视容器状态是否stopping");
									Mono<Boolean> watchResult = baasWatcher.watch(name, version);
									watchResult.publishOn(Schedulers.parallel()).subscribe(stopping -> {
										if (stopping) {
											logger.info("k8s代理监视容器状态为stopping场景");
											aliveMap.removeAsync(value);
										}
									}, error -> {
										logger.info("k8s代理监视容器状态过程中发生异常");
									});
								} catch (Exception e) {
									logger.info("调用k8s代理监视容器状态是否stopping过程中发生异常");
									logger.error("k8s watch error is {}", e.getMessage());
								}
							} else {
								logger.info("k8s代理kill容器失败");
							}
						}, error -> {
							logger.info("k8s代理拉起容器过程中发生异常");
							logger.error("k8s pull error is {}", error);
						});
					}
				}
			});
			LockSupport.parkNanos(1);
		}
	}

}
