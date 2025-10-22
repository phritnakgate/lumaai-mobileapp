package org.bkkz.lumaapp.util.enums

enum class LLMIntent(val intent: String) {
    CHECK("CHECK"),
    ADD("ADD"),
    EDIT("EDIT"),
    DELETE("REMOVE"),
    SEARCH("SEARCH"),
    GOOGLESEARCH("GOOGLESEARCH"),
    GENFORM("GENFORM"),
    EXIT("EXIT"),
    UNKNOWN("UNKNOWN")
}