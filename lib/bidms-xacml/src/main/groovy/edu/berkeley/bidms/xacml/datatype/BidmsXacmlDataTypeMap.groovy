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
package edu.berkeley.bidms.xacml.datatype

import com.att.research.xacml.api.DataTypeException
import com.att.research.xacml.api.Identifier
import com.att.research.xacml.std.IdentifierImpl
import com.att.research.xacml.std.datatypes.DataTypeBase
import groovy.transform.CompileStatic

@CompileStatic
class BidmsXacmlDataTypeMap extends DataTypeBase<MapHolder> {

    public static final String ID_DATATYPE_BIDMS_MAP_STRING = "urn:edu:berkeley:bidms:xacml:data-type:map"
    public static final Identifier ID_DATATYPE_BIDMS_MAP = new IdentifierImpl(ID_DATATYPE_BIDMS_MAP_STRING)

    private static final BidmsXacmlDataTypeMap singleInstance = new BidmsXacmlDataTypeMap();

    private BidmsXacmlDataTypeMap() {
        super(ID_DATATYPE_BIDMS_MAP, MapHolder)
    }

    static BidmsXacmlDataTypeMap newInstance() {
        return singleInstance
    }

    @Override
    MapHolder convert(Object source) throws DataTypeException {
        if (source == null || (source instanceof MapHolder)) {
            return (MapHolder) source
        } else if (source instanceof Map) {
            return new MapHolder((Map) source)
        } else {
            throw new DataTypeException(this, "Unable to convert ${source.getClass().canonicalName} to a Map")
        }
    }
}
