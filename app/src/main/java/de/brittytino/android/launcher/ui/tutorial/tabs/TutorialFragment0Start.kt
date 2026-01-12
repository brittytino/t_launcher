package de.brittytino.android.launcher.ui.tutorial.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.brittytino.android.launcher.databinding.Tutorial0StartBinding
import de.brittytino.android.launcher.ui.UIObject

/**
 * The [TutorialFragment0Start] is a used as a tab in the TutorialActivity.
 *
 * It displays info about the app and gets the user into the tutorial
 */
class TutorialFragment0Start : Fragment(), UIObject {

    private lateinit var binding: Tutorial0StartBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = Tutorial0StartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }
}