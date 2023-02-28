package org.codebase.myam.ui.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData(0)

    private val _appList = MutableLiveData<List<ApplicationInfo>>()

    private val _pm = MutableLiveData<PackageManager>()

    private val _filteredAppList =  MutableLiveData<List<ApplicationInfo>>()

    fun setPm(pm: PackageManager) {
        _pm.value = pm
    }

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun getIndex(): Int {
        return _index.value ?: 0
    }

    fun getAppList(): LiveData<List<ApplicationInfo>> {
        return _appList
    }

    fun setAppList(appList: List<ApplicationInfo>) {
        _appList.value = appList
    }

    fun getFilteredAppList(): LiveData<List<ApplicationInfo>> {
        return _filteredAppList
    }

    fun setFilteredAppList(applicationInfos: List<ApplicationInfo>) {
        _filteredAppList.value = applicationInfos
    }

}