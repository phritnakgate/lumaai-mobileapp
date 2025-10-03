package org.bkkz.lumaapp.util.enums

enum class LLMIntent(val intent: String) {
    CHECK("CHECK"),
    ADD("ADD"),
    EDIT("EDIT"),
    DELETE("DELETE"),
    SEARCH("SEARCH"),
    GOOGLESEARCH("GOOGLESEARCH"),
    GENFORM("GENFORM")
}