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
package edu.berkeley.bidms.app.sgs.executor.jdbc;

import edu.berkeley.bidms.app.sgs.config.properties.SorConfigProperties;
import edu.berkeley.bidms.app.sgs.converter.XmlStringToJSONObjectConverter;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractor;
import edu.berkeley.bidms.app.sgs.executor.EntryContentExtractorException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Extract content from a JDBC {@link Clob} column.
 */
public class ClobColumnExtractor implements EntryContentExtractor<Clob, JdbcQueryRowContent> {

    /**
     * Extracts a particular {@link Clob} column from the current {@link
     * ResultSet} row.
     *
     * @param sorConfig SOR configuration.
     * @param objIdx    The JDBC column index in the rs row.  i.e., this is
     *                  used for {@code rs.getClob(objIdx)}.
     * @param rs        The current {@link ResultSet} row.
     * @return An instance of {@link JdbcQueryRowContent} containing the
     * extracted row data.
     * @throws EntryContentExtractorException If an error occurred.
     */
    public JdbcQueryRowContent extractContent(SorConfigProperties sorConfig, int objIdx, ResultSet rs) throws EntryContentExtractorException {
        try {
            Clob clob = rs.getClob(objIdx);
            try {
                return extractContent(sorConfig, null, clob);
            } finally {
                clob.free();
            }
        } catch (SQLException e) {
            throw new EntryContentExtractorException(e);
        }
    }

    /**
     * Extracts an XML string from a JDBC {@link Clob} column.  This clob
     * string must adhere to a particular syntax format, given below.
     * <p>
     * Oracle SQL should look something like: {@code SELECT
     * XMLElement("QUERY", ...).getCLOBVal() FROM ... } The important aspect
     * of the above example is the {@code getCLOBVal()}.  Otherwise, a
     * significant Oracle-server-side memory leak can occur when iterating a
     * large number of rows.
     *
     * <p>
     * Required convention of the Clob string:
     * <pre>{@code
     *   <QUERY>
     *     <SORNAME>%SORNAME%</SORNAME>
     *     <VERSION>JSONVERSION</VERSION>
     *     <QUERYTIME>YYYY-MM-DD HH24:MI:SS.FF3 TZHTZM</QUERYTIME>
     *     <SOROBJKEY></SOROBJKEY>
     *     <%SORNAME%>
     *       ...XML content...
     *     </%SORNAME%>
     *   </QUERY>
     * }</pre>
     * <p>
     * Date string example: {@code 2020-01-23 11:47:26.394 -0800}
     *
     * @param sorConfig A {@link SorConfigProperties} instance that contains
     *                  configuration for the SOR.
     * @param notUsed   This parameter is ignored for this implementation
     *                  because the query time should be embedded within the
     *                  content itself (see content convention above).
     * @param clob      The {@link Clob} containing the content.
     * @return An instance of {@link JdbcQueryRowContent} containing the
     * extracted row data.
     * @throws EntryContentExtractorException If an error occurred.
     */
    @Override
    public JdbcQueryRowContent extractContent(SorConfigProperties sorConfig, OffsetDateTime notUsed, Clob clob) throws EntryContentExtractorException {
        try {
            Long clobLengthAsLong = clob.length();
            if (clobLengthAsLong > Integer.MAX_VALUE) {
                throw new EntryContentExtractorException("The content length is " + clobLengthAsLong + " characters which is greater than max supported character length of " + Integer.MAX_VALUE);
            }
            int clobLength = clobLengthAsLong.intValue();
            char[] buf = new char[4096];
            try (StringWriter writer = new StringWriter(clobLength)) {
                try (Reader reader = clob.getCharacterStream()) {
                    for (int numRead = 0; numRead >= 0; numRead = reader.read(buf)) {
                        writer.write(buf, 0, numRead);
                    }
                }
                String xmlString = writer.toString();

                // CLOB string must follow the XML syntax convention specified in the method Javadoc.
                try {
                    JSONObject jsonObj = XmlStringToJSONObjectConverter.xmlStringToJSONObject(xmlString);
                    if (!jsonObj.has("QUERY")) {
                        throw new EntryContentExtractorException("Could not find QUERY key in the json: " + jsonObj.toString(4));
                    }
                    JSONObject query = jsonObj.getJSONObject("QUERY");
                    if (!query.has("SORNAME")) {
                        throw new EntryContentExtractorException("Could not find QUERY.SORNAME key in the json: " + jsonObj.toString(4));
                    }
                    if (!query.has("QUERYTIME")) {
                        throw new EntryContentExtractorException("Could not find QUERY.QUERYTIME key in the json: " + jsonObj.toString(4));
                    }
                    if (!query.has("SOROBJKEY")) {
                        throw new EntryContentExtractorException("Could not find QUERY.SOROBJKEY key in the json: " + jsonObj.toString(4));
                    }
                    if (!query.has(sorConfig.getSorName())) {
                        throw new EntryContentExtractorException("Could not find QUERY." + sorConfig.getSorName() + " key in the json: " + jsonObj.toString(4));
                    }
                    String sorName = query.getString("SORNAME");
                    OffsetDateTime queryTime = convertQueryTimeString(query.getString("QUERYTIME"));
                    String sorObjKey = query.get("SOROBJKEY").toString();
                    JSONObject sorJsonObj = query.getJSONObject(sorName);
                    return new JdbcQueryRowContent(sorName, sorObjKey, queryTime, sorJsonObj);
                } catch (RuntimeException e) {
                    throw new EntryContentExtractorException("Could not convert query row content to JSON", e);
                }
            } catch (IOException e) {
                throw new EntryContentExtractorException(e);
            }
        } catch (SQLException e) {
            throw new EntryContentExtractorException(e);
        }
    }

    private OffsetDateTime convertQueryTimeString(String queryTimeString) {
        // In Oracle, to produce the timestamp: to_char(systimestamp, 'YYYY-MM-DD HH24:MI:SS.FF3 TZHTZM')
        return OffsetDateTime.parse(queryTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS X"));
    }
}
