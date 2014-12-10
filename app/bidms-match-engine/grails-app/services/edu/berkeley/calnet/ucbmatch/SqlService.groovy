package edu.berkeley.calnet.ucbmatch

import groovy.sql.Sql

import javax.sql.DataSource

class SqlService {
    static transactional = false

    DataSource datasource

    Sql getSqlInstance() {
        new Sql(datasource)

    }
}
