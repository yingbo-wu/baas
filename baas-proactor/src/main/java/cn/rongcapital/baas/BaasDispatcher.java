package cn.rongcapital.baas;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class BaasDispatcher {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.dispatcher.uri}")
	private String uri;

	@Autowired
	private WebClient client;

	public void dispatch(String tenant, String name, String version, String pinCode, Map<String, Object> body) {
		logger.info("请求消息进入BaasDispatcher.dispatch");
		Mono<String> result = client.post()
									.uri(uri, tenant, name, version, pinCode)
									.accept(MediaType.APPLICATION_JSON)
									.contentType(MediaType.APPLICATION_JSON)
									.body(Mono.just(body), Map.class)
									.retrieve()
									.bodyToMono(String.class);
		result = result.timeout(Duration.ofMillis(DurationDefinition.EXECUTOR_TIMEOUT_MS));
		result.publishOn(Schedulers.parallel()).subscribe();
	}

}
