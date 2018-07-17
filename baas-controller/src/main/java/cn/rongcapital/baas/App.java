package cn.rongcapital.baas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableAsync
@EnableWebFlux
@SpringBootApplication
@Import({ CommonConfig.class })
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
