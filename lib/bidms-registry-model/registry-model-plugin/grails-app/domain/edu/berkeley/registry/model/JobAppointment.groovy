package edu.berkeley.registry.model

import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCode
import edu.berkeley.util.domain.transform.ConverterConfig

@ConverterConfig(excludes = ["person", "sorObject", "person_"])
@LogicalEqualsAndHashCode(
        excludes = ["id", "belongsTo", "constraints", "mapping", "transients", "version", "person", "person_"],
        changeCallbackClass = JobAppointmentHashCodeChangeCallback
)
class JobAppointment extends PersonAppointment {

    static class JobAppointmentHashCodeChangeCallback extends PersonCollectionHashCodeChangeHandler<JobAppointment> {
        JobAppointmentHashCodeChangeCallback() {
            super("jobAppointments")
        }
    }

    String jobCode
    String jobTitle
    String deptCode
    String deptName
    Date hireDate

    private Person person_

    // After testing it looks like having person here, as well as in the superclass, is necessary, since Person has a set of JobAppointments.
    static constraints = {
        jobCode size: 0..64, nullable: true
        jobTitle size: 0..255, nullable: true
        deptCode size: 0..64, nullable: true
        deptName size: 0..255, nullable: true
        hireDate nullable: true
    }

    static mapping = {
        table name: "JobAppointment"
        version false
        id column: 'apptId', generator: 'foreign', params: [property: 'id'], sqlType: 'BIGINT'
        // workaround for GORM bug when using inheritance/table-per-subclass
        person_ column: PersonAppointment.getUidColumnName(), sqlType: 'VARCHAR(64)'
        jobCode column: 'jobCode', sqlType: 'VARCHAR(64)'
        jobTitle column: 'jobTitle', sqlType: 'VARCHAR(255)'
        deptCode column: 'deptCode', sqlType: 'VARCHAR(64)'
        deptName column: 'deptName', sqlType: 'VARCHAR(255)'
        hireDate column: 'hireDate', sqlType: 'DATE'
    }

    int compareTo(obj) {
        return hashCode() <=> obj?.hashCode()
    }

    // workaround for GORM bug when using inheritance/table-per-subclass
    Person getPerson_() { return person }

    // workaround for GORM bug when using inheritance/table-per-subclass
    void setPerson_(Person p) {
        super.setPerson(p)
        this.person_ = p
    }

    // workaround for GORM bug when using inheritance/table-per-subclass
    void setPerson(Person p) {
        super.setPerson(p)
        this.person_ = p
    }
}
