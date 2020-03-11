package edu.berkeley.hibernate.dialect

import org.hibernate.dialect.PostgreSQLDialect

import java.sql.Types

/**
 * Fixes the issue where the dialect expect int2 but the database returns smallint etc...
 */
class PostgreSQLFixDialect extends PostgreSQLDialect {
    public PostgreSQLFixDialect() {
        super();
        registerColumnType( Types.BIGINT, "bigint" );
        registerColumnType( Types.INTEGER, "integer" );
        registerColumnType( Types.SMALLINT, "smallint" );
        registerColumnType( Types.TINYINT, "smallint" );
    }
}
