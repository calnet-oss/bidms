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
package edu.berkeley.bidms.app.registryModel.model

import edu.berkeley.bidms.registryModel.repo.ExtendedRepository
import edu.berkeley.bidms.app.registryModel.model.type.PrioritizedEnum
import edu.berkeley.bidms.app.registryModel.model.type.TypeEnum

abstract class AbstractPrioritizedTypeSpec<T, EN extends TypeEnum & PrioritizedEnum, I, R extends ExtendedRepository<T, I>> extends AbstractTypeSpec<T, EN, I, R> {
    abstract int getPriority(T type)

    abstract List<String> getUnprioritizedTypeNames()

    void "test enum priority"() {
        when:
        insert()

        then:
        typeNames.each { typeName ->
            T type = findByTypeName(typeName)
            if (!unprioritizedTypeNames.contains(typeName)) {
                int priority = getPriority(type)
                assert typeName && priority >= 0 && priority != Integer.MAX_VALUE
            } else {
                // unprioritized
                assert typeName && getPriority(type) == Integer.MAX_VALUE
            }
        }
    }
}
