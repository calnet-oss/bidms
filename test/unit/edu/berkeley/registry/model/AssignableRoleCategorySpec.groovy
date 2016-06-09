package edu.berkeley.registry.model

import edu.berkeley.registry.model.AssignableRoleCategory
import edu.berkeley.calnet.groovy.transform.LogicalEqualsAndHashCodeInterface
import spock.lang.Specification

class AssignableRoleCategorySpec extends Specification {
    void "confirm AssignableRoleCategory using LogicalEqualsAndHashCode annotation"() {
        given:
            AssignableRoleCategory obj = new AssignableRoleCategory()
        expect:
            obj instanceof LogicalEqualsAndHashCodeInterface
    }
}
