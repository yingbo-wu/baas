package cn.rongcapital.baas;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.jayway.jsonpath.JsonPath;

import cn.rongcapital.baas.utils.GsonUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BaasAgent {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.k8s.kill.uri}")
	private String killUri;

	@Value("${baas.k8s.check.uri}")
	private String checkUri;

	@Autowired
	private WebClient client;

	@SuppressWarnings("unchecked")
	public Mono<Boolean> kill(String name, String version) {
		logger.info("请求消息进入BaasAgent.pull");
		logger.info("GET方式调用k8s deployments api");
		Mono<String> findResult = client.get()
										.uri(killUri, name)
										.accept(MediaType.APPLICATION_JSON)
										.retrieve()
										.bodyToMono(String.class);
		Mono<Boolean> mono = Mono.create(callback -> {
			logger.info("构造Mono实例");
			logger.info("订阅GET方式调用k8s deployments api处理结果");
			findResult.publishOn(Schedulers.parallel()).subscribe(value -> {
				try {
					logger.info("解析k8s deployments api返回结果");
					Map<String, Object> map = GsonUtils.create().fromJson(value, Map.class);
					Map<String, Object> specMap = (Map<String, Object>) map.get("spec");
					Long replicas = (Long) specMap.get("replicas");
					if (0 < replicas) {
						logger.info("执行k8s deployments api返回结果中的副本集数量>0场景");
						specMap.put("replicas", 0);
						logger.info("PUT方式调用k8s deployments api, 设置副本集数量为0");
						Mono<String> pullResult = client.put()
														.uri(killUri, name)
														.accept(MediaType.APPLICATION_JSON)
														.body(Mono.just(map), Map.class)
														.retrieve()
														.bodyToMono(String.class);
						pullResult.publishOn(Schedulers.parallel()).subscribe(ok -> {
							logger.info("PUT方式调用k8s deployments api成功");
							callback.success(true);
						}, error -> {
							logger.info("PUT方式调用k8s deployments api失败");
							callback.success(false);
							logger.error("k8s pull error is {}", error);
						});
					} else {
						logger.info("执行k8s deployments api返回结果中的副本集数量为0场景");
						callback.success(true);
					}
				} catch (Exception e) {
					logger.info("解析k8s deployments api返回结果过程中发生异常");
					callback.success(false);
					logger.error("k8s pull error is {}", e);
				}
			}, error -> {
				logger.info("GET方式调用k8s deployments api过程中发生异常");
				callback.success(false);
				logger.error("k8s find error is {}", error);
			});
		});
		mono = mono.timeout(Duration.ofMillis(DurationDefinition.CONTROLLER_TIMEOUT_MS));
		return mono;
	}

	public Mono<Boolean> check(String name, String version) {
		logger.info("请求消息进入BaasAgent.check");
		logger.info("调用k8s pods api");
		Mono<String> result = client.get()
									.uri(checkUri)
									.accept(MediaType.APPLICATION_JSON)
									.retrieve()
									.bodyToMono(String.class);
		Mono<Boolean> mono = Mono.create(callback -> {
			logger.info("构造Mono实例");
			logger.info("订阅调用k8s pods api处理结果");
			result.publishOn(Schedulers.parallel()).subscribe(value -> {
				try {
					logger.info("解析k8s pods api结果");
					List<Object> states = JsonPath.read(value, "$.items[*].status.containerStatuses[?(@.name == 'baas-" + name + "')]");
					if (CollectionUtils.isEmpty(states)) {
						logger.info("执行k8s pods api结果中state不存在场景");
						callback.success(true);
					} else {
						logger.info("执行k8s pods api结果中state存在场景");
						callback.success(false);
					}
				} catch (Exception e) {
					logger.info("解析k8s pods api结果过程中发生异常");
					callback.success(false);
					logger.error("k8s check error is {}", e);
				}
			}, error -> {
				logger.info("调用k8s pods api过程中发生异常");
				callback.success(false);
				logger.error("k8s check error is {}", error);
			});
		});
		mono = mono.timeout(Duration.ofMillis(DurationDefinition.CONTROLLER_TIMEOUT_MS));
		return mono;
	}

}
