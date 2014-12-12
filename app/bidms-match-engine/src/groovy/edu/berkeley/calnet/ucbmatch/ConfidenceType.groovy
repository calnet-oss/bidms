package edu.berkeley.calnet.ucbmatch

enum ConfidenceType {
    CANONICAL(95),
    POTENTIAL(85)

    int levelOfConfidence

    ConfidenceType(int levelOfConfidence) {
        this.levelOfConfidence = levelOfConfidence
    }
}