package cn.rongcapital.baas;

import java.io.IOException;
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

@Component
public class BaasInvoker {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.function.base.url}")
	private String baseUrlFormat;

	@Value("${baas.function.uri}")
	private String uri;

	@Autowired
	private WebClientCache clientCache;

	public Mono<String> invoke(BaasBean bean) {
		logger.info("请求消息进入BaasInvoker.invoke");
		String tenant = bean.getTenant();
		String name = bean.getName();
		String version = bean.getVersion();
		Integer port = bean.getPort();
		Map<String, Object> body = bean.getBody();
		String baseUrl = String.format(baseUrlFormat, port);
		logger.info("从客户端缓存中获取当前组件对应的客户端");
		WebClient client = clientCache.getClient(baseUrl);
		logger.info("调用组件并通信");
		Mono<String> result = client.post()
									.uri(uri, tenant, name, version)
									.accept(MediaType.APPLICATION_JSON)
									.contentType(MediaType.APPLICATION_JSON)
									.body(Mono.just(body), Map.class)
									.retrieve()
									.bodyToMono(String.class);
		result = result.timeout(Duration.ofMillis(DurationDefinition.EXECUTOR_TIMEOUT_MS)).retry(5, error -> {
			logger.info("IOException Retry");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return error instanceof IOException;
		});
		return result;
	}

}
