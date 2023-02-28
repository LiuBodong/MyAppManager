package org.codebase.myam

import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class ApplicationAdapter(
    private val appList: List<String>,
    private val packageManager: PackageManager?
) :
    RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

    private val TAG: String = BuildConfig.APPLICATION_ID

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById<ImageView>(R.id.app_icon)
        val name: TextView = view.findViewById<TextView>(R.id.app_name)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.application_element, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appList.size
    }

    override fun onBindViewHolder(holder: ApplicationAdapter.ViewHolder, position: Int) {
        val packageName = appList[position]
        val app = packageManager!!.getApplicationInfo(packageName, 0)
        val icon = app.loadIcon(packageManager)
        holder.icon.setImageDrawable(icon)
        holder.name.text = app.loadLabel(packageManager)
        if (!app.enabled) {
            holder.name.setTextColor(Color.DKGRAY)
        } else {
            holder.name.setTextColor(Color.WHITE)
        }
        holder.name.setOnClickListener { view: View ->
            val info = packageManager.getApplicationInfo(packageName, 0)
            val process = Runtime.getRuntime().exec("su")
            val out = DataOutputStream(process.outputStream)
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            if (info.enabled) {
                out.writeBytes("pm disable-user --user 0 $packageName\n")
            } else {
                out.writeBytes("pm enable --user 0 $packageName\n")
            }
            out.flush()
            out.writeBytes("exit\n")
            out.flush()
            while (bufferedReader.ready()) {
                val line = bufferedReader.readLine() ?: break
                Log.i(TAG, line)
            }
            bufferedReader.close()
            val exitVal = process.waitFor()
            if (exitVal == 0) {
                val newInfo = packageManager.getApplicationInfo(packageName, 0)
                if (newInfo.enabled != info.enabled) {
                    if (view is TextView) {
                        if (newInfo.enabled) {
                            view.setTextColor(Color.WHITE)
                        } else {
                            view.setTextColor(Color.DKGRAY)
                        }
                    }
                    Toast.makeText(
                        holder.name.context,
                        "Switch $packageName to ${if (newInfo.enabled) "Enabled" else "Disabled"}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(holder.name.context, "Switch failed!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    holder.name.context,
                    "Switch failed! exit code = $exitVal",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}