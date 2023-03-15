package org.codebase.myam

import android.app.AlertDialog
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

    private fun disableApp(packageName: String): Int {
        return execCmd("pm disable-user --user 0 $packageName\n")
    }

    private fun enableApp(packageName: String): Int {
        return execCmd("pm enable --user 0 $packageName\n")
    }

    private fun execCmd(cmd: String): Int {
        val process = Runtime.getRuntime().exec("su")
        val out = DataOutputStream(process.outputStream)
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        out.writeBytes(cmd)
        out.flush()
        out.writeBytes("exit\n")
        out.flush()
        while (bufferedReader.ready()) {
            val line = bufferedReader.readLine() ?: break
            Log.i(TAG, line)
        }
        bufferedReader.close()
        return process.waitFor()
    }

    override fun onBindViewHolder(holder: ApplicationAdapter.ViewHolder, position: Int) {
        val packageName = appList[position]
        val app = packageManager!!.getApplicationInfo(packageName, 0)
        val icon = app.loadIcon(packageManager)
        holder.icon.setImageDrawable(icon)
        val appName = app.loadLabel(packageManager)
        holder.name.text = appName
        if (!app.enabled) {
            holder.name.setTextColor(Color.RED)
        } else {
            holder.name.setTextColor(Color.parseColor("#3f8b00"))
        }
        holder.icon.setOnLongClickListener { view: View ->
            AlertDialog.Builder(view.context)
                .setTitle("Confirm")
                .setMessage("Are you sure to uninstall $appName?")
                .setPositiveButton("Yes") { dialog, _ ->
                    val uninstallRes = execCmd("pm uninstall --user 0 $packageName")
                    if (uninstallRes == 0) {
                        Toast.makeText(
                            view.context,
                            "Successfully uninstalled $appName",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        Toast.makeText(
                            view.context,
                            "Failed to uninstall $appName",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            true
        }
        holder.name.setOnLongClickListener { view: View ->
            val info = packageManager.getApplicationInfo(packageName, 0)
            if (!info.enabled) {
                enableApp(info.packageName)
            }
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null) {
                Toast.makeText(
                    holder.name.context,
                    "No Launcher found for package $packageName",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                view.context.startActivity(intent)
            }
            true
        }
        holder.name.setOnClickListener { view: View ->
            val info = packageManager.getApplicationInfo(packageName, 0)
            val exitVal =
                if (info.enabled) {
                    disableApp(packageName)
                } else {
                    enableApp(packageName)
                }
            if (exitVal == 0) {
                val newInfo = packageManager.getApplicationInfo(packageName, 0)
                if (newInfo.enabled != info.enabled) {
                    if (view is TextView) {
                        if (newInfo.enabled) {
                            view.setTextColor(Color.parseColor("#3f8b00"))
                        } else {
                            view.setTextColor(Color.RED)
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
