package de.brittytino.android.launcher.ui.list.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.brittytino.android.launcher.databinding.ListOtherBinding

/**
 * The [ListFragmentOther] is used as a tab in ListActivity,
 * when the `intention` for launching the ListActivity was to select something to be launched.
 *
 * It lists `other` things like internal activities to be launched as an action.
 */
class ListFragmentOther : Fragment() {

    private lateinit var binding: ListOtherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListOtherBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        // set up the list / recycler
        val viewManager = LinearLayoutManager(context)
        val viewAdapter = OtherRecyclerAdapter(requireActivity())

        binding.listOtherRview.apply {
            // improve performance (since content changes don't change the layout size)
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        super.onStart()
    }
}
