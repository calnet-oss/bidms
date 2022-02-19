package edu.berkeley.bidms.app.restservice.common.util;

import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.stream.Collectors;

public class ObjectErrorUtil {
    /**
     * Like ObjectError.toString(), but redacts rejected value for FieldErrors.
     * Useful for passphrase validation errors, etc.
     */
    public static String redactingToString(List<ObjectError> oeList) {
        Object[] errorStrings = oeList.stream().map(oe -> {
            if (oe instanceof FieldError) {
                return toStringNoValue((FieldError) oe);
            } else {
                return oe.toString();
            }
        }).collect(Collectors.toList()).toArray();

        StringBuilder result = new StringBuilder(128);
        result.append("[").append(StringUtils.arrayToDelimitedString(errorStrings, ",")).append("]");
        return result.toString();
    }

    // Modified from Spring's FieldError.toString() to remove rejected value
    public static String toStringNoValue(FieldError fe) {
        return "Field error in object '" + fe.getObjectName() + "' on field '" + fe.getField() +
                "'; " + resolvableToString(fe);
    }

    // From Spring's DefaultMessageSourceResolvable.resolvableToString()
    protected static String resolvableToString(FieldError fe) {
        StringBuilder result = new StringBuilder(64);
        result.append("codes [").append(StringUtils.arrayToDelimitedString(fe.getCodes(), ","));
        result.append("]; arguments [").append(StringUtils.arrayToDelimitedString(fe.getArguments(), ","));
        result.append("]; default message [").append(fe.getDefaultMessage()).append(']');
        return result.toString();
    }
}
