/*
 * Copyright (c) 2020, Regents of the University of California and
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
// Note: DISTANCE is not supported when using H2DB in test environment
import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.EXACT
import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.FIXED_VALUE
import static edu.berkeley.bidms.app.matchengine.config.MatchConfig.MatchType.SUBSTRING

matchTable('MatchView')

referenceId {
    responseType = 'enterprise'
    column = 'uid'
    systemOfRecordAttribute = 'identifiersor'
    identifierAttribute = 'identifier'
}

attributes {
    identifiersor {
        description = 'System of Record'
        column = 'identifiersor'
        isPrimaryKeyColumn = 'idissorprimarykey'
        attribute = 'systemOfRecord'
        search {
            caseSensitive = true
        }
    }
    identifier {
        description = 'System Of Record Identifier'
        column = 'identifier'
        attribute = 'identifier'
        group = 'sor'
        outputPath = 'identifiers'
        search {
            caseSensitive = true
        }
    }
    givenName {
        description = 'Given Name (official)'
        column = 'givenname'
        path = 'names'
        attribute = 'givenName'
        group = 'official'
        search {
            // this matches on the first character only (i.e., "initial" of the givenName)
            substring = [from: 1, length: 1]
            caseSensitive = false
            distance = 2
        }
    }
    surName {
        description = 'Sur Name (official)'
        column = 'surname'
        path = 'names'
        attribute = 'surName'
        group = 'official'
        search {
            caseSensitive = false
            distance = 2
        }
    }
    dateOfBirth {
        description = 'Date of Birth'
        column = 'birthdate'
        attribute = 'dateOfBirth'
        invalidates = true
        search {
            dateFormat = 'yyyy-MM-dd'
        }
    }
    employeeId {
        description = 'Employee ID'
        column = 'identifier'
        path = 'identifiers'
        attribute = 'identifier'
        group = 'employeeId'
        invalidates = true
        search {
            caseSensitive = true
        }
    }
    employeeIdType {
        description = 'Employee ID Type name'
        column = 'identifiertype'
        path = 'identifiers'
        attribute = 'identifier'
        group = 'employeeId'
        invalidates = true
        search {
            caseSensitive = true
            fixedValue = 'employeeId'
        }
    }
    studentId {
        description = 'Student ID'
        column = 'identifier'
        path = 'identifiers'
        attribute = 'identifier'
        group = 'studentId'
        search {
            caseSensitive = true
        }
    }
    studentIdType {
        description = 'Student ID Type name'
        column = 'identifiertype'
        path = 'identifiers'
        attribute = 'identifier'
        group = 'studentId'
        search {
            caseSensitive = true
            fixedValue = 'studentId'
        }
    }
    secondaryToEmployee {
        description = 'Incoming match data is from PAYROLL_SECONDARY, matchee is PAYROLL'
        column = 'identifiersor'
        isPrimaryKeyColumn = 'idissorprimarykey'
        attribute = 'systemOfRecord'
        input {
            fixedValue = 'PAYROLL_SECONDARY'
        }
        search {
            caseSensitive = true
            fixedValue = 'PAYROLL'
        }
    }
    phoneNumber {
        description = 'Phone number'
        column = 'phoneNumber'
        attribute = 'phoneNumber'
        path = 'phoneNumbers'
        input {
            stringList = true
        }
    }
    emailAddress {
        description = 'Email address'
        column = 'emailAddress'
        attribute = 'emailAddress'
        path = 'emailAddresses'
        input {
            stringList = true
        }
        search {
            caseSensitive = false
        }
    }
}

confidences {
    superCanonical 'SUPERCANONICAL_EMPLOYEE_ID', secondaryToEmployee: FIXED_VALUE, employeeId: EXACT, employeeIdType: FIXED_VALUE

    canonical 'CANONICAL_EXACT_NAME_DOB', givenName: EXACT, surName: EXACT, dateOfBirth: EXACT

    // givenName: SUBSTRING is configured to match on first character of the givenName only
    canonical 'CANONICAL_FIRSTNAME_INITIAL_EXACT_LASTNAME_STUDENTID', givenName: SUBSTRING, surName: EXACT, studentId: EXACT, studentIdType: FIXED_VALUE
    canonical 'CANONICAL_FIRSTNAME_INITIAL_EXACT_LASTNAME_EMPLOYEEID', givenName: SUBSTRING, surName: EXACT, employeeId: EXACT, employeeIdType: FIXED_VALUE

    canonical 'CANONICAL_EXACT_LASTNAME_DOB_STUDENTID', surName: EXACT, dateOfBirth: EXACT, studentId: EXACT, studentIdType: FIXED_VALUE
    canonical 'CANONICAL_EXACT_LASTNAME_DOB_EMPLOYEEID', surName: EXACT, dateOfBirth: EXACT, employeeId: EXACT, employeeIdType: FIXED_VALUE

    potential 'POTENTIAL_FIRSTNAME_INITIAL_EXACT_LASTNAME_DOB', givenName: SUBSTRING, surName: EXACT, dateOfBirth: EXACT

    potential 'POTENTIAL_EXACT_LASTNAME_PHONE', surName: EXACT, phoneNumber: EXACT
    potential 'POTENTIAL_EXACT_LASTNAME_EMAIL', surName: EXACT, emailAddress: EXACT
}
