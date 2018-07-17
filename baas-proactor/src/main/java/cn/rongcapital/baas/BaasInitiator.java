package cn.rongcapital.baas;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaasInitiator {

	@Autowired
	private BaasExecutor baasExecutor;

	@PostConstruct
	public void init() {
		baasExecutor.startup();
	}

}
