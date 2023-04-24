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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Static utility methods for JSON operations.
 */
public class JsonUtil {
    // Default objectMapper that does not produce maps with sorted keys
    private final static ObjectMapper objectMapper = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"))
            .defaultTimeZone(TimeZone.getTimeZone("GMT"))
            .build();

    // A specialized objectMapper that produces maps with sorted keys.
    // One use of this is to serialize person objects into JSON.
    private final static ObjectMapper objectMapperSorted = JsonMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"))
            .defaultTimeZone(TimeZone.getTimeZone("GMT"))
            .build();

    private final static XmlMapper xmlMapper = XmlMapper.builder()
            .build();

    public static ObjectMapper getSortedKeysObjectMapper() {
        return objectMapperSorted;
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
        return convertObjectToJson(obj, prettyPrint, false);
    }

    /**
     * Convert a POJO to a JSON string.  This implementation has the ability
     * to enable pretty-print such the JSON will have added whitespace to
     * make it easier to read.  It also has the ability to produce JSON with
     * sorted keys.
     *
     * @param obj         The object to convert to JSON.
     * @param prettyPrint true to enable pretty-print
     * @param sortedKeys  true to enable sorting of keys in the JSON maps
     * @return A JSON string.  null will be returned if the obj is null.
     * @throws JsonProcessingException If an error occurs converting the
     *                                 object to JSON.
     */
    public static String convertObjectToJson(Object obj, boolean prettyPrint, boolean sortedKeys) throws JsonProcessingException {
        if (prettyPrint) {
            if (sortedKeys) {
                return obj != null ? objectMapperSorted.writerWithDefaultPrettyPrinter().writeValueAsString(obj) : null;
            } else {
                return obj != null ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) : null;
            }
        } else {
            if (sortedKeys) {
                return obj != null ? objectMapperSorted.writeValueAsString(obj) : null;
            } else {
                return obj != null ? objectMapper.writeValueAsString(obj) : null;
            }
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
        return convertObjectToMap(obj, false);
    }

    /**
     * Convert a POJO to a map.  This is equivalent to first converting the
     * POJO to a JSON string and then converting that JSON string to a map.
     *
     * @param obj        The object to convert to a map.
     * @param sortedKeys true to enable sorting of keys in the JSON maps
     * @return A map built from the object using JSON semantics.  null will
     * be returned if the obj is null.
     * @throws JsonProcessingException If an error occurs converting the
     *                                 object to a map.
     */
    public static Map convertObjectToMap(Object obj, boolean sortedKeys) throws JsonProcessingException {
        if (obj == null) {
            return null;
        }
        return sortedKeys ? objectMapperSorted.convertValue(obj, Map.class) : objectMapper.convertValue(obj, Map.class);
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
     * @return The object converted from a map.
     * @throws IllegalArgumentException If conversion fails due to
     *                                  incompatible type.  See {@link
     *                                  ObjectMapper#convertValue(Object,
     *                                  Class)}.
     */
    public static <T> T convertMapToObject(Map<?, ?> map, Class<T> clazz) throws IllegalArgumentException {
        return map != null ? objectMapper.convertValue(map, clazz) : null;
    }

    /**
     * Options that can be enabled when converting XML to JSON or a map.  If
     * no options are specified then see {@link
     * #DEFAULT_XML_DESERIALIZATION_OPTIONS}.
     */
    public enum XmlDeserializationOption {
        /**
         * Enabling WRAP_ROOT will include the original root XML element in
         * the map.
         */
        WRAP_ROOT,

        /**
         * Enabling REMOVE_ARRAY_WRAPPER will remove wrapper keys around
         * arrays.  See comments of
         * {@link #removeXmlMapArrayWrappers(Map, Map, Object)} for further
         * details.
         */
        REMOVE_ARRAY_WRAPPER
    }

    /**
     * Default options for converting XML to JSON or a map:
     * <ul>
     *     <li>WRAP_ROOT</li>
     *     <li>REMOVE_ARRAY_WRAPPER</li>
     * </ul>
     */
    protected static final XmlDeserializationOption[] DEFAULT_XML_DESERIALIZATION_OPTIONS = new XmlDeserializationOption[]{
            XmlDeserializationOption.WRAP_ROOT,
            XmlDeserializationOption.REMOVE_ARRAY_WRAPPER
    };

    /**
     * Convert XML to a map.
     *
     * @param xml     The XML string to convert to a map.
     * @param options Options for conversion.
     * @return The map converted from XML.
     * @throws JsonProcessingException If an error occurs converting the XML
     *                                 to a map.
     */
    public static Map<?, ?> convertXmlToMap(String xml, XmlDeserializationOption[] options) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        Map<?, ?> map = null;

        // Jackson does not include the root xml element in the map so we
        // can wrap it with a dummy root element to get the original root
        // into the map.
        if (isWrapRootEnabled(options)) {
            // Deserialize as a DOM.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
            is.close();

            // Modify original DOM by wrapping it with a new root element.
            Node originalRoot = doc.removeChild(doc.getDocumentElement());
            Element newRoot = doc.createElement("ROOT");
            newRoot.appendChild(originalRoot);
            doc.appendChild(newRoot);

            // Reserialize for Jackson to parse.
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory.newDefaultInstance().newTransformer().transform(domSource, result);
            writer.flush();
            String reserialized = writer.toString();
            writer.close();

            map = xmlMapper.readValue(reserialized, Map.class);
        } else {
            // no root node wrapper, deserialize input as-is
            map = xmlMapper.readValue(xml, Map.class);
        }

        return isRemoveArrayWrapperEnabled(options) ? removeXmlMapArrayWrappers(map, null, null) : map;
    }

    /**
     * Convert XML to a map using default options: {@link #DEFAULT_XML_DESERIALIZATION_OPTIONS}.
     *
     * @param xml The XML string to convert to a map.
     * @return The map converted from XML.
     * @throws JsonProcessingException If an error occurs converting the XML
     *                                 to a map.
     */
    public static Map<?, ?> convertXmlToMap(String xml) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        return convertXmlToMap(xml, DEFAULT_XML_DESERIALIZATION_OPTIONS);
    }

    /**
     * Convert XML to a JSON string.
     *
     * @param xml     The XML string to convert to JSON.
     * @param options Options for conversion.
     * @return A JSON string.
     * @throws JsonProcessingException If an error occurs converting the XML
     *                                 to JSON.
     */
    public static String convertXmlToJson(String xml, XmlDeserializationOption[] options) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        return convertMapToJson(convertXmlToMap(xml, options));
    }

    /**
     * Convert XML to a JSON string using default options: {@link #DEFAULT_XML_DESERIALIZATION_OPTIONS}.
     *
     * @param xml The XML string to convert to JSON.
     * @return A JSON string.
     * @throws JsonProcessingException If an error occurs converting the XML
     *                                 to JSON.
     */
    public static String convertXmlToJson(String xml) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        return convertMapToJson(convertXmlToMap(xml, DEFAULT_XML_DESERIALIZATION_OPTIONS));
    }

    private static boolean isWrapRootEnabled(XmlDeserializationOption[] options) {
        return Arrays.asList(options).contains(XmlDeserializationOption.WRAP_ROOT);
    }

    private static boolean isRemoveArrayWrapperEnabled(XmlDeserializationOption[] options) {
        return Arrays.asList(options).contains(XmlDeserializationOption.REMOVE_ARRAY_WRAPPER);
    }

    /**
     * This removes the "wrapper key" for arrays that Jackson inserts.  The
     * purpose of removing the wrapper around the array is to maintain
     * consistency with another xml-to-json library that was previously in
     * use.
     * <p>
     * This is best described with an example:
     * Jackson will deserialize XML as:
     * <br><code>{"PERSON":{"NAMES":{"NAME":[{"FIRST_NAME":"First1"},{"FIRST_NAME":"First2"}]}}}</code><br>
     * and this method will convert it to (by removing the NAME wrapper around the array):
     * <br><code>{"PERSON":{"NAMES":[{"FIRST_NAME":"First1"},{"FIRST_NAME":"First2"}]}}</code>
     */
    public static <K, V> Map<K, V> removeXmlMapArrayWrappers(Map<K, V> map, Map<K, V> parent, K parentKey) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                removeXmlMapArrayWrappers((Map<K, V>) entry.getValue(), map, entry.getKey());
            } else if (parent != null && entry.getValue() instanceof List) {
                // This entry is an array so lets remove the wrapper by
                // replacing the parent with the array itself.
                parent.put(parentKey, entry.getValue());
            }
        }
        return map;
    }
}
