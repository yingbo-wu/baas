package cn.rongcapital.baas;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RScoredSortedSet;
import org.redisson.client.protocol.ScoredEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class BaasSelector {

	private final static long EXPIRE = 1000 * 60 * 10;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${baas.redisson.alive.set.name}")
	private String aliveSetName;

	@Autowired
	private RedissonCache redissonCache;

	public Mono<String> select() {
		long currentTime = System.currentTimeMillis();
		return Mono.create(callback -> {
			RScoredSortedSet<Object> sortedSet = redissonCache.getSortedSet(aliveSetName);
			Collection<ScoredEntry<Object>> collection = sortedSet.entryRange(0, 0);
			if (CollectionUtils.isNotEmpty(collection)) {
				ScoredEntry<Object> entry = collection.stream().findFirst().get();
				logger.info("entry is {}", entry);
				double score = entry.getScore();
				logger.info("score is {}", score);
				if (EXPIRE < currentTime - score) {
					Object value = entry.getValue();
					logger.info("value is {}", value);
					sortedSet.remove(value);
					callback.success((String) value);
				} else {
					callback.success("NONE");
				}
			} else {
				callback.success("NONE");
			}
		});
	}

}
