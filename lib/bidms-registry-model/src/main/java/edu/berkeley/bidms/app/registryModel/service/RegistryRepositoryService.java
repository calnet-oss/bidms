/*
 * Copyright (c) 2019, Regents of the University of California and
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
package edu.berkeley.bidms.app.registryModel.service;

import edu.berkeley.bidms.app.registryModel.repo.AddressRepository;
import edu.berkeley.bidms.app.registryModel.repo.AddressTypeRepository;
import edu.berkeley.bidms.app.registryModel.repo.AppointmentTypeRepository;
import edu.berkeley.bidms.app.registryModel.repo.AssignableRoleCategoryRepository;
import edu.berkeley.bidms.app.registryModel.repo.DateOfBirthRepository;
import edu.berkeley.bidms.app.registryModel.repo.DelegateProxyRepository;
import edu.berkeley.bidms.app.registryModel.repo.DelegateProxyTypeRepository;
import edu.berkeley.bidms.app.registryModel.repo.DownstreamObjectRepository;
import edu.berkeley.bidms.app.registryModel.repo.DownstreamSystemRepository;
import edu.berkeley.bidms.app.registryModel.repo.EmailRepository;
import edu.berkeley.bidms.app.registryModel.repo.EmailTypeRepository;
import edu.berkeley.bidms.app.registryModel.repo.IdentifierArchiveRepository;
import edu.berkeley.bidms.app.registryModel.repo.IdentifierRepository;
import edu.berkeley.bidms.app.registryModel.repo.IdentifierTypeRepository;
import edu.berkeley.bidms.app.registryModel.repo.JobAppointmentRepository;
import edu.berkeley.bidms.app.registryModel.repo.NameTypeRepository;
import edu.berkeley.bidms.app.registryModel.repo.PartialMatchRepository;
import edu.berkeley.bidms.app.registryModel.repo.PersonNameRepository;
import edu.berkeley.bidms.app.registryModel.repo.PersonRepository;
import edu.berkeley.bidms.app.registryModel.repo.PersonRoleArchiveRepository;
import edu.berkeley.bidms.app.registryModel.repo.PersonRoleRepository;
import edu.berkeley.bidms.app.registryModel.repo.PersonSorObjectsJsonRepository;
import edu.berkeley.bidms.app.registryModel.repo.PersonSorObjectsSyncKeyRepository;
import edu.berkeley.bidms.app.registryModel.repo.SORObjectChecksumRepository;
import edu.berkeley.bidms.app.registryModel.repo.SORObjectRepository;
import edu.berkeley.bidms.app.registryModel.repo.SORRepository;
import edu.berkeley.bidms.app.registryModel.repo.TelephoneRepository;
import edu.berkeley.bidms.app.registryModel.repo.TelephoneTypeRepository;
import edu.berkeley.bidms.app.registryModel.repo.TrackStatusRepository;
import edu.berkeley.bidms.app.registryModel.repo.auth.RegistryUserRepository;
import edu.berkeley.bidms.app.registryModel.repo.credentialManagement.ChangeEmailTokenRepository;
import edu.berkeley.bidms.app.registryModel.repo.credentialManagement.CredentialTokenRepository;
import edu.berkeley.bidms.app.registryModel.repo.credentialManagement.ResetPassphraseTokenRepository;
import edu.berkeley.bidms.app.registryModel.repo.view.PersonSearchViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A convenience service to retrieve repositories for JPA entity types.
 */
@Service
public class RegistryRepositoryService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressTypeRepository addressTypeRepository;

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @Autowired
    private AssignableRoleCategoryRepository assignableRoleCategoryRepository;

    @Autowired
    private AssignableRoleCategoryRepository assignableRoleRepository;

    @Autowired
    private AssignableRoleCategoryRepository registryRoleRepository;

    @Autowired
    private RegistryUserRepository registryUserRepository;

    @Autowired
    private ChangeEmailTokenRepository changeEmailTokenRepository;

    @Autowired
    private CredentialTokenRepository credentialTokenRepository;

    @Autowired
    private ResetPassphraseTokenRepository resetPassphraseTokenRepository;

    @Autowired
    private DateOfBirthRepository dateOfBirthRepository;

    @Autowired
    private DelegateProxyRepository delegateProxyRepository;

    @Autowired
    private DelegateProxyTypeRepository delegateProxyTypeRepository;

    @Autowired
    private DownstreamObjectRepository downstreamObjectRepository;

    @Autowired
    private DownstreamSystemRepository downstreamSystemRepository;

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private EmailTypeRepository emailTypeRepository;

    @Autowired
    private IdentifierArchiveRepository identifierArchiveRepository;

    @Autowired
    private IdentifierRepository identifierRepository;

    @Autowired
    private IdentifierTypeRepository identifierTypeRepository;

    @Autowired
    private JobAppointmentRepository jobAppointmentRepository;

    @Autowired
    private NameTypeRepository nameTypeRepository;

    @Autowired
    private PartialMatchRepository partialMatchRepository;

    @Autowired
    private PersonNameRepository personNameRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonRoleArchiveRepository personRoleArchiveRepository;

    @Autowired
    private PersonRoleRepository personRoleRepository;

    @Autowired
    private PersonSorObjectsJsonRepository personSorObjectsJsonRepository;

    @Autowired
    private PersonSorObjectsSyncKeyRepository personSorObjectsSyncKeyRepository;

    @Autowired
    private SORObjectChecksumRepository sorObjectChecksumRepository;

    @Autowired
    private SORObjectRepository sorObjectRepository;

    @Autowired
    private SORRepository sorRepository;

    @Autowired
    private TelephoneRepository telephoneRepository;

    @Autowired
    private TelephoneTypeRepository telephoneTypeRepository;

    @Autowired
    private TrackStatusRepository trackStatusRepository;

    @Autowired
    private PersonSearchViewRepository personSearchViewRepository;

    public AddressRepository getForAddress() {
        return addressRepository;
    }

    public AddressTypeRepository getForAddressType() {
        return addressTypeRepository;
    }

    public AppointmentTypeRepository getForAppointmentType() {
        return appointmentTypeRepository;
    }

    public AssignableRoleCategoryRepository getForAssignableRoleCategory() {
        return assignableRoleCategoryRepository;
    }

    public AssignableRoleCategoryRepository getForAssignableRole() {
        return assignableRoleRepository;
    }

    public AssignableRoleCategoryRepository getForRegistryRole() {
        return registryRoleRepository;
    }

    public RegistryUserRepository getForRegistryUser() {
        return registryUserRepository;
    }

    public ChangeEmailTokenRepository getForChangeEmailToken() {
        return changeEmailTokenRepository;
    }

    public CredentialTokenRepository getForCredentialToken() {
        return credentialTokenRepository;
    }

    public ResetPassphraseTokenRepository getForResetPassphraseToken() {
        return resetPassphraseTokenRepository;
    }

    public DateOfBirthRepository getForDateOfBirth() {
        return dateOfBirthRepository;
    }

    public DelegateProxyRepository getForDelegateProxy() {
        return delegateProxyRepository;
    }

    public DelegateProxyTypeRepository getForDelegateProxyType() {
        return delegateProxyTypeRepository;
    }

    public DownstreamObjectRepository getForDownstreamObject() {
        return downstreamObjectRepository;
    }

    public DownstreamSystemRepository getForDownstreamSystem() {
        return downstreamSystemRepository;
    }

    public EmailRepository getForEmail() {
        return emailRepository;
    }

    public EmailTypeRepository getForEmailType() {
        return emailTypeRepository;
    }

    public IdentifierArchiveRepository getForIdentifierArchive() {
        return identifierArchiveRepository;
    }

    public IdentifierRepository getForIdentifier() {
        return identifierRepository;
    }

    public IdentifierTypeRepository getForIdentifierType() {
        return identifierTypeRepository;
    }

    public JobAppointmentRepository getForJobAppointment() {
        return jobAppointmentRepository;
    }

    public NameTypeRepository getForNameType() {
        return nameTypeRepository;
    }

    public PartialMatchRepository getForPartialMatch() {
        return partialMatchRepository;
    }

    public PersonNameRepository getForPersonName() {
        return personNameRepository;
    }

    public PersonRepository getForPerson() {
        return personRepository;
    }

    public PersonRoleArchiveRepository getForPersonRoleArchive() {
        return personRoleArchiveRepository;
    }

    public PersonRoleRepository getForPersonRole() {
        return personRoleRepository;
    }

    public PersonSorObjectsJsonRepository getForPersonSorObjectsJson() {
        return personSorObjectsJsonRepository;
    }

    public PersonSorObjectsSyncKeyRepository getForPersonSorObjectsSyncKey() {
        return personSorObjectsSyncKeyRepository;
    }

    public SORObjectChecksumRepository getForSorObjectChecksum() {
        return sorObjectChecksumRepository;
    }

    public SORObjectRepository getForSorObject() {
        return sorObjectRepository;
    }

    public SORRepository getForSor() {
        return sorRepository;
    }

    public TelephoneRepository getForTelephone() {
        return telephoneRepository;
    }

    public TelephoneTypeRepository getForTelephoneType() {
        return telephoneTypeRepository;
    }

    public TrackStatusRepository getForTrackStatus() {
        return trackStatusRepository;
    }

    public PersonSearchViewRepository getForPersonSearchView() {
        return personSearchViewRepository;
    }
}
