package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import grails.transaction.Transactional
import groovy.sql.Sql

import javax.sql.DataSource

@Transactional
class PersonService {
    DataSource datasource
    MatchConfig matchConfig
    def serviceMethod() {
        def sql = new Sql(datasource)
        println matchConfig
        sql.rows("select * from matchgrid").collect()
    }
}
