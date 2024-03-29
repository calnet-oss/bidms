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
package edu.berkeley.bidms.app.registryModel.repo;

import edu.berkeley.bidms.app.registryModel.model.Person;
import edu.berkeley.bidms.app.registryModel.model.SOR;
import edu.berkeley.bidms.app.registryModel.model.SORObject;
import edu.berkeley.bidms.registryModel.repo.ExtendedRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for {@link SORObject} entities.
 */
public interface SORObjectRepository extends ExtendedRepository<SORObject, Long> {
    SORObject findBySorAndSorPrimaryKey(SOR sor, String sorPrimaryKey);

    SORObject findByPersonAndSorAndSorPrimaryKey(Person person, SOR sor, String sorPrimaryKey);

    List<SORObject> findAllByPerson(Person person);

    List<SORObject> findAllByPersonAndSor(Person person, SOR sor);

    List<SORObject> findAllBySorPrimaryKey(String sorPrimaryKey);

    List<SORObject> findAllBySorPrimaryKeyIn(List<String> sorPrimaryKeys);

    @Query("SELECT obj.sorObject.id FROM PartialMatch obj WHERE obj.isReject=?1 GROUP BY obj.sorObject.id")
    List<Long> findAllPartialMatchSORObjectIdsByIsRejectGroupBySorObject(boolean isReject);
}
