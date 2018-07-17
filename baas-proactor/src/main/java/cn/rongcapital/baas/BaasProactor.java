package cn.rongcapital.baas;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class BaasProactor {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BaasDispatcher baasDispatcher;

	@Autowired
	private BaasExecutor baasExecutor;

	@PostMapping("/baas/proactor/{tenant}/{name}/{version}")
	public Mono<BaasResult> process(@PathVariable String tenant, @PathVariable String name, @PathVariable String version, @RequestBody Map<String, Object> body) {
		logger.info("请求消息进入BaasProactor.process");
		logger.info("生成pin码");
		String pinCode = UUID.randomUUID().toString();
		logger.info("调用dispatch");
		baasDispatcher.dispatch(tenant, name, version, pinCode, body);
		logger.info("绑定执行结果");
		return baasExecutor.bind(pinCode);
	}

}
