package edu.berkeley.match

import edu.berkeley.registry.model.Person

abstract class PersonMatch {
}

class PersonNoMatch extends PersonMatch {}

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