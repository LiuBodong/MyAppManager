package org.codebase.myam.ui.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.codebase.myam.ApplicationAdapter
import org.codebase.myam.BuildConfig
import org.codebase.myam.databinding.FragmentAnotherBinding
import java.text.Collator
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment(private val pm: PackageManager, private val textView: TextView) :
    Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentAnotherBinding? = null
    private lateinit var comparator: Comparator<ApplicationInfo>

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
        setAppList()
        textView.addTextChangedListener(MyTextWatcher())
    }

    private fun setAppList() {
        comparator = Comparator { o1, o2 ->
            val enabledSort = o1.enabled.compareTo(o2.enabled)
            if (enabledSort == 0) {
                val s1 = o1.loadLabel(pm).toString()
                val s2 = o2.loadLabel(pm).toString()
                if (s1[0].isLetterOrDigit() && s2[0].isLetterOrDigit()) {
                    s1.compareTo(s2)
                } else if (s1[0].isLetterOrDigit() && !s2[0].isLetterOrDigit()) {
                    +1
                } else if (!s1[0].isLetterOrDigit() && s2[0].isLetterOrDigit()) {
                    -1
                } else {
                    Collator.getInstance(Locale.SIMPLIFIED_CHINESE)
                        .compare(s1, s2)
                }
            } else {
                enabledSort
            }
        }
        when (pageViewModel.getIndex()) {
            0 -> {
                val appList = pm.getInstalledApplications(0)
                    .filter { a -> a.packageName != BuildConfig.APPLICATION_ID && (a.flags and ApplicationInfo.FLAG_SYSTEM) != 1 }
                    .sortedWith(comparator)
                pageViewModel.setAppList(appList)
                pageViewModel.setFilteredAppList(appList)
            }
            1 -> {
                val appList = pm.getInstalledApplications(0)
                    .filter { a -> a.packageName != BuildConfig.APPLICATION_ID && (a.flags and ApplicationInfo.FLAG_SYSTEM) == 1 }
                    .sortedWith(comparator)
                pageViewModel.setAppList(appList)
                pageViewModel.setFilteredAppList(appList)
            }
        }
    }

    inner class MyTextWatcher :
        TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            val searh = s.toString().trim()
            var applicationInfos = pageViewModel.getAppList().value!!
            if (searh != null && searh.isNotEmpty() && searh.isNotBlank()) {
                applicationInfos = pageViewModel.getAppList().value!!.filter { a ->
                    a.packageName.contains(
                        searh,
                        true
                    ) || a.loadLabel(pm).contains(searh, true)
                }.sortedWith(comparator)
            }
            pageViewModel.setFilteredAppList(applicationInfos)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAnotherBinding.inflate(inflater, container, false)
        val root = binding.root

        val listView: RecyclerView = binding.appList2
        val appList = pageViewModel.getFilteredAppList()
        appList.observe(viewLifecycleOwner) { t ->
            val llm = LinearLayoutManager(binding.constraintLayout.context)
            llm.orientation = LinearLayoutManager.VERTICAL
            listView.layoutManager = llm
            listView.adapter = ApplicationAdapter(t!!.map { a -> a.packageName }, pm)
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
        fun newInstance(
            sectionNumber: Int,
            packageManager: PackageManager,
            textView: TextView
        ): PlaceholderFragment {
            return PlaceholderFragment(packageManager, textView).apply {
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

    override fun onPause() {
        super.onPause()
        setAppList()
    }
}
