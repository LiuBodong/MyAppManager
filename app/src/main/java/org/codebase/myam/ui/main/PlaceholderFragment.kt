package org.codebase.myam.ui.main

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.codebase.myam.ApplicationAdapter
import org.codebase.myam.databinding.FragmentAnotherBinding

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment(private val pm: PackageManager) : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentAnotherBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setPm(pm)
            if (savedInstanceState != null) {
                val index = savedInstanceState.getInt(ARG_SECTION_NUMBER, 0)
                setIndex(index)
            } else {
                val index = arguments?.getInt(ARG_SECTION_NUMBER, 0)
                setIndex(index ?: 0)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAnotherBinding.inflate(inflater, container, false)
        val root = binding.root

        val listView: RecyclerView = binding.appList2
        val appList = pageViewModel.getAppList()
        appList.observe(viewLifecycleOwner) { t ->
            val llm = LinearLayoutManager(binding.constraintLayout.context)
            llm.orientation = LinearLayoutManager.VERTICAL
            listView.layoutManager = llm
            listView.adapter = ApplicationAdapter(t ?: List(0) { "" }, pm)
        }
        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int, packageManager: PackageManager): PlaceholderFragment {
            return PlaceholderFragment(packageManager).apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}