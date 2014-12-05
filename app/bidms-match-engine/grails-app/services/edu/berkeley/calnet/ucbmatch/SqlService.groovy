package edu.berkeley.calnet.ucbmatch

import grails.transaction.Transactional
import groovy.sql.Sql

import javax.sql.DataSource

@Transactional
class SqlService {
    DataSource datasource

    Sql getSqlInstance() {
        new Sql(datasource)

    }
}
