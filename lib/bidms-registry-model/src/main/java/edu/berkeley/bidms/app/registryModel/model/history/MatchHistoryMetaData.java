/*
 * Copyright (c) 2023, Regents of the University of California and
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
package edu.berkeley.bidms.app.registryModel.model.history;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MatchHistoryMetaData {
    /**
     * Populated for an exact match.
     */
    private MatchHistoryExactMatch exactMatch;

    /**
     * Total number of uids that the sorObject potentially matched to.
     * Normally, this will match the size of the potentialMatches list size,
     * but if the potentialMatches list has to be truncated, this count is
     * the number of potential matches before truncation.
     */
    private Integer fullPotentialMatchCount;

    /**
     * Populated for partial matches.
     * Potential matches are uids the sorObject potentially matches to.
     * It's possible an accidental bad match rule produces a potential match
     * to a great many uids, so calling code should either error out if too
     * many matches found and/or should truncate this list to avoid the
     * possibility of an overly large list being persisted to the database.
     */
    @Size(max = 64)
    private List<MatchHistoryPartialMatch> potentialMatches;

    private MatchHistoryUnassignment unassignment;

    public MatchHistoryExactMatch getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(MatchHistoryExactMatch exactMatch) {
        this.exactMatch = exactMatch;
    }

    public Integer getFullPotentialMatchCount() {
        return fullPotentialMatchCount;
    }

    public void setFullPotentialMatchCount(Integer fullPotentialMatchCount) {
        this.fullPotentialMatchCount = fullPotentialMatchCount;
    }

    public List<MatchHistoryPartialMatch> getPotentialMatches() {
        return potentialMatches;
    }

    public void setPotentialMatches(List<MatchHistoryPartialMatch> potentialMatches) {
        this.potentialMatches = potentialMatches;
    }

    public MatchHistoryUnassignment getUnassignment() {
        return unassignment;
    }

    public void setUnassignment(MatchHistoryUnassignment unassignment) {
        this.unassignment = unassignment;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class MatchHistoryExactMatch {
        /**
         * list of rule names that resulted in the exact match
         */
        private List<String> ruleNames;

        public List<String> getRuleNames() {
            return ruleNames;
        }

        public void setRuleNames(List<String> ruleNames) {
            this.ruleNames = ruleNames;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class MatchHistoryPartialMatch {
        private String potentialMatchToUid;

        /**
         * list of rule names that resulted in the partial match
         */
        private List<String> ruleNames;

        public String getPotentialMatchToUid() {
            return potentialMatchToUid;
        }

        public void setPotentialMatchToUid(String potentialMatchToUid) {
            this.potentialMatchToUid = potentialMatchToUid;
        }

        public List<String> getRuleNames() {
            return ruleNames;
        }

        public void setRuleNames(List<String> ruleNames) {
            this.ruleNames = ruleNames;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class MatchHistoryUnassignment {
        /**
         * The uid that was removed from the SORObject when the split
         * occurred or the previous uid that was assigned when a new uid was
         * assigned during a merge.
         */
        private String unassignedUid;

        public String getUnassignedUid() {
            return unassignedUid;
        }

        public void setUnassignedUid(String unassignedUid) {
            this.unassignedUid = unassignedUid;
        }
    }
}
