package cn.rongcapital.baas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientCache {

	@Autowired
	private ReactorClientHttpConnector clientHttpConnector;

	@Cacheable(value = "web_client_cache", key = "#baseUrl")
	public WebClient getClient(String baseUrl) {
		return WebClient.builder().clientConnector(clientHttpConnector).baseUrl(baseUrl).build();
	}

}
