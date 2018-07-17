package cn.rongcapital.baas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import cn.rongcapital.baas.entity.FunctionInfo;
import cn.rongcapital.baas.repository.FunctionInfoRepository;

@Component
public class FunctionCache {

	@Autowired
	private FunctionInfoRepository functionInfoRepository;

	@Cacheable(value = "function_cache", key = "#name+#version")
	public FunctionInfo getFunction(String name, String version) {
		return functionInfoRepository.findOneByNameAndVersion(name, version);
	}

}
