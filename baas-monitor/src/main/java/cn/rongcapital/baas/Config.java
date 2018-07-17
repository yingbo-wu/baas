package cn.rongcapital.baas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Config {

	@Bean
	public WebClient webClient(@Autowired ReactorClientHttpConnector clientHttpConnector, @Value("${baas.k8s.base.url}") String baseUrl) {
		return WebClient.builder().clientConnector(clientHttpConnector).baseUrl(baseUrl).build();
	}

}
