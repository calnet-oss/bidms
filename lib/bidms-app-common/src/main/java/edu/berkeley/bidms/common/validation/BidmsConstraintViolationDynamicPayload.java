package edu.berkeley.bidms.common.validation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BidmsConstraintViolationDynamicPayload {
    private final String code;
    private final String message;
    private List<?> attributes;

    public BidmsConstraintViolationDynamicPayload(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BidmsConstraintViolationDynamicPayload(String code, String message, List<?> attributes) {
        this.code = code;
        this.message = message;
        this.attributes = attributes;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<?> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<?> attributes) {
        this.attributes = attributes;
    }

    public Map<String, ?> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (code != null) {
            map.put("code", code);
        }
        if (message != null) {
            map.put("message", message);
        }
        if (attributes != null) {
            map.put("attributes", attributes);
        }
        return map;
    }
}
