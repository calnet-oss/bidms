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
package edu.berkeley.bidms.app.sgs.executor;

import edu.berkeley.bidms.app.sgs.converter.ConversionErrorException;
import edu.berkeley.bidms.common.validation.ValidationException;
import edu.berkeley.bidms.common.validation.Validator;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.converter.Converter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A base class for representing data a "row" or an "entry" when iterating
 * SOR query results.
 *
 * @param <N> The native content data type.
 */
public abstract class QueryEntryContent<N> {
    @NotBlank
    private final String sorName;

    @NotBlank
    private final String sorObjKey;

    @NotNull
    private final OffsetDateTime queryTime;

    private final N nativeContent;

    private Converter<N, String> nativeToObjJsonConverter;

    private String objJson;

    protected QueryEntryContent(String sorName, String sorObjKey, OffsetDateTime queryTime, N nativeContent) {
        this.sorName = sorName;
        this.sorObjKey = sorObjKey;
        this.queryTime = queryTime;
        this.nativeContent = nativeContent;
    }

    /**
     * @param sorName                  The SOR name.
     * @param sorObjKey                The primary key of the entry within
     *                                 the SOR.
     * @param queryTime                The time the query was started.
     * @param nativeContent            Native content of the query entry.
     * @param nativeToObjJsonConverter A {@link Converter} to convert the
     *                                 native content to JSON stored in
     *                                 {@code objJson} column in the {@code
     *                                 SORObject} table.
     */
    public QueryEntryContent(String sorName, String sorObjKey, OffsetDateTime queryTime, N nativeContent, Converter<N, String> nativeToObjJsonConverter) {
        this(sorName, sorObjKey, queryTime, nativeContent);
        this.nativeToObjJsonConverter = nativeToObjJsonConverter;
    }

    /**
     * @return The SOR name.
     */
    public String getSorName() {
        return sorName;
    }

    /**
     * @return The primary key of the entry within the SOR.
     */
    public String getSorObjKey() {
        return sorObjKey;
    }

    /**
     * @return The time the query was started.
     */
    public OffsetDateTime getQueryTime() {
        return queryTime;
    }

    /**
     * @return Native content of the query entry.
     */
    public N getNativeContent() {
        return nativeContent;
    }

    /**
     * @param nativeToObjJsonConverter A {@link Converter} to convert the
     *                                 native content to JSON stored in
     *                                 {@code objJson} column in the {@code
     *                                 SORObject} table.
     */
    public void setNativeToObjJsonConverter(Converter<N, String> nativeToObjJsonConverter) {
        this.nativeToObjJsonConverter = nativeToObjJsonConverter;
    }

    /**
     * Using the {@code nativeToObjJsonConverter}, converts the native
     * content to a JSON string suitable for storage in the {@code objJson}
     * column in the {@code SORObject} table.
     *
     * @return A JSON string.
     * @throws ConversionException If an error occurred while converting the
     *                             native content to JSON.
     */
    public String getObjJson() throws ConversionException {
        if (objJson == null) {
            if (nativeToObjJsonConverter != null) {
                this.objJson = nativeToObjJsonConverter.convert(getNativeContent());
            } else {
                throw new ConversionErrorException("objJson is null and there is no nativeToObjJsonConverter set");
            }
        }
        return objJson;
    }

    /**
     * Sets the JSON string.  Note that normally the objJson is generated
     * using the {@code nativeToObjJsonConverter}, which happens within
     * {@link #getObjJson()} if objJson is null, rather than this method
     * being called.
     *
     * @param objJson A JSON string suitable for storage in the {@code
     *                objJson} column in the {@code SORObject} table.
     */
    public void setObjJson(String objJson) {
        this.objJson = objJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryEntryContent)) return false;
        QueryEntryContent<?> that = (QueryEntryContent<?>) o;
        return sorName.equals(that.sorName) &&
                sorObjKey.equals(that.sorObjKey) &&
                queryTime.equals(that.queryTime) &&
                nativeContent.equals(that.nativeContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sorName, sorObjKey, queryTime, nativeContent);
    }

    /**
     * @param validator Validate this object using a validator.
     * @throws ValidationException If a validation error occurred.
     */
    public void validate(Validator validator) throws ValidationException {
        validator.validate(this);
    }

    /**
     * @return A full distinguishing (unique) identifier for a SOR object. In
     * many cases this will be the same as the {@code sorObjKey} but in some
     * cases there may be a fuller form of the primary key.  For example, in
     * LDAP, we may use {@code uid} as the sorObjKey but the full identifier
     * is the DN.
     */
    public abstract String getFullIdentifier();
}
