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

import javax.persistence.EntityManager;

/**
 * A convenience service to retrieve repositories for JPA entity types.
 */
@Service
public class RegistryRepositoryService {
    @Autowired(required = false)
    private EntityManager entityManager;

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

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AddressRepository getAddressRepository() {
        return addressRepository;
    }

    public void setAddressRepository(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public AddressTypeRepository getAddressTypeRepository() {
        return addressTypeRepository;
    }

    public void setAddressTypeRepository(AddressTypeRepository addressTypeRepository) {
        this.addressTypeRepository = addressTypeRepository;
    }

    public AppointmentTypeRepository getAppointmentTypeRepository() {
        return appointmentTypeRepository;
    }

    public void setAppointmentTypeRepository(AppointmentTypeRepository appointmentTypeRepository) {
        this.appointmentTypeRepository = appointmentTypeRepository;
    }

    public AssignableRoleCategoryRepository getAssignableRoleCategoryRepository() {
        return assignableRoleCategoryRepository;
    }

    public void setAssignableRoleCategoryRepository(AssignableRoleCategoryRepository assignableRoleCategoryRepository) {
        this.assignableRoleCategoryRepository = assignableRoleCategoryRepository;
    }

    public AssignableRoleCategoryRepository getAssignableRoleRepository() {
        return assignableRoleRepository;
    }

    public void setAssignableRoleRepository(AssignableRoleCategoryRepository assignableRoleRepository) {
        this.assignableRoleRepository = assignableRoleRepository;
    }

    public AssignableRoleCategoryRepository getRegistryRoleRepository() {
        return registryRoleRepository;
    }

    public void setRegistryRoleRepository(AssignableRoleCategoryRepository registryRoleRepository) {
        this.registryRoleRepository = registryRoleRepository;
    }

    public RegistryUserRepository getRegistryUserRepository() {
        return registryUserRepository;
    }

    public void setRegistryUserRepository(RegistryUserRepository registryUserRepository) {
        this.registryUserRepository = registryUserRepository;
    }

    public ChangeEmailTokenRepository getChangeEmailTokenRepository() {
        return changeEmailTokenRepository;
    }

    public void setChangeEmailTokenRepository(ChangeEmailTokenRepository changeEmailTokenRepository) {
        this.changeEmailTokenRepository = changeEmailTokenRepository;
    }

    public CredentialTokenRepository getCredentialTokenRepository() {
        return credentialTokenRepository;
    }

    public void setCredentialTokenRepository(CredentialTokenRepository credentialTokenRepository) {
        this.credentialTokenRepository = credentialTokenRepository;
    }

    public ResetPassphraseTokenRepository getResetPassphraseTokenRepository() {
        return resetPassphraseTokenRepository;
    }

    public void setResetPassphraseTokenRepository(ResetPassphraseTokenRepository resetPassphraseTokenRepository) {
        this.resetPassphraseTokenRepository = resetPassphraseTokenRepository;
    }

    public DateOfBirthRepository getDateOfBirthRepository() {
        return dateOfBirthRepository;
    }

    public void setDateOfBirthRepository(DateOfBirthRepository dateOfBirthRepository) {
        this.dateOfBirthRepository = dateOfBirthRepository;
    }

    public DelegateProxyRepository getDelegateProxyRepository() {
        return delegateProxyRepository;
    }

    public void setDelegateProxyRepository(DelegateProxyRepository delegateProxyRepository) {
        this.delegateProxyRepository = delegateProxyRepository;
    }

    public DelegateProxyTypeRepository getDelegateProxyTypeRepository() {
        return delegateProxyTypeRepository;
    }

    public void setDelegateProxyTypeRepository(DelegateProxyTypeRepository delegateProxyTypeRepository) {
        this.delegateProxyTypeRepository = delegateProxyTypeRepository;
    }

    public DownstreamObjectRepository getDownstreamObjectRepository() {
        return downstreamObjectRepository;
    }

    public void setDownstreamObjectRepository(DownstreamObjectRepository downstreamObjectRepository) {
        this.downstreamObjectRepository = downstreamObjectRepository;
    }

    public DownstreamSystemRepository getDownstreamSystemRepository() {
        return downstreamSystemRepository;
    }

    public void setDownstreamSystemRepository(DownstreamSystemRepository downstreamSystemRepository) {
        this.downstreamSystemRepository = downstreamSystemRepository;
    }

    public EmailRepository getEmailRepository() {
        return emailRepository;
    }

    public void setEmailRepository(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public EmailTypeRepository getEmailTypeRepository() {
        return emailTypeRepository;
    }

    public void setEmailTypeRepository(EmailTypeRepository emailTypeRepository) {
        this.emailTypeRepository = emailTypeRepository;
    }

    public IdentifierArchiveRepository getIdentifierArchiveRepository() {
        return identifierArchiveRepository;
    }

    public void setIdentifierArchiveRepository(IdentifierArchiveRepository identifierArchiveRepository) {
        this.identifierArchiveRepository = identifierArchiveRepository;
    }

    public IdentifierRepository getIdentifierRepository() {
        return identifierRepository;
    }

    public void setIdentifierRepository(IdentifierRepository identifierRepository) {
        this.identifierRepository = identifierRepository;
    }

    public IdentifierTypeRepository getIdentifierTypeRepository() {
        return identifierTypeRepository;
    }

    public void setIdentifierTypeRepository(IdentifierTypeRepository identifierTypeRepository) {
        this.identifierTypeRepository = identifierTypeRepository;
    }

    public JobAppointmentRepository getJobAppointmentRepository() {
        return jobAppointmentRepository;
    }

    public void setJobAppointmentRepository(JobAppointmentRepository jobAppointmentRepository) {
        this.jobAppointmentRepository = jobAppointmentRepository;
    }

    public NameTypeRepository getNameTypeRepository() {
        return nameTypeRepository;
    }

    public void setNameTypeRepository(NameTypeRepository nameTypeRepository) {
        this.nameTypeRepository = nameTypeRepository;
    }

    public PartialMatchRepository getPartialMatchRepository() {
        return partialMatchRepository;
    }

    public void setPartialMatchRepository(PartialMatchRepository partialMatchRepository) {
        this.partialMatchRepository = partialMatchRepository;
    }

    public PersonNameRepository getPersonNameRepository() {
        return personNameRepository;
    }

    public void setPersonNameRepository(PersonNameRepository personNameRepository) {
        this.personNameRepository = personNameRepository;
    }

    public PersonRepository getPersonRepository() {
        return personRepository;
    }

    public void setPersonRepository(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public PersonRoleArchiveRepository getPersonRoleArchiveRepository() {
        return personRoleArchiveRepository;
    }

    public void setPersonRoleArchiveRepository(PersonRoleArchiveRepository personRoleArchiveRepository) {
        this.personRoleArchiveRepository = personRoleArchiveRepository;
    }

    public PersonRoleRepository getPersonRoleRepository() {
        return personRoleRepository;
    }

    public void setPersonRoleRepository(PersonRoleRepository personRoleRepository) {
        this.personRoleRepository = personRoleRepository;
    }

    public PersonSorObjectsJsonRepository getPersonSorObjectsJsonRepository() {
        return personSorObjectsJsonRepository;
    }

    public void setPersonSorObjectsJsonRepository(PersonSorObjectsJsonRepository personSorObjectsJsonRepository) {
        this.personSorObjectsJsonRepository = personSorObjectsJsonRepository;
    }

    public PersonSorObjectsSyncKeyRepository getPersonSorObjectsSyncKeyRepository() {
        return personSorObjectsSyncKeyRepository;
    }

    public void setPersonSorObjectsSyncKeyRepository(PersonSorObjectsSyncKeyRepository personSorObjectsSyncKeyRepository) {
        this.personSorObjectsSyncKeyRepository = personSorObjectsSyncKeyRepository;
    }

    public SORObjectChecksumRepository getSorObjectChecksumRepository() {
        return sorObjectChecksumRepository;
    }

    public void setSorObjectChecksumRepository(SORObjectChecksumRepository sorObjectChecksumRepository) {
        this.sorObjectChecksumRepository = sorObjectChecksumRepository;
    }

    public SORObjectRepository getSorObjectRepository() {
        return sorObjectRepository;
    }

    public void setSorObjectRepository(SORObjectRepository sorObjectRepository) {
        this.sorObjectRepository = sorObjectRepository;
    }

    public SORRepository getSorRepository() {
        return sorRepository;
    }

    public void setSorRepository(SORRepository sorRepository) {
        this.sorRepository = sorRepository;
    }

    public TelephoneRepository getTelephoneRepository() {
        return telephoneRepository;
    }

    public void setTelephoneRepository(TelephoneRepository telephoneRepository) {
        this.telephoneRepository = telephoneRepository;
    }

    public TelephoneTypeRepository getTelephoneTypeRepository() {
        return telephoneTypeRepository;
    }

    public void setTelephoneTypeRepository(TelephoneTypeRepository telephoneTypeRepository) {
        this.telephoneTypeRepository = telephoneTypeRepository;
    }

    public TrackStatusRepository getTrackStatusRepository() {
        return trackStatusRepository;
    }

    public void setTrackStatusRepository(TrackStatusRepository trackStatusRepository) {
        this.trackStatusRepository = trackStatusRepository;
    }

    public PersonSearchViewRepository getPersonSearchViewRepository() {
        return personSearchViewRepository;
    }

    public void setPersonSearchViewRepository(PersonSearchViewRepository personSearchViewRepository) {
        this.personSearchViewRepository = personSearchViewRepository;
    }
}
