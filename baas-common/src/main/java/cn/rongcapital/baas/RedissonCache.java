package cn.rongcapital.baas;

import org.redisson.api.RDeque;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class RedissonCache {

	@Autowired
	private RedissonClient redissonClient;

	@Cacheable(value = "redisson_cache", key = "#queueName")
	public RDeque<Object> getQueue(String queueName) {
		return redissonClient.getDeque(queueName);
	}

	@Cacheable(value = "redisson_cache", key = "#mapName")
	public RMap<String, Object> getMap(String mapName) {
		return redissonClient.getMap(mapName);
	}

	@Cacheable(value = "redisson_cache", key = "#setName")
	public RSet<Object> getSet(String setName) {
		return redissonClient.getSet(setName);
	}

	@Cacheable(value = "redisson_cache", key = "#setName")
	public RScoredSortedSet<Object> getSortedSet(String setName) {
		return redissonClient.getScoredSortedSet(setName);
	}

}
