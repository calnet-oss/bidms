package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Name
import edu.berkeley.calnet.ucbmatch.database.Record
import edu.berkeley.calnet.ucbmatch.util.AttributeValueResolver
import grails.gorm.transactions.Transactional

@Transactional
class RowMapperService {

    MatchConfig matchConfig

    /**
     * Map row data to candidates. If a candidate already exists in the set of candidates, it is updated.
     * @param searchResults database rows
     * @param confidenceType canonical or potential
     * @param matchInput the match request
     * @return set of candidates that match
     */
    Set<Record> mapDataRowsToRecords(List<SearchResult> searchResults, ConfidenceType confidenceType, Map matchInput) {

        // Group all searchResults by the ruleName
        Map<String, List<SearchResult>> searchResultsByRuleName = searchResults.groupBy {it.ruleName }

        // Create a list of each referenceId and what ruleName that caused the match
        List<Map<String, String>> referenceIdByRuleName = searchResultsByRuleName.collectMany { ruleName, searchResults1 ->
            searchResults1*.rows.flatten().collect {
                [ruleName: ruleName, referenceId: it[matchConfig.matchReference.column]]
            }
        }
        // Group the list by referenceId
        Map<String, List<Map<String, String>>> referenceIdByRuleNameGrouped = referenceIdByRuleName.groupBy {
            it.referenceId
        }
        // For each group of referenceIds create a list of referenceIds and the rules that caused the match
        List<Map<String, List<String>>> ruleNamesByReferenceId = referenceIdByRuleNameGrouped.collect { String referenceId, def referenceByIdByRuleName ->
            [referenceId: referenceId, ruleNames: referenceByIdByRuleName*.ruleName.unique()]
        }

        // Collect a list of Record objects that holds the referenceId, the rules that caused the match, and what match type
        return ruleNamesByReferenceId.collect { new Record(ruleNames: it.ruleNames, referenceId: it.referenceId, exactMatch: confidenceType.exactMatch) }

//        Group the returned rows by referenceId. The value is a list of db rows returned
//        def Map<String, List<Map>> groupedByReferenceId = rows.groupBy { it[matchConfig.matchReference.column] }
//        def configByPath = matchConfig.matchAttributeConfigs.groupBy {
//            // Use output path, path or Root
//            it.outputPath?.capitalize() ?: it.path?.capitalize() ?: 'Root'
//        }
//
//        def candidates = groupedByReferenceId.inject([] as Set) { Set<Candidate> list, group ->
//            def referenceId = group.key
//            def groupRows = group.value
//            groupRows.each { dataRow ->
//                def systemOfRecord = getSystemOfRecordFromRow(dataRow)
//
//                // Start by find an existing candidate with the same referenceId and systemOfRecord
//                def candidate = list.find { it.referenceId == referenceId && it.systemOfRecord == systemOfRecord }
//                if (!candidate) {
//                    candidate = new Candidate(systemOfRecord: systemOfRecord, referenceId: referenceId, exactMatch: confidenceType.exactMatch)
//                    list << candidate
//                }
//                // Transfer data from database dataRow to candidate by dynamically calling mapRowToCandidate(Root|Identifiers|Names) methods
//                configByPath.each { path, pathConfigs ->
//                    "mapRowToCandidate$path"(candidate, dataRow, pathConfigs)
//                }
//
//                // If the candidate is an exact match, go through the runCrossCheck to test if some of the attributes does not match
//                if(candidate.exactMatch == ConfidenceType.CANONICAL.exactMatch) {
//                    runCrossCheckOnCandidate(candidate, dataRow, matchInput)
//                }
//            }
//            return list
//        }
//
//        return candidates
    }

    /**
     * If an incoming attribute (matchInput) is not part of the rule used for matching it can still
     * veto the exact match, if this attribute is marked with 'invalidates' and the attribute value is not
     * equals to the row value.
     *
     * Example:
     *
     * matchInput is firstName, lastName, dateOfBirth and ssn
     * matchRule is firstName EXACT, lastName EXACT and dateOfBirth EXACT
     *
     * the rule finds a row in the database where firstName, lastName and dateOfBirth matches, but
     * when crossChecking the candidate, the ssn from the row does not match the ssn in the matchInput.
     *
     * This will degrade the candidate to a potential match
     *
     * @param candidate
     * @param rowData
     * @param matchInput
     */
    private void runCrossCheckOnCandidate(Candidate candidate, Map rowData, Map matchInput) {
        def invalidatingAttributes = matchConfig.matchAttributeConfigs.findAll { it.invalidates }
        def hasInvalidatingAttribute = invalidatingAttributes.any { matchAttributeConfig ->
            def value = AttributeValueResolver.getAttributeValue(matchAttributeConfig, matchInput)
            def rowDataValue = rowData."${matchAttributeConfig.column}" ?: null
            return value != rowDataValue
        }

        if (hasInvalidatingAttribute) {
            candidate.exactMatch = ConfidenceType.POTENTIAL.exactMatch
        }
    }

    Candidate mapDataRowToCandidate(Map row, ConfidenceType confidenceType, Map matchInput) {
        try {
            def candidates = mapDataRowsToRecords([row] as Set, confidenceType, matchInput)
            return candidates?.size() == 1 ? candidates[0] : null
        } catch (ex) {
            log.error "Error mapping row to candidate", ex
            throw ex
        }

    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    private void mapRowToCandidateRoot(Candidate candidate, Map dataRow, List<MatchAttributeConfig> attributeConfigs) {
        attributeConfigs.each {
//            if (it.name == matchConfig.matchReference.systemOfRecordAttribute) {
//                return // Skip SystemOfRecord
//            }
            def value = dataRow[it.column]
            if (value) {
                candidate[it.attribute] = value
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    // Cannot be static because it is called dynamic
    private void mapRowToCandidateIdentifiers(Candidate candidate, Map dataRow, List<MatchAttributeConfig> attributeConfigs) {
        mapRowToGroupedAttribute(candidate.identifiers, dataRow, attributeConfigs, Identifier)
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    // Cannot be static because it is called dynamic
    private void mapRowToCandidateNames(Candidate candidate, Map row, List<MatchAttributeConfig> attributeConfigs) {
        mapRowToGroupedAttribute(candidate.names, row, attributeConfigs, Name)
    }

    private void mapRowToGroupedAttribute(Set candidateGroup, Map dataRow, List<MatchAttributeConfig> attributeConfigs, Class groupClass) {
        // Split the attributeConfigs up into groups identified by the group property
        def configGroups = attributeConfigs.groupBy { it.group }

        configGroups.inject(candidateGroup) { Set groupSet, configGroup ->
            // For each group property find the type
            def type = configGroup.key

            def attributeConfigsForType = configGroup.value
            // See if there is already an existing identifier
            if (groupSet.find { it.type == type }) {
                return groupSet
            }
            // Check if the group has any database values
            if (!attributeConfigsForType.any { dataRow[it.column] }) {
                return groupSet
            }
            // Create a new instance and mapDataRowsToRecords database values to the new group
            def group = [:]
            group.type = type
            attributeConfigsForType.each {
                def value = dataRow[it.column]
                if (value) {
                    group[it.attribute] = value
                }
            }
            groupSet << group
        }
    }

    private String getSystemOfRecordFromRow(Map<String, String> databaseRow) {
        def systemOfRecordAttribute = matchConfig.matchReference.systemOfRecordAttribute
        def attributeConfig = matchConfig.matchAttributeConfigs.find { it.name == systemOfRecordAttribute }
        return databaseRow?."${attributeConfig.column}"
    }
}
