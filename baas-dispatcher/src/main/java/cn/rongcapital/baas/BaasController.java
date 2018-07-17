package cn.rongcapital.baas;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class BaasController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.controller.uri}")
	private String uri;

	@Autowired
	private WebClient client;

	public Mono<Integer> control(String tenant, String name, String version) {
		logger.info("请求消息进入BaasController.control");
		logger.info("调用Controller服务api");
		Mono<Integer> controlResult = client.get()
											.uri(uri, tenant, name, version)
											.accept(MediaType.APPLICATION_JSON)
											.retrieve()
											.bodyToMono(Integer.class);
		controlResult = controlResult.timeout(Duration.ofMillis(DurationDefinition.DISPATCHER_TIMEOUT_MS));
		return controlResult;
	}

}
