package edu.berkeley.match

import edu.berkeley.registry.model.Person
import groovy.transform.Canonical

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

    @Override
    String toString() {
        return "PersonNoMatch()"
    }
}

@Canonical
class PersonExactMatch extends PersonMatch {
    Person person
    List<String> ruleNames

    @Override
    String toString() {
        return "PersonExactMatch(uid=${person.uid}, ruleNames=$ruleNames)"
    }
}

// indicates sorobject already matched up
@Canonical
class PersonExistingMatch extends PersonMatch {
    Person person

    @Override
    String toString() {
        return "PersonExistingMatch(uid=${person.uid})"
    }
}

@Canonical
class PersonPartialMatches extends PersonMatch {
    List<PersonPartialMatch> partialMatches

    @Override
    String toString() {
        return "PersonPartialMatches(partialMatches=${partialMatches})"
    }
}

@Canonical
class PersonPartialMatch {
    Person person
    List<String> ruleNames

    @Override
    String toString() {
        return "PersonPartialMatch(uid=${person.uid}, ruleNames=$ruleNames)"
    }
}