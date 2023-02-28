package org.codebase.myam.ui.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.codebase.myam.BuildConfig

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData(0)

    private val _appList = MutableLiveData<List<String>?>()

    private val _pm = MutableLiveData<PackageManager>()

    fun setPm(pm: PackageManager) {
        _pm.value = pm
    }

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun getIndex(): Int {
        return _index.value ?: 0
    }

    fun getAppList(): LiveData<List<String>?> {
        val pm = _pm.value
        val comparator: Comparator<ApplicationInfo> = Comparator { o1, o2 ->
            val enabledSort = o1.enabled.compareTo(o2.enabled)
            if (enabledSort == 0) {
                if (pm != null) {
                    o1.loadLabel(pm).toString()
                        .compareTo(o2.loadLabel(pm).toString(), true)
                } else {
                    0
                }
            } else {
                enabledSort
            }
        }
        when (getIndex()) {
            0 -> {
                val appList = _pm.value?.getInstalledApplications(0)
                    ?.filter { a -> a.packageName != BuildConfig.APPLICATION_ID && (a.flags and ApplicationInfo.FLAG_SYSTEM) != 1 }
                    ?.sortedWith(comparator)
                _appList.value = appList?.map { i -> i.packageName }
            }
            1 -> {
                val appList = _pm.value?.getInstalledApplications(0)
                    ?.filter { a -> a.packageName != BuildConfig.APPLICATION_ID && (a.flags and ApplicationInfo.FLAG_SYSTEM) == 1 }
                    ?.sortedWith(comparator)
                _appList.value = appList?.map { i -> i.packageName }
            }
        }
        return _appList
    }

}