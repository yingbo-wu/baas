package cn.rongcapital.baas;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BaasWatcher {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BaasAgent baasAgent;

	public Mono<Boolean> watch(String name, String version) {
		logger.info("请求消息进入BaasWatcher.watch");
		Mono<Boolean> result = Mono.create(callback -> {
			logger.info("构造Mono实例");
			try {
				logger.info("调用k8s代理检测容器状态");
				Mono<Boolean> checkResult = baasAgent.check(name, version);
				logger.info("订阅k8s代理检测结果");
				checkResult.publishOn(Schedulers.parallel()).subscribe(stopping -> {
					if (stopping) {
						logger.info("k8s代理检测结果为stopping场景");
						callback.success(true);
					} else {
						logger.info("k8s代理检测结果为other场景");
						callback.error(new RuntimeException("container state is not stopping"));
					}
				}, error -> {
					logger.info("调用k8s代理检测容器状态过程中发生异常");
					callback.error(new RuntimeException(error.getMessage()));
				});
			} catch (Exception e) {
				logger.info("运行异常");
				callback.error(e);
			}
		});
		result = result.timeout(Duration.ofMillis(DurationDefinition.MONITOR_TIMEOUT_MS)).delayElement(Duration.ofMillis(DurationDefinition.MONITOR_DELAY_MS)).retry(200);
		return result;
	}

}
