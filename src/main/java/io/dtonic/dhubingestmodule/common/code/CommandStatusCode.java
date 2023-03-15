package io.dtonic.dhubingestmodule.common.code;

public enum CommandStatusCode {
    /* 진행 중 */
    COMMAND_STATUS_DELETING("DELETING"),
    COMMAND_STATUS_UPDATING("UPDATING"),
    COMMAND_STATUS_STARTING("STARTING"),
    COMMAND_STATUS_STOPPING("STOPPING"),
    COMMAND_STATUS_CREATING("CREATING"),
    COMMAND_STATUS_RUNNING("RUNNING"),

    /* 결과 */
    COMMAND_STATUS_SUCCEED("SUCCEED"),
    COMMAND_STATUS_FAILED("FAILED"),
    
    /* 커맨드 */
    COMMAND_CREATE("CREATE"),
    COMMAND_START("START"),
    COMMAND_DELETE("DELETE"),
    COMMAND_UPDATE("UPDATE"),
    COMMAND_RUN("RUN");


    private String code;

    private CommandStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CommandStatusCode parseCode(String code) {
        for (CommandStatusCode statusCode : values()) {
            if (statusCode.getCode().equals(code)) {
                return statusCode;
            }
        }
        return null;
    }
}
