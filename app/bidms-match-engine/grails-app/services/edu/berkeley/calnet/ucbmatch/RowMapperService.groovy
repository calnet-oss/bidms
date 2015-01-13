package edu.berkeley.calnet.ucbmatch

import edu.berkeley.calnet.ucbmatch.config.MatchAttributeConfig
import edu.berkeley.calnet.ucbmatch.config.MatchConfig
import edu.berkeley.calnet.ucbmatch.database.Candidate
import edu.berkeley.calnet.ucbmatch.database.Name
import edu.berkeley.calnet.ucbmatch.util.AttributeValueResolver
import grails.transaction.Transactional

@Transactional
class RowMapperService {

    MatchConfig matchConfig

    Set<Candidate> mapDataRowsToCandidates(Set rows, ConfidenceType confidenceType, Map matchInput) {
        // Group the returned rows by referenceId. The value is a list of db rows returned
        def Map<String, List<Map>> groupedByReferenceId = rows.groupBy { it.reference_id }

        def configByPath = matchConfig.matchAttributeConfigs.groupBy {
            // Use output path, path or Root
            it.outputPath?.capitalize() ?: it.path?.capitalize() ?: 'Root'
        }

        def candidates = groupedByReferenceId.inject([] as Set) { Set<Candidate> list, group ->
            def referenceId = group.key
            def groupRows = group.value
            groupRows.each { dataRow ->
                def systemOfRecord = getSystemOfRecordFromRow(dataRow)

                // Start by find an existing candidate with the same referenceId and systemOfRecord
                def candidate = list.find { it.referenceId == referenceId && it.systemOfRecord == systemOfRecord }
                if (!candidate) {
                    candidate = new Candidate(systemOfRecord: systemOfRecord, referenceId: referenceId, exactMatch: confidenceType.exactMatch)
                    list << candidate
                }
                // Transfer data from database dataRow to candidate by dynamically calling mapRowToCandidate(Root|Identifiers|Names) methods
                configByPath.each { path, pathConfigs ->
                    "mapRowToCandidate$path"(candidate, dataRow, pathConfigs)
                }
                if(candidate.exactMatch == ConfidenceType.CANONICAL.exactMatch) {
                    runCrossCheckOnCandidate(candidate, dataRow, matchInput)
                }
            }
            return list
        }

        return candidates
    }

    /**
     *
     * @param candidate
     * @param rowData
     * @param matchInput
     */
    private void runCrossCheckOnCandidate(Candidate candidate, Map rowData, Map matchInput) {
        def invalidatingAttributes = matchConfig.matchAttributeConfigs.findAll { it.invalidates }
        def hasInvalidatingAttribute = invalidatingAttributes.any { matchAttributeConfig ->
            def value = AttributeValueResolver.getAttributeValue(matchAttributeConfig, matchInput)
            def rowDataValue = rowData."${matchAttributeConfig.column}"
            return value != rowDataValue
        }

        if(hasInvalidatingAttribute) {
            candidate.exactMatch = ConfidenceType.POTENTIAL.exactMatch
        }
    }

    Candidate mapDataRowToCandidate(Map row, ConfidenceType confidenceType) {
        def candidates = mapDataRowsToCandidates([row] as Set, confidenceType)
        return candidates?.size() == 1 ? candidates[0] : null

    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    private void mapRowToCandidateRoot(Candidate candidate, Map dataRow, List<MatchAttributeConfig> attributeConfigs) {
        attributeConfigs.each {
            if (it.name == matchConfig.matchReference.systemOfRecordAttribute) {
                return // Skip SystemOfRecord
            }
            def value = dataRow[it.column]
            if (value) {
                candidate[it.attribute] = value
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"]) // Cannot be static because it is called dynamic
    private void mapRowToCandidateIdentifiers(Candidate candidate, Map dataRow, List<MatchAttributeConfig> attributeConfigs) {
        mapRowToGroupedAttribute(candidate.identifiers, dataRow, attributeConfigs, Identifier)
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"]) // Cannot be static because it is called dynamic
    private void mapRowToCandidateNames(Candidate candidate, Map row, List<MatchAttributeConfig> attributeConfigs) {
        mapRowToGroupedAttribute(candidate.names, row, attributeConfigs, Name)
    }

    private static void mapRowToGroupedAttribute(Set candidateGroup, Map dataRow, List<MatchAttributeConfig> attributeConfigs, Class groupClass) {
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
            // Create a new instance and mapDataRowsToCandidates database values to the new group
            def group = groupClass.newInstance()
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
