/*
 * Copyright (c) 2014, Regents of the University of California and
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
package edu.berkeley.bidms.app.matchengine.config;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MatchAttributeConfig {
    private String name;
    private String description;
    private String column;
    private String isPrimaryKeyColumn;
    private String path;
    private String outputPath;
    private String attribute;
    private String group;
    private boolean invalidates;
    private List<Pattern> nullEquivalents = List.of(Pattern.compile("[0-]+"), Pattern.compile("\\s+"));
    private SearchSettings search;
    private InputSettings input;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getIsPrimaryKeyColumn() {
        return isPrimaryKeyColumn;
    }

    public void setIsPrimaryKeyColumn(String isPrimaryKeyColumn) {
        this.isPrimaryKeyColumn = isPrimaryKeyColumn;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isInvalidates() {
        return invalidates;
    }

    public void setInvalidates(boolean invalidates) {
        this.invalidates = invalidates;
    }

    public List<Pattern> getNullEquivalents() {
        return nullEquivalents;
    }

    public void setNullEquivalents(List<Pattern> nullEquivalents) {
        this.nullEquivalents = nullEquivalents;
    }

    public SearchSettings getSearch() {
        return search;
    }

    public void setSearch(SearchSettings search) {
        this.search = search;
    }

    public InputSettings getInput() {
        return input;
    }

    public void setInput(InputSettings input) {
        this.input = input;
    }

    @Override
    public String toString() {
        return "MatchAttributeConfig{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", column='" + column + '\'' +
                ", isPrimaryKeyColumn='" + isPrimaryKeyColumn + '\'' +
                ", path='" + path + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", attribute='" + attribute + '\'' +
                ", group='" + group + '\'' +
                ", invalidates=" + invalidates +
                ", nullEquivalents=" + nullEquivalents +
                ", search=" + search +
                ", input=" + input +
                '}';
    }

    public static class SearchSettings {
        private boolean caseSensitive;
        private boolean alphanumeric;
        private boolean timestamp;
        private Map substring;
        private int distance;
        private String fixedValue;
        private String dateFormat;

        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        public void setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }

        public boolean isAlphanumeric() {
            return alphanumeric;
        }

        public void setAlphanumeric(boolean alphanumeric) {
            this.alphanumeric = alphanumeric;
        }

        public boolean isTimestamp() {
            return timestamp;
        }

        public void setTimestamp(boolean timestamp) {
            this.timestamp = timestamp;
        }

        public Map getSubstring() {
            return substring;
        }

        void setSubstring(Map substring) {
            if (!substring.containsKey("from")) {
                throw new IllegalArgumentException("Missing 'from' key in Map");
            }
            if (!substring.containsKey("length")) {
                throw new IllegalArgumentException("Missing 'length' key in Map");
            }
            this.substring = substring;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public String getFixedValue() {
            return fixedValue;
        }

        public void setFixedValue(String fixedValue) {
            this.fixedValue = fixedValue;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public void setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public String toString() {
            return "SearchSettings{" +
                    "caseSensitive=" + caseSensitive +
                    ", alphanumeric=" + alphanumeric +
                    ", timestamp=" + timestamp +
                    ", substring=" + substring +
                    ", distance=" + distance +
                    ", fixedValue='" + fixedValue + '\'' +
                    ", dateFormat='" + dateFormat + '\'' +
                    '}';
        }
    }

    public static class InputSettings {
        private String fixedValue;

        public String getFixedValue() {
            return fixedValue;
        }

        public void setFixedValue(String fixedValue) {
            this.fixedValue = fixedValue;
        }

        @Override
        public String toString() {
            return "InputSettings{" +
                    "fixedValue='" + fixedValue + '\'' +
                    '}';
        }
    }
}
