package de.brittytino.android.launcher.widgets

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import de.brittytino.android.launcher.ui.widgets.FavoritesView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("widget:favorites")
class FavoritesWidget(
    override var id: Int,
    override var position: WidgetPosition,
    override val panelId: Int,
    override var allowInteraction: Boolean = true,
    var favorites: MutableMap<Int, String> = mutableMapOf() // Slot Index -> "packageName|userId"
) : Widget() {

    override fun createView(activity: Activity): View {
        return FavoritesView(activity, this)
    }

    override fun findView(views: Sequence<View>): View? {
        return views.mapNotNull { it as? FavoritesView }.firstOrNull { it.widget.id == id }
    }

    override fun getPreview(context: Context): Drawable? {
        return null
    }

    override fun getIcon(context: Context): Drawable? {
        return null
    }

    override fun isConfigurable(context: Context): Boolean {
        return false // Can implement internal configuration later
    }

    override fun configure(activity: Activity, requestCode: Int) { }
}
