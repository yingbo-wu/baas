package cn.rongcapital.baas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class BaasController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BaasService baasService;

	@GetMapping("/baas/controller/{tenant}/{name}/{version}")
	public Mono<Integer> process(@PathVariable String tenant, @PathVariable String name, @PathVariable String version) {
		logger.info("请求消息进入BaasController.process");
		Mono<Integer> result = baasService.find(tenant, name, version);
		return result;
	}

}
