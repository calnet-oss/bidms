package edu.berkeley.calnet.ucbmatch

enum ConfidenceType {
    CANONICAL(true),
    POTENTIAL(false)

    boolean exactMatch

    ConfidenceType(boolean exactMatch) {
        this.exactMatch = exactMatch
    }
}