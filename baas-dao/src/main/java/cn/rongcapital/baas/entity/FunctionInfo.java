package cn.rongcapital.baas.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "function_info")
public class FunctionInfo {

	public static final String FIELD_NAME = "name";
	public static final String FIELD_VERSION = "version";
	public static final String FIELD_PORT = "port";
	public static final String FIELD_TYPE = "type";

	@Id
	private String id;

	@Field(FIELD_NAME)
	private String name;

	@Field(FIELD_VERSION)
	private String version;

	@Field(FIELD_PORT)
	private Integer port;

	@Field(FIELD_TYPE)
	private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
