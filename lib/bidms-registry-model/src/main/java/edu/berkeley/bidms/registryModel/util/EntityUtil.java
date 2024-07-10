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
package edu.berkeley.bidms.registryModel.util;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Static utility methods for entity objects. (Typically JPA entities, but
 * usable for POJOs as well.)
 */
public class EntityUtil {
    /**
     * Generate a salted hash code for an array of entity attribute values.
     * This method is typically called from an overridden {@link
     * Object#hashCode()} method in an entity class.
     *
     * <p>
     * The implementation requires that the salts be odd integers and that
     * the
     * <i>initOddRand</i> salt does not equal the <i>multOddRand</i> salt.
     * Note that the intention is that each JPA entity class has its own
     * unique salt values.
     * </p>
     *
     * <p>
     * On Unix-like systems, an odd random integer can be generated from the
     * shell:
     * </p>
     * <pre>{@code
     * (rand=0; while [ $(echo "$rand % 2"|bc) -eq 0 ]; do rand=$(od -An -N4 -i /dev/urandom); done && echo $rand)
     * }</pre>
     *
     * <p>
     * The array of entity attribute values passed in are the "hashable"
     * values of the entity used to determine if one entity instance is equal
     * to another.  This typically does <b>not</b> include the entity's ID,
     * but includes the rest of the entity's persisted values.  This is
     * particularly useful when rebuilding collections from business logic
     * and comparing it to the persisted collection.
     * </p>
     *
     * @param initOddRand A random <b>odd</b> integer.
     * @param multOddRand A random <b>odd</b> integer.
     * @param objects     An array of attribute values within the entity.
     *                    This should be the same array of attribute values
     *                    used for equality comparison with {@link
     *                    #isEqual(Object, Object[], Object, Object[])}.
     * @return
     * @throws IllegalArgumentException If neither initOddRand nor
     *                                  multOddRand are odd, or initOddRand
     *                                  equals multOddRand.
     */
    public static int genHashCode(int initOddRand, int multOddRand, Object[] objects) {
        if (initOddRand % 2 == 0) {
            throw new IllegalArgumentException("initOddRand parameter must be an odd integer, not an even integer");
        }
        if (multOddRand % 2 == 0) {
            throw new IllegalArgumentException("multOddRand parameter must be an odd integer, not an even integer");
        }
        if (initOddRand == multOddRand) {
            throw new IllegalArgumentException("The initOddRand and multOddRand parameters can not equal each other");
        }
        final HashCodeBuilder builder = new HashCodeBuilder(initOddRand, multOddRand);
        for (Object obj : objects) {
            builder.append(obj);
        }
        return builder.toHashCode();
    }

    /**
     * This method is similar to {@link #genHashCode(int, int, Object[])}
     * except it is typically called from an overridden {@link
     * Object#equals(Object)} method in an entity class to check for equality
     * between two entities.  See {@link #genHashCode(int, int, Object[])}
     * documentation for a more detailed explanation of entity equality.
     *
     * @param left                 Entity object 1 of the equality
     *                             comparison.
     * @param leftHashCodeObjects  An array of attribute values within entity
     *                             object 1.  This should be the same array
     *                             of attribute values used for generating
     *                             the hash code with {@link #genHashCode(int,
     *                             int, Object[])}.
     * @param right                Entity object 2 of the equality
     *                             comparison.
     * @param rightHashCodeObjects An array of attribute values within entity
     *                             object 2.  This should be the same array
     *                             of attribute values used for generating
     *                             the hash code with {@link #genHashCode(int,
     *                             int, Object[])}.
     * @param <T>                  The type of the entities being compared
     *                             for equality.
     * @return true if the two entities are equal.
     */
    public static <T> boolean isEqual(T left, Object[] leftHashCodeObjects, T right, Object[] rightHashCodeObjects) {
        if (right == left) {
            return true;
        }
        EqualsBuilder builder = new EqualsBuilder();
        for (int i = 0; i < leftHashCodeObjects.length; i++) {
            builder.append(leftHashCodeObjects[i], rightHashCodeObjects[i]);
        }
        return builder.isEquals();
    }

    /**
     * This method is similar to {@link #genHashCode(int, int, Object[])} and
     * {@link #isEqual(Object, Object[], Object, Object[])} except it is
     * typically called from an implementation of the {@link
     * Comparable#compareTo(Object)} method in an entity class to compare two
     * entities. See {@link #genHashCode(int, int, Object[])} documentation
     * for a more detailed explanation of entity equality.
     *
     * @param left                 Entity object 1 of the equality
     *                             comparison.
     * @param leftHashCodeObjects  An array of attribute values within entity
     *                             object 1.  This should be the same array
     *                             of attribute values used for generating
     *                             the hash code with {@link #genHashCode(int,
     *                             int, Object[])}.
     * @param right                Entity object 2 of the equality
     *                             comparison.
     * @param rightHashCodeObjects An array of attribute values within entity
     *                             object 2.  This should be the same array
     *                             of attribute values used for generating
     *                             the hash code with {@link #genHashCode(int,
     *                             int, Object[])}.
     * @param <T>                  The type of the entities being compared
     *                             for equality.
     * @return A negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    public static <T> int compareTo(T left, Object[] leftHashCodeObjects, T right, Object[] rightHashCodeObjects) {
        if (right == left) {
            return 0;
        }
        CompareToBuilder builder = new CompareToBuilder();
        for (int i = 0; i < leftHashCodeObjects.length; i++) {
            builder.append(leftHashCodeObjects[i], rightHashCodeObjects[i]);
        }
        return builder.toComparison();
    }
}
