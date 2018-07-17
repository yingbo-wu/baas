package cn.rongcapital.baas;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class BaasDispatcher {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BaasController baasController;

	@Autowired
	private BaasPubliser baasPubliser;

	@PostMapping("/baas/dispatcher/{tenant}/{name}/{version}/{pinCode}")
	public Mono<Integer> process(@PathVariable String tenant, @PathVariable String name, @PathVariable String version, @PathVariable String pinCode, @RequestBody Map<String, Object> body) {
		logger.info("请求消息进入BaasDispatcher.process");
		BaasBean bean = new BaasBean(tenant, name, version, pinCode, body);
		logger.info("调用Controller代理获取容器端口信息");
		Mono<Integer> result = baasController.control(tenant, name, version);
		logger.info("订阅Controller代理获取容器端口信息");
		result.publishOn(Schedulers.parallel()).subscribe(port -> {
			logger.info("正常情况发布BaasBean");
			logger.info("port is {}", port);
			bean.setPort(port);
			baasPubliser.publish(bean);
		}, error -> {
			logger.info("异常情况发布BaasBean");
			bean.setPort(0);
			baasPubliser.publish(bean);
			logger.error("dispatcher error is {}", error);
		});
		return result;
	}

}
