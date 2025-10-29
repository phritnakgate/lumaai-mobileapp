package org.bkkz.lumaapp.util.component.chat

sealed class QuickPromptLov {
    data class Headline(val headline: String) : QuickPromptLov()
    data class Items(val displayString: String ,val prompt: String) : QuickPromptLov()
}