package dev.vizualjack.staypositive

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import dev.vizualjack.staypositive.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val SAVE_FILE_NAME = "data"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    public var todayCash = 0f
    public var entries = ArrayList<Entry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        load()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    fun load() {
        val file = File(baseContext.filesDir, SAVE_FILE_NAME)
        if(!file.exists()) return
        val jsonString = BufferedReader(FileReader(file)).readText()
        val dataJson = JSONObject(jsonString)
        todayCash = dataJson.getString("todayCash").toFloat()
        val jsonEntries = dataJson.getJSONArray("entries")
        for (i in 0 until jsonEntries.length()) {
            val jsonEntry = jsonEntries.getJSONObject(i)
            val name = jsonEntry.getString("name")
            val value = jsonEntry.getString("value").toFloat()
            val startTime = LocalDate.parse(jsonEntry.getString("startTime"))
            val type = EntryType.values()[jsonEntry.getInt("type")]
            entries.add(Entry(name, value, startTime, type))
        }
    }

    public fun save() {
        val entryArray = JSONArray()
        for (entry in entries) {
            val arrayEntry = JSONObject()
            arrayEntry.put("name", entry.name)
            arrayEntry.put("value", entry.value)
            arrayEntry.put("startTime", entry.startTime)
            arrayEntry.put("type", entry.type!!.ordinal)
            entryArray.put(arrayEntry)
        }
        val dataJson = JSONObject()
        dataJson.put("todayCash", todayCash)
        dataJson.put("entries", entryArray)
        baseContext.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE).use {
            it?.write(dataJson.toString().toByteArray())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}