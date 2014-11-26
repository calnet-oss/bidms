package edu.berkeley.calnet.ucbmatch

import grails.transaction.Transactional
import groovy.sql.Sql

import javax.sql.DataSource

@Transactional
class PersonService {
    DataSource datasource

    def serviceMethod() {
        def sql = new Sql(datasource)

        sql.rows("select * from matchgrid").collect()
    }
}
