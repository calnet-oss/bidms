/*
 * Copyright (c) 2020, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Static utility methods for JSON operations.
 */
public class JsonUtil {
    private final static ObjectMapper objectMapper = JsonMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"))
            .defaultTimeZone(TimeZone.getTimeZone("GMT"))
            .build();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static void registerModule(Module module) {
        objectMapper.registerModule(module);
    }

    /**
     * Convert a map to a JSON string.  <i>{}</i> will be returned if the map
     * is null.  This implementation has pretty-print turned off, so the JSON
     * string returned has minimal whitespace.
     *
     * @param map The map to convert to JSON.
     * @return A JSON string.  <i>{}</i> will be returned if the map is null.
     * @throws JsonProcessingException If an error occurs converting the map
     *                                 to JSON.
     */
    public static String convertMapToJson(Map<?, ?> map) throws JsonProcessingException {
        return convertMapToJson(map, false);
    }

    /**
     * Convert a map to a JSON string.  <i>{}</i> will be returned if the map
     * is null.  This implementation has the ability to enable pretty-print
     * such the JSON will have added whitespace to make it easier to read.
     *
     * @param map         The map to convert to JSON.
     * @param prettyPrint true to enable pretty-print
     * @return A JSON string.  <i>{}</i> will be returned if the map is null.
     * @throws JsonProcessingException If an error occurs converting the map
     *                                 to JSON.
     */
    public static String convertMapToJson(Map<?, ?> map, boolean prettyPrint) throws JsonProcessingException {
        if (prettyPrint) {
            return map != null ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map) : "{}";
        } else {
            return map != null ? objectMapper.writeValueAsString(map) : "{}";
        }
    }

    /**
     * Convert a JSON string to a map.  An empty map will be returned if the
     * json string is null or empty.  Otherwise, the root JSON element must
     * be an object.
     *
     * @param json The JSON string to convert to a map.
     * @return A map built from the JSON string.  An empty map will be
     * returned if the json string is null or empty.
     * @throws JsonProcessingException If an error occurs converting the JSON
     *                                 string to a map.
     */
    public static Map convertJsonToMap(String json) throws JsonProcessingException {
        return json != null && json.length() > 0 ? objectMapper.readValue(json, Map.class) : new HashMap();
    }

    /**
     * Convert a list to a JSON string.  <i>[]</i> will be returned if the
     * list is null.  This implementation has pretty-print turned off, so the
     * JSON string returned has minimal whitespace.
     *
     * @param list The list to convert to JSON.
     * @return A JSON string.  <i>[]</i> will be returned if the list is
     * null.
     * @throws JsonProcessingException If an error occurs converting the list
     *                                 to JSON.
     */
    public static String convertListToJson(List<?> list) throws JsonProcessingException {
        return convertListToJson(list, false);
    }

    /**
     * Convert a list to a JSON string.  <i>[]</i> will be returned if the
     * list is null.  This implementation has the ability to enable
     * pretty-print such the JSON will have added whitespace to make it
     * easier to read.
     *
     * @param list        The list to convert to JSON.
     * @param prettyPrint true to enable pretty-print
     * @return A JSON string.  <i>[]</i> will be returned if the list is
     * null.
     * @throws JsonProcessingException If an error occurs converting the list
     *                                 to JSON.
     */
    public static String convertListToJson(List<?> list, boolean prettyPrint) throws JsonProcessingException {
        if (prettyPrint) {
            return list != null ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list) : "[]";
        } else {
            return list != null ? objectMapper.writeValueAsString(list) : "[]";
        }
    }

    /**
     * Convert a JSON string to a list.  An empty list will be returned if
     * the json string is null or empty.  Otherwise, the root JSON element
     * must be an array.
     *
     * @param json The JSON string to convert to a list.
     * @return A list built from the JSON string.  An empty list will be
     * returned if the json string is null or empty.
     * @throws JsonProcessingException If an error occurs converting the JSON
     *                                 string to a list.
     */
    public static List convertJsonToList(String json) throws JsonProcessingException {
        return json != null ? objectMapper.readValue(json, List.class) : new ArrayList();
    }

    /**
     * Convert a POJO to a JSON string.  This implementation has pretty-print
     * turned off, so the JSON string returned has minimal whitespace.
     *
     * @param obj The object to convert to JSON.
     * @return A JSON string.  null will be returned if the obj is null.
     * @throws JsonProcessingException If an error occurs converting the
     *                                 object to JSON.
     */
    public static String convertObjectToJson(Object obj) throws JsonProcessingException {
        return convertObjectToJson(obj, false);
    }

    /**
     * Convert a POJO to a JSON string.  This implementation has the ability
     * to enable pretty-print such the JSON will have added whitespace to
     * make it easier to read.
     *
     * @param obj         The object to convert to JSON.
     * @param prettyPrint true to enable pretty-print
     * @return A JSON string.  null will be returned if the obj is null.
     * @throws JsonProcessingException If an error occurs converting the
     *                                 object to JSON.
     */
    public static String convertObjectToJson(Object obj, boolean prettyPrint) throws JsonProcessingException {
        if (prettyPrint) {
            return obj != null ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) : null;
        } else {
            return obj != null ? objectMapper.writeValueAsString(obj) : null;
        }
    }

    /**
     * Convert a POJO to a map.  This is equivalent to first converting the
     * POJO to a JSON string and then converting that JSON string to a map.
     *
     * @param obj The object to convert to a map.
     * @return A map built from the object using JSON semantics.  null will
     * be returned if the obj is null.
     * @throws JsonProcessingException If an error occurs converting the
     *                                 object to a map.
     */
    public static Map convertObjectToMap(Object obj) throws JsonProcessingException {
        if (obj == null) {
            return null;
        }
        // Not sure this is the most efficient way: TODO perhaps convertValue(obj, Map) instead?
        return convertJsonToMap(convertObjectToJson(obj));
    }

    /**
     * Convert JSON to an object.
     *
     * @param json  The JSON string to convert to an object.
     * @param clazz The class of the object.
     * @param <T>   The class type of the object
     * @return The object converted from JSON.
     * @throws JsonProcessingException If an error occurs converting the JSON
     *                                 to an object.
     */
    public static <T> T convertJsonToObject(String json, Class<T> clazz) throws JsonProcessingException {
        return json != null ? objectMapper.readValue(json, clazz) : null;
    }

    /**
     * Convert a map to an object.
     *
     * @param map   The map to convert to an object.
     * @param clazz The class of the object.
     * @param <T>   The class type of the object.
     * @return The cbject converted from a map.
     * @throws IllegalArgumentException If conversion fails due to
     *                                  incompatible type.  See {@link
     *                                  ObjectMapper#convertValue(Object,
     *                                  Class)}.
     */
    public static <T> T convertMapToObject(Map<?, ?> map, Class<T> clazz) throws IllegalArgumentException {
        return map != null ? objectMapper.convertValue(map, clazz) : null;
    }
}
