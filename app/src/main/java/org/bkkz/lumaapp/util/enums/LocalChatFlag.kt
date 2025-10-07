package org.bkkz.lumaapp.util.enums

enum class LocalChatFlag(val flag: Int, val serviceResponseIntent : String? = null) {
    CHAT_USER(0),
    CHAT_MODEL(1),
    CHAT_VIEW_TASK(2, "CHECK"),
    CHAT_ADD_TASK(3, "ADD"),
    CHAT_EDIT_TASK(4, "EDIT"),
    CHAT_DELETE_TASK(5, "DELETE"),
    CHAT_WEB(6, "GOOGLESEARCH"),
    CHAT_GENFORM(7)
}