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

import java.util.Date;

/**
 * Static utility methods for dates.
 */
public class DateUtil {
    /**
     * Returns true if left date is greater than right date {@code (left >
     * right)}.
     *
     * @param left  The left date.
     * @param right The right date.
     * @return true if {@code left > right}
     */
    public static boolean greaterThan(Date left, Date right) {
        if (left != null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.compareTo(right) > 0;
    }

    /**
     * Returns true if left date is less than right date {@code (left <
     * right)}.
     *
     * @param left  The left date.
     * @param right The right date.
     * @return true if {@code left < right}
     */
    public static boolean lessThan(Date left, Date right) {
        if (left == null && right != null) {
            return true;
        }
        if (right == null) {
            return false;
        }
        return left.compareTo(right) < 0;
    }

    /**
     * Returns true if left date is greater than or equals to the right date
     * {@code (left >= right)}.
     *
     * @param left  The left date.
     * @param right The right date.
     * @return true if {@code left >= right}
     */
    public static boolean greaterThanEqualsTo(Date left, Date right) {
        if (left != null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.compareTo(right) >= 0;
    }

    /**
     * Returns true if the left date is less than or equals to the right date
     * {@code (left <= right)}.
     *
     * @param left  The left date.
     * @param right The right date.
     * @return true if {@code left <= right}
     */
    public static boolean lessThanEqualsTo(Date left, Date right) {
        if (left == null && right != null) {
            return true;
        }
        if (right == null) {
            return false;
        }
        return left.compareTo(right) <= 0;
    }
}
