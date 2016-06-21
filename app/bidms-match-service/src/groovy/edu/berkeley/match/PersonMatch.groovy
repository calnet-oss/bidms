package edu.berkeley.match

import edu.berkeley.registry.model.Person

abstract class PersonMatch {
}

class PersonNoMatch extends PersonMatch {
    /**
     * If true, then matchOnly flag was true on match input, meaning this
     * person should not go to the newUid queue.  This happens when we
     * receive data about a person from a SOR where the "SOR" really isn't
     * the true System of Record for the person.  Example: Employees in
     * Campus Solutions that were imported from HCM.
     */
    Boolean matchOnly
}

class PersonExactMatch extends PersonMatch {
    Person person
}

// indicates sorobject already matched up
class PersonExistingMatch extends PersonMatch {
    Person person
}

class PersonPartialMatches extends PersonMatch {
    List<Person> people
}