package org.codebase.myam

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getAppList()
    }

    private fun getAppList() {
        val pm = packageManager
        val installedApplications = pm.getInstalledApplications(0)
        val appList = ArrayList(installedApplications)
            .filter { a -> !isSystemApp(a) }
            .filter { a -> a.packageName != BuildConfig.APPLICATION_ID }
            .map { a -> a.packageName }
        val recyclerView = findViewById<RecyclerView>(R.id.app_list)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = llm
        recyclerView.adapter =
            ApplicationAdapter(appList, pm)
    }

    private fun isSystemApp(applicationInfo: ApplicationInfo): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1
    }
}