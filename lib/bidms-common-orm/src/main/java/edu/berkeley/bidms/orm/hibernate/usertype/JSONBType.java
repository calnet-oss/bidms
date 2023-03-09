/*
 * Copyright (c) 2015, Regents of the University of California and
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
package edu.berkeley.bidms.orm.hibernate.usertype;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * A {@link UserType} that persists objects as JSONB.  Supports both
 * PostgreSQL and H2.
 */
public class JSONBType implements UserType {

    // Needed to support PostgreSQL.  Will be null if not using PostgreSQL.
    static Class<?> pgObjectClass;

    static Class<?> h2DriverClass;

    static {
        try {
            pgObjectClass = Class.forName("org.postgresql.util.PGobject");
        } catch (Exception ignored) {
            // PostgreSQL class not available
        }
        try {
            h2DriverClass = Class.forName("org.h2.Driver");
        }
        catch(Exception ignored) {
            // H2DB driver not available
        }
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{h2DriverClass == null ? Types.OTHER : Types.JAVA_OBJECT};
    }

    @Override
    public Class<?> returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        if (names != null && names.length > 0) {
            Object result = rs.getObject(names[0]);
            // Support PostgreSQL: See if PGobject.  (It will be if this is
            // a JSONB column.)
            if (result != null && pgObjectClass != null && pgObjectClass.isAssignableFrom(result.getClass())) {
                // is an instanceof PGobject.  Need to return
                // PGobject.getValue().
                try {
                    result = result.getClass().getMethod("getValue").invoke(result);
                } catch (Exception e) {
                    throw new RuntimeException("Couldn't invoke getValue() on instance of " + result.getClass().getName(), e);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        st.setObject(index, value, (value == null) ? Types.NULL : h2DriverClass == null ? Types.OTHER : Types.JAVA_OBJECT);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) return null;
        return new String((String) value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
