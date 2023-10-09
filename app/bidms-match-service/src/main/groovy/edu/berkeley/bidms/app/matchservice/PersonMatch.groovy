/*
 * Copyright (c) 2015, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.matchservice

import edu.berkeley.bidms.app.registryModel.model.Person
import groovy.transform.Canonical

abstract class PersonMatch {
    String eventId
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
    String eventId
    Person person
    List<String> ruleNames

    @Override
    String toString() {
        return "PersonPartialMatch(uid=${person.uid}, ruleNames=$ruleNames)"
    }
}