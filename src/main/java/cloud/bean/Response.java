package cloud.bean;

public class Response<T> {
	
	public Response(String code, String message, T result) {
		super();
		this.code = code;
		this.message = message;
		this.result = result;
	}
	
	private String code;
	private String message;
	private T result;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T data) {
		this.result = data;
	}

}
