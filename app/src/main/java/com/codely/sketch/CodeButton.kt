package com.codely.sketch


enum class CodeButton(val value: Int) {
    VAR_DEC(0),
    PRINT(1),
    MODIFY(2),
    IF_ELSE(3);

    companion object {
        private val map = CodeButton.values().associateBy(CodeButton::value)
        fun fromInt(type: Int) = map[type]
    }
}