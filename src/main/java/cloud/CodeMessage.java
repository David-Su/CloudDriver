package cloud;

public enum CodeMessage {

	OK("0000", ""), TOKEN_ILLEGAL("0001", "�Ƿ�token"), TOKEN_TIMEOUT("0002", "token����"),
	UN_OR_PW_ERROR("0003", "�˺Ż��������"),CREATE_DIR_FAIL("0004", "�����ļ���Ŀ¼ʧ��");

	public String code;

	public String message;

	CodeMessage(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
