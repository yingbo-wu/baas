package cn.rongcapital.baas;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaasInitiator {

	@Autowired
	private BaasMonitor baasMonitor;

	@PostConstruct
	public void init() {
		baasMonitor.startup();
	}

}
