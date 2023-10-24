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
package edu.berkeley.bidms.app.registryModel.model.type;

public enum MatchHistoryResultTypeEnum {
    /**
     * sorObject matched to an existing uid
     */
    EXACT,

    /**
     * sorObject potentially matches to one or more uids and further manual reconciliation is to be done to resolve
     */
    POTENTIAL,

    /**
     * no match and new uid assignment is disabled: sorObject created with no uid
     */
    NONE_MATCH_ONLY,

    /**
     * new uid should be assigned but was deferred for later assignment
     */
    NONE_NEW_UID_DEFERRED,

    /**
     * new uid has been assigned
     */
    NONE_NEW_UID,

    /**
     * When new uid is actually assigned.
     * This could be at the same time of match, but it can also be deferred and done later by another process.
     * When done at the same time as a match, there will be two MatchHistory rows inserted at about the same time.
     * First would be a row with NONE_NEW_UID type inserted by match-service and the
     * second would be a row with this NEW_UID type inserted by the registry-provisioning service responsible for creating new uids.
     */
    NEW_UID,

    /**
     * During manual reconciliation, the sorObject was matched to an existing uid.
     */
    RECONCILIATION_MATCH,

    /**
     * During manual reconciliation, the sorObject was marked to be assigned a new uid.
     * The new uid isn't actually assigned in this step, but rather it is sent back
     * through the match-service and a NEW_UID row should appear after this.
     */
    RECONCILIATION_NEW_UID_DEFERRED,

    /**
     * During manual reconciliation, the potential match is rejected.
     */
    RECONCILIATION_REJECT,

    /**
     * During manual reconciliation, the potential match is unrejected.
     */
    RECONCILIATION_UNREJECT,

    /**
     * When a sorObject is split off an existing uid by removing the uid from the sorObject.
     */
    SPLIT_REMOVE_UID,

    /**
     * When a sorObject is swapped between two uids.
     * Note: This may be a candidate for removal due to lack of implementation upstream.
     */
    SWAP,

    /**
     * When a sorObject is moved to another uid due to merging of two uids into one.
     */
    MERGE
}
