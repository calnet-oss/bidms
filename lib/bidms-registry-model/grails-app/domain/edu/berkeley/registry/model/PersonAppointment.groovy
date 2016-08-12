package edu.berkeley.registry.model

import edu.berkeley.util.domain.DomainUtil
import org.hibernate.FetchMode

abstract class PersonAppointment implements Comparable {
    Long id
    AppointmentType apptType
    SORObject sorObject
    String apptIdentifier
    boolean isPrimaryAppt
    Date beginDate
    Date endDate

    static belongsTo = [person: Person]

    static constraints = {
        person unique: ['sorObject', 'apptType', 'apptIdentifier']
        apptIdentifier size: 1..64
        beginDate nullable: true
        endDate nullable: true
    }

    static mapping = {
        tablePerHierarchy false // table-per-subclass model
        table name: "PersonAppointment"
        version false
        id column: 'id', generator: 'sequence', params: [sequence: 'PersonAppointment_seq'], sqlType: 'BIGINT'
        apptType column: 'apptTypeId', sqlType: 'SMALLINT', fetch: FetchMode.JOIN
        person column: PersonAppointment.getUidColumnName(), sqlType: 'VARCHAR(64)'
        sorObject column: 'sorObjectId', sqlType: 'BIGINT'
        apptIdentifier column: 'apptIdentifier', sqlType: 'VARCHAR(64)'
        isPrimaryAppt column: 'isPrimaryAppt', sqlType: 'BOOLEAN'
        beginDate column: 'apptBeginDate', sqlType: 'DATE'
        endDate column: 'apptEndDate', sqlType: 'DATE'
    }

    // Makes the column name unique in test mode to avoid GRAILS-11600
    // 'unique' bug.  See https://jira.grails.org/browse/GRAILS-11600 and
    // comments in DomainUtil.
    static String getUidColumnName() {
        return DomainUtil.testSafeColumnName("PersonAppointment", "uid")
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }
}
