package cn.rongcapital.baas.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import cn.rongcapital.baas.entity.FunctionInfo;

@Repository
public interface FunctionInfoRepository extends MongoRepository<FunctionInfo, String> {

	FunctionInfo findOneByNameAndVersion(String name, String version);

}
