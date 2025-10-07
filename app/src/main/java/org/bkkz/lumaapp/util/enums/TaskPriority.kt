package org.bkkz.lumaapp.util.enums

enum class TaskPriority(val value: Int, val displayName: String) {
    HIGH(0, "เร่งด่วนมาก"),
    MEDIUM(1, "เร่งด่วนปานกลาง"),
    LOW(2, "ไม่เร่งด่วน");

    companion object {
        fun fromInt(value: Int) = TaskPriority.entries.firstOrNull { it.value == value }
    }
}