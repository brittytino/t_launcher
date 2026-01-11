package de.jrpie.android.launcher.ui.tutorial.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.databinding.Tutorial1ConceptBinding
import de.jrpie.android.launcher.ui.UIObject

/**
 * The [TutorialFragment1Concept] is a used as a tab in the TutorialActivity.
 *
 * It is used to display info about Launchers concept (open source, efficiency ...)
 */
class TutorialFragment1Concept : Fragment(), UIObject {
    private lateinit var binding: Tutorial1ConceptBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = Tutorial1ConceptBinding.inflate(inflater, container, false)
        binding.tutorialConceptBadgeVersion.text = BuildConfig.VERSION_NAME
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

}
