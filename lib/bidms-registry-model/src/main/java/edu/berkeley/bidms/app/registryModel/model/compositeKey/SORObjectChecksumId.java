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
package edu.berkeley.bidms.app.registryModel.model.compositeKey;

import edu.berkeley.bidms.app.registryModel.model.SOR;
import edu.berkeley.bidms.app.registryModel.model.SORObjectChecksum;
import edu.berkeley.bidms.registryModel.util.EntityUtil;

import java.io.Serializable;

/**
 * Composite key for {@link SORObjectChecksum}.
 */
public class SORObjectChecksumId implements Serializable {
    private SOR sor;
    private String sorObjKey;

    public SORObjectChecksumId() {
    }

    public SORObjectChecksumId(SOR sor, String sorObjKey) {
        this.sor = sor;
        this.sorObjKey = sorObjKey;
    }

    private static final int HCB_INIT_ODDRAND = 1565873683;
    private static final int HCB_MULT_ODDRAND = -175918775;

    private Object[] getHashCodeObjects() {
        return new Object[]{sor.getId(), sorObjKey};
    }

    @Override
    public int hashCode() {
        return EntityUtil.genHashCode(
                HCB_INIT_ODDRAND, HCB_MULT_ODDRAND,
                getHashCodeObjects()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SORObjectChecksumId) {
            return EntityUtil.isEqual(this, getHashCodeObjects(), obj, ((SORObjectChecksumId) obj).getHashCodeObjects());
        }
        return false;
    }

    public SOR getSor() {
        return sor;
    }

    public void setSor(SOR sor) {
        this.sor = sor;
    }

    public String getSorObjKey() {
        return sorObjKey;
    }

    public void setSorObjKey(String sorObjKey) {
        this.sorObjKey = sorObjKey;
    }
}
