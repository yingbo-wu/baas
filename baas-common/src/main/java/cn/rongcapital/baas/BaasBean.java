package cn.rongcapital.baas;

import java.util.HashMap;
import java.util.Map;

public class BaasBean {

	private String tenant;

	private String name;

	private String version;

	private Integer port;

	private String pinCode;

	private Map<String, Object> body;

	public BaasBean() {}

	public BaasBean(String tenant, String name, String version, String pinCode, Map<String, Object> body) {
		this.tenant = tenant;
		this.name = name;
		this.version = version;
		this.pinCode = pinCode;
		if (null == body) {
			this.body = new HashMap<String, Object>();
		} else {
			this.body = body;
		}
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
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

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}

}
