package cloud.bean

enum class CodeMessage(var code: String, var message: String) {
    OK("0000", ""), TOKEN_ILLEGAL("0001", "�Ƿ�token"), TOKEN_TIMEOUT("0002", "token����"), UN_OR_PW_ERROR("0003", "�˺Ż��������"), CREATE_DIR_FAIL("0004", "�����ļ���Ŀ¼ʧ��");
}