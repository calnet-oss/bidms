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
package edu.berkeley.bidms.app.sgs.converter;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.core.convert.converter.Converter;

/**
 * Convert a basic XML string into a JSON object.  (Basic XML meaning only a
 * subset of XML is supported.)
 * <p>
 * Note that all syntax of XML doesn't translate directly into JSON so
 * translation restrictions apply.  This implementation currently utilizes
 * {@link XML#toJSONObject(String)} to perform the conversion.
 * <p>
 * Our idea of "basic XML" is something like {@code <KEYS><KEY1>VAL</KEY1><KEY2>VAL</KEY2></KEYS>}.
 */
public class XmlStringToJSONObjectConverter implements Converter<String, JSONObject> {
    @Override
    public JSONObject convert(String xml) {
        return xmlStringToJSONObject(xml);
    }

    public static JSONObject xmlStringToJSONObject(String xml) {
        return XML.toJSONObject(xml);
    }
}
