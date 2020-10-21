package edu.berkeley.bidms.common.validation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConstraintViolationDynamicPayload {
    private final String code;
    private final String message;
    private List<?> arguments;

    public ConstraintViolationDynamicPayload(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ConstraintViolationDynamicPayload(String code, String message, List<?> arguments) {
        this.code = code;
        this.message = message;
        this.arguments = arguments;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<?> getArguments() {
        return arguments;
    }

    public void setArguments(List<?> arguments) {
        this.arguments = arguments;
    }

    public Map<String, ?> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (code != null) {
            map.put("code", code);
        }
        if (message != null) {
            map.put("message", message);
        }
        if (arguments != null) {
            map.put("arguments", arguments);
        }
        return map;
    }
}
