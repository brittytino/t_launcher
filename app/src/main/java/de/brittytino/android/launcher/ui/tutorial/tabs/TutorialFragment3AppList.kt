package de.brittytino.android.launcher.ui.tutorial.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.ui.UIObject

/**
 * The [TutorialFragment3AppList] is a used as a tab in the TutorialActivity.
 *
 * Tells the user how his screen will look and how the app can be used
 */
class TutorialFragment3AppList : Fragment(), UIObject {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tutorial_3_app_list, container, false)
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

}
