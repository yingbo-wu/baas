package cn.rongcapital.baas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

@EnableMongoRepositories({ "cn.rongcapital.baas.repository" })
@Configuration
public class MongoConfig {

	@Value("${baas.mongo.servers}")
	private String servers;

	@Value("${baas.mongo.database}")
	private String database;

	@Bean
	public MongoClient mongoClient() {
		MongoClientOptions options = MongoClientOptions.builder().connectTimeout(60000).build();
		List<ServerAddress> addressList = new ArrayList<ServerAddress>();
		Stream.of(servers.split(",")).forEach(server -> {
			String[] strs = server.split(":");
			ServerAddress address = new ServerAddress(strs[0], Integer.parseInt(strs[1]));
			addressList.add(address);
		});
		return new MongoClient(addressList, options);
	}

	@Bean
	public MongoDbFactory mongoDbFactory() {
		return new SimpleMongoDbFactory(mongoClient(), database);
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		MongoDbFactory mongoDbFactory = mongoDbFactory();
		MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), new MongoMappingContext());
		converter.setTypeMapper(new DefaultMongoTypeMapper(null));
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);
		return mongoTemplate;
	}

}
