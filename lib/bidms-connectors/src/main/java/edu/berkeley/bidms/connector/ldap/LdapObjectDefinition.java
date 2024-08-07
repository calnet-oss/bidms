/*
 * Copyright (c) 2017, Regents of the University of California and
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
package edu.berkeley.bidms.connector.ldap;

import edu.berkeley.bidms.connector.ObjectDefinition;
import org.springframework.ldap.query.LdapQuery;

public interface LdapObjectDefinition extends ObjectDefinition {
    /**
     * The globally unique identifier attribute in the directory, which is
     * typically an operational attribute.
     * <p>
     * This is often entryUUID in LDAP directories and objectGUID in Active
     * Directory directories.
     *
     * @return The globally unique identifier attribute name.
     */
    String getGloballyUniqueIdentifierAttributeName();

    /**
     * The primary key attribute in the directory.  For a person, this is
     * often uid.
     *
     * @return The primary key attribute name.
     */
    String getPrimaryKeyAttributeName();

    /**
     * Get a Spring LdapQuery object to query the directory for objects by a
     * globally unique identifier.
     *
     * @param pkey             When searching by globally unique identifier,
     *                         the object must also match this expected
     *                         primary key.
     * @param uniqueIdentifier The globally unique identifier value.
     * @return The Spring LdapQuery object to query the directory for objects
     * by their globally unique identifier.
     */
    LdapQuery getLdapQueryForGloballyUniqueIdentifier(String pkey, Object uniqueIdentifier);

    /**
     * Get a Spring LdapQuery object to query the directory for objects by a
     * primary key value.
     *
     * @param pkey The primary key value.
     * @return The Spring LdapQuery object to query the directory for objects
     * by their primary key.  null if searching by primary key is not
     * supported.
     */
    LdapQuery getLdapQueryForPrimaryKey(String pkey);

    /**
     * Implements criteria for successfully accepting a DN as a good entry
     * when resolving multiple entries when searching by primary key.  In
     * some (probably rare) cases, the directory may contain entries with the
     * same primary key but are known not ever to be a "primary" entry.  For
     * example, it's been observed that a vendor LDAP directory will leave
     * undesired replication artifacts in the directory when replicating
     * within a cluster.  These replication artifacts are to be disregarded
     * and deleted as undesired duplicates.
     * <p>
     * Note this is *only* used when resolving results when searching by
     * primary key.
     *
     * @param dn The dn to evaluate for acceptance.
     * @return true if the dn is accepted or false if the dn is rejected.
     */
    boolean acceptAsExistingDn(String dn);

    /**
     * @return true indicates that when updating, the existing attributes
     * that aren't in the update map will be kept instead of removed.
     */
    boolean isKeepExistingAttributesWhenUpdating();

    /**
     * @return true indicates that entires in the directory with the same
     * primary key that aren't considered the primary entry will be removed.
     * The primary entry is decided by the first entry encountered where
     * acceptAsExistingDn(dn) returns true.
     */
    boolean isRemoveDuplicatePrimaryKeys();

    /**
     * @return The objectClass to filter by when searching for primary keys.
     */
    String getObjectClass();

    /**
     * Returns an array of attribute names (with their dynamic indicators)
     * for which the values of these attributes are dynamically determined by
     * a callback that is assigned to the dynamic indicator.
     * <p>
     * The strings in this array have the following naming convention:
     * <code>attributeName.indicator</code> where <i>attributeName</i> is
     * the attribute in the downstream system and <i>indicator</i> is a
     * string that identifies which callback to use.  The callbacks are
     * configured in the connector's dynamicAttributeCallbacks map, where the
     * map key is
     * <i>attributeName.indicator</i> or <i>indicator</i> (for an indicator
     * that applies to all attributes) and the value is the instance of the
     * callback.
     * </p>
     * 'dn.ONCREATE' may be included, which is a special case that will
     * disable renaming of the object.
     *
     * @return An array of attribute names (with their dynamic indicators)
     * for which the values of these attributes are dynamically determined by
     * a callback that is assigned to the dynamic indicator.
     */
    String[] getDynamicAttributeNames();

    /**
     * If true, then the attribute values of DNs will be checked with case
     * sensitivity enabled when DNs are compared for equality. Attribute
     * names in DNs remain case insensitive when compared.
     * <p>
     * Different implementations of LDAP and AD servers behave differently in
     * regards to DN case sensitivity.  AD is an example where case sensitive
     * DN checking should be enabled.
     */
    boolean isCaseSensitiveDnCheckingEnabled();

    /**
     * The optional "group directive meta attribute" is a meta attribute that
     * is not an actual directory attribute but rather indicates to the
     * connector that the directory entry being persisted should be added or
     * removed from directory groups.
     * <p>
     * This is best described with an example.  If the
     * <code>getGroupDirectiveMetaAttributePrefix()</code> is
     * <code>GROUPS</code>, then when persisting an entry, the attribute map
     * could contain two attributes:
     * <pre>
     * GROUPS.ADD
     * GROUPS.REMOVE
     * </pre>
     * <p>
     * These two attributes are string collections of group DNs.
     * <pre>{@code
     * attrMap["GROUPS.ADD"] = ["cn=groupA,dc=example,dc=com","cn=groupB,dc=example,dc=com"]
     * attrMap["GROUPS.REMOVE"] = ["cn=groupC,dc=example,dc=com","cn=groupD,dc=example,dc=com"]
     * }</pre>
     * <p>
     * This tells the connector to add the entry to <code>groupA</code> and
     * <code>groupB</code> and remove the entry from <code>groupC</code> and
     * <code>groupD</code>.
     * <p>
     * These meta attributes can be dynamic, determined by a {@link
     * LdapDynamicAttributeCallback}.  This would be useful for appending a
     * base DN.  In this case, the attribute map would contain something
     * like:
     * <pre>{@code
     * attrMap["GROUPS.ADD.DYNAMIC"] = ["cn=groupA","cn=groupB"]
     * attrMap["GROUPS.REMOVE.DYNAMIC"] = ["cn=groupC","cn=groupD"]
     * }</pre>
     * <p>
     * Then the {@link LdapDynamicAttributeCallback} handler assigned to
     * these two meta attributes would append
     * <code>,dc=example,dc=com</code>.
     * <p>
     * If the directory implements a <code>memberOf</code> virtual
     * attribute (such as Active Directory), then you could have your {@link
     * LdapDynamicAttributeCallback} handler compare the existing
     * <code>memberOf</code> list with your desired <code>memberOf</code>
     * list to calculate which groups needed to be added and which groups
     * need to be removed.
     * <p>
     * The default LdapConnector implementation will not check if the member
     * already exists in the group for an ADD and will not check if a member
     * is missing from the group for a DELETE.
     *
     * @return The string prefix for the "group directive meta attribute", or
     * null if no group directives should be handled.
     */
    String getGroupDirectiveMetaAttributePrefix();

    /**
     * If {@link #getGroupDirectiveMetaAttributePrefix} is enabled (not
     * null), then this returns the attribute name within the group object
     * class that contains group member DNs.  For an Active Directory
     * <code>group</code> object class, this would be <code>member</code>.
     * For LDAP <code>groupOfUniqueNames</code> object class, this would be
     * <code>uniqueMember</code>.
     *
     * @return The attribute name within the group object class that contains
     * group member DNs.
     */
    String getGroupMemberAttributeName();
}
