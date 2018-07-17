package cn.rongcapital.baas;

public class BaasResult {

	private int statusCode;

	private String functionResult;

	private String errorMessage;

	public BaasResult() {}

	public BaasResult(int statusCode, String functionResult) {
		this.statusCode = statusCode;
		this.functionResult = functionResult;
	}

	public BaasResult(int statusCode, String functionResult, String errorMessage) {
		this.statusCode = statusCode;
		this.functionResult = functionResult;
		this.errorMessage = errorMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getFunctionResult() {
		return functionResult;
	}

	public void setFunctionResult(String functionResult) {
		this.functionResult = functionResult;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
