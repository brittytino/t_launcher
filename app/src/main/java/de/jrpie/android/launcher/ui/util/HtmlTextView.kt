package de.jrpie.android.launcher.ui.util

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class HtmlTextView(context: Context, attr: AttributeSet?, int: Int) :
    AppCompatTextView(context, attr, int) {
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context) : this(context, null, 0)

    init {
        @Suppress("deprecation") // required to support API level < 24
        text = Html.fromHtml(text.toString())
        movementMethod = LinkMovementMethod.getInstance()
    }
}