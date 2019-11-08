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
package edu.berkeley.bidms.common.util;

import java.security.SecureRandom;

/**
 * Generate strings with random characters.
 */
public class RandomStringUtil {
    /**
     * Returns a random number of characters
     *
     * @param n         number of characters
     * @param templates what character sets should be used for the first
     *                  templates.length characters. Can be none.
     * @return a random character string of length n, starting with the
     * specified char sets
     */
    public static String randomString(int n, CharTemplate... templates) {
        CharTemplate[] internalTemplates = templates != null ? templates : new CharTemplate[]{};

        char[] buffer = new char[n];
        for (int i = 0; i < n; i++) {
            if (i < internalTemplates.length) {
                buffer[i] = internalTemplates[i].getRandomChar();
            } else {
                buffer[i] = CharTemplate.ALPHANUMERIC.getRandomChar();
            }
        }

        return new String(buffer);
    }

    public static enum CharTemplate {

        /**
         * Upper or lower case ASCII character: [a-zA-Z]
         */
        ALPHA(concatChars(lowerCase, upperCase)),

        /**
         * ASCII digit: [0-9]
         */
        NUMERIC(numeric),

        /**
         * Upper or lower case ASCII character or ASCII digit: [a-zA-Z0-9]
         */
        ALPHANUMERIC(concatChars(lowerCase, upperCase, numeric)),

        /**
         * Lower case ASCII character: [a-z]
         */
        LOWER_ALPHA(lowerCase),

        /**
         * Upper case ASCII character: [A-Z]
         */
        UPPER_ALPHA(upperCase),

        /**
         * Lower case ASCII character or ASCII digit: [a-z0-9]
         */
        LOWER_ALPHANUMERIC(concatChars(lowerCase, numeric)),

        /**
         * Upper case ASCII character or ASCII digit: [A-Z0-9]
         */
        UPPER_ALPHANUMERIC(concatChars(upperCase, numeric));

        private final char[] characters;
        private static final SecureRandom random = new SecureRandom();

        CharTemplate(char[] characters) {
            this.characters = characters;
        }

        public Character getRandomChar() {
            return characters[random.nextInt(characters.length)];
        }

        private static char[] concatChars(char[]... characterArrays) {
            int characterCount = 0;
            for (char[] charArray : characterArrays) {
                characterCount += charArray.length;
            }
            char[] buffer = new char[characterCount];
            int bufferIndex = 0;
            for (char[] charArray : characterArrays) {
                System.arraycopy(charArray, 0, buffer, bufferIndex, charArray.length);
                bufferIndex += charArray.length;
            }
            return buffer;
        }
    }

    // [a-z]
    private static final char[] lowerCase = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    // [A-Z]
    private static final char[] upperCase = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    // [0-9]
    private static final char[] numeric = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
}
