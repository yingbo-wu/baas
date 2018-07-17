package cn.rongcapital.baas;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;

@EnableCaching
@Configuration
public class CommonConfig {

	@Bean
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(new ConcurrentMapCache("redisson_cache"), new ConcurrentMapCache("web_client_cache"), new ConcurrentMapCache("function_cache")));
		return cacheManager;
	}

	@Bean
	public RedissonClient redissonClient(@Value("${baas.redisson.address}") String address) {
		Config config = new Config();
		config.useSingleServer()
			  .setAddress(address)
			  .setDnsMonitoring(false)
			  .setDatabase(0)
			  .setConnectTimeout(15000)
			  .setTimeout(60000);
		return Redisson.create(config);
	}

	@Bean
	public ReactorClientHttpConnector clientHttpConnector() {
		return new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 600000).option(ChannelOption.SO_KEEPALIVE, true).compression(true).afterNettyContextInit(ctx -> {
			ctx.addHandlerLast(new ReadTimeoutHandler(600000, TimeUnit.MILLISECONDS));
		}));
	}

}
