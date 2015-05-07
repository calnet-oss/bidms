package edu.berkeley.hibernate.usertype;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


/**
 * A {@link UserType} that persists objects as JSONB.
 * <p/>
 * static mapping = {
 * data type: JSONBType, sqlType: 'jsonb'
 * }
 */
public class JSONBType implements UserType {

    // Needed to support PostgreSQL.  Will be null if not using PostgreSQL.
    static Class<?> pgObjectClass;

    static {
        try {
            pgObjectClass = Class.forName("org.postgresql.util.PGobject");
        } catch (Exception e) {
            // PostgreSQL class not available
        }
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.OTHER};
    }

    @Override
    public Class<?> returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return ObjectUtils.equals(x, y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
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
            } else {
                // is not an instanceof PGobject.  Return the object as-is.
                return result;
            }
        }
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        st.setObject(index, value, (value == null) ? Types.NULL : Types.OTHER);
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
