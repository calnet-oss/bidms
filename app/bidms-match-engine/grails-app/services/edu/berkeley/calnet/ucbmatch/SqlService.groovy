package edu.berkeley.calnet.ucbmatch

import grails.transaction.Transactional
import groovy.sql.Sql

import javax.sql.DataSource

@Transactional
class SqlService {
    DataSource dataSource_functionalDS

    Sql getSqlInstance() {
        new Sql(dataSource_functionalDS)
    }
}
