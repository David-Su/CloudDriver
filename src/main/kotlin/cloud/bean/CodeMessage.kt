package cloud.bean

enum class CodeMessage(var code: String, var message: String) {
    OK("0000", ""),
    TOKEN_ILLEGAL("0001", "非法token"),
    TOKEN_TIMEOUT("0002", "token过期"),
    UN_OR_PW_ERROR("0003", "账号或密码错误"),
    CREATE_DIR_FAIL("0004", "创建文件夹目录失败");
}