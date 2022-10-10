package dev.vizualjack.staypositive

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
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
    public var payments = ArrayList<Payment>()

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
            val nextTime = LocalDate.parse(jsonEntry.getString("nextTime"))
            var lastTime: LocalDate? = null
            if (jsonEntry.has("lastTime"))
                lastTime = LocalDate.parse(jsonEntry.getString("lastTime"))
            val type = PaymentType.values()[jsonEntry.getInt("type")]
            payments.add(Payment(name, value, nextTime, lastTime, type))
        }
    }

    public fun save() {
        val entryArray = JSONArray()
        for (entry in payments) {
            val arrayEntry = JSONObject()
            arrayEntry.put("name", entry.name)
            arrayEntry.put("value", entry.value)
            arrayEntry.put("nextTime", entry.nextTime)
            arrayEntry.put("lastTime", entry.lastTime)
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

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
////        return when (item.itemId) {
////            R.id.action_settings -> true
////            else -> super.onOptionsItemSelected(item)
////        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}