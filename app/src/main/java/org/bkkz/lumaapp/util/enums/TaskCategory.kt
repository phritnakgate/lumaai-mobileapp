package org.bkkz.lumaapp.util.enums

enum class TaskCategory(val value: Int, val displayName: String) {
    CODING(0, "Coding"),
    MEETING(1, "ประชุม"),
    LEARNING(2, "อบรม"),
    POC(3, "POC"),
    OTHERS(4, "อื่นๆ");

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value }
    }

}