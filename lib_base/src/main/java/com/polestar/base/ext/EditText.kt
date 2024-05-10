package com.polestar.base.ext

import android.text.InputFilter
import android.widget.EditText
import java.util.Locale

fun EditText.autoUpperTransformX() = setupFilter(uppercase)

fun EditText.numberOrCharUppercase() = setupFilter(letterOrDigit, uppercase)

fun EditText.numberOrChar() = setupFilter(letterOrDigit)

fun EditText.setupFilter(vararg filter: InputFilter) {
    filters = filter
}


// InputFilter 使所有输入都转换为大写
val uppercase = InputFilter { source, _, _, _, _, _ ->
    source.toString().uppercase(Locale.getDefault())
}

// InputFilter 只能输入字母和数字
val letterOrDigit = InputFilter { source,  _, _, _, _, _->
    source.filter { c->
        (c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9')
    }
}

// InputFilter 不能输入表情符号
val withoutEmoji = InputFilter { source, _, _, _, _, _ ->
    source.filter { c ->
        val type = Character.getType(c)
        type != Character.SURROGATE.toInt() && type != Character.OTHER_SYMBOL.toInt()
    }
}

// InputFilter 只能输入字母和数字
val digit = InputFilter { source,  _, _, _, _, _->
    source.filter { c->
        (c in '0'..'9')
    }
}
