package dev.vizualjack.staypositive

import android.content.Context
import android.os.Bundle
import android.view.Menu
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
    public var accounts = ArrayList<Account>()
    public var selectedAccount: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        load()
        for (account in accounts) {
            account.cash = PaymentUtil.calculatePast(account.payments!!, account.cash!!)
            account.cash = Util.roundToCashLikeValue(account.cash!!)
        }
        save()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun checkAndRecreateDataJson(dataJson: JSONObject):Boolean {
        if (!dataJson.has("todayCash")) return false
        val todayCash = dataJson.getString("todayCash").toFloat()
        val paymentsArray = dataJson.getJSONArray("entries")
        val accountObject = JSONObject()
        accountObject.put("name", "Default")
        accountObject.put("cash", todayCash)
        accountObject.put("payments", paymentsArray)
        val accountsArray = JSONArray()
        accountsArray.put(accountObject)
        dataJson.put("accounts", accountsArray)
        dataJson.remove("todayCash")
        dataJson.remove("entries")
        return true
    }

    fun load() {
        val file = File(baseContext.filesDir, SAVE_FILE_NAME)
        if(!file.exists()) return
        var jsonString = BufferedReader(FileReader(file)).readText()
        jsonString = jsonString.replace("ONES", "0").replace("MONTHLY", "1")
        val dataJson = JSONObject(jsonString)
        val recreated = checkAndRecreateDataJson(dataJson)
        val accountsArray = dataJson.getJSONArray("accounts")
        for (i in 0 until accountsArray.length()) {
            val accountObject = accountsArray.getJSONObject(i)
            val accountName = accountObject.getString("name")
            val cash = accountObject.getString("cash").toFloat()
            val paymentsArray = accountObject.getJSONArray("payments")
            val payments = ArrayList<Payment>()
            for (i in 0 until paymentsArray.length()) {
                val paymentObject = paymentsArray.getJSONObject(i)
                val name = paymentObject.getString("name")
                val value = paymentObject.getString("value").toFloat()
                val nextTime = LocalDate.parse(paymentObject.getString("nextTime"))
                var lastTime: LocalDate? = null
                if (paymentObject.has("lastTime"))
                    lastTime = LocalDate.parse(paymentObject.getString("lastTime"))
                val type = PaymentType.values()[paymentObject.getInt("type")]
                payments.add(Payment(name, value, nextTime, lastTime, type))
            }
            accounts.add(Account(accountName, cash, payments))
        }
        if (recreated) save()
    }

    fun save() {
        val accountsArray = JSONArray()
        for (account in accounts) {
            val accountObject = JSONObject()
            accountObject.put("name", account.name)
            accountObject.put("cash", account.cash)
            val paymentsArray = JSONArray()
            for (payment in account.payments!!) {
                val paymentObject = JSONObject()
                paymentObject.put("name", payment.name)
                paymentObject.put("value", payment.value)
                paymentObject.put("nextTime", payment.nextTime)
                paymentObject.put("lastTime", payment.lastTime)
                paymentObject.put("type", payment.type!!.ordinal)
                paymentsArray.put(paymentObject)
            }
            accountObject.put("payments", paymentsArray)
            accountsArray.put(accountObject)
        }
        val dataJson = JSONObject()
        dataJson.put("accounts", accountsArray)
        baseContext.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE).use {
            it?.write(dataJson.toString().toByteArray())
        }
    }

//    public fun Oldsave() {
//        val entryArray = JSONArray()
//        for (entry in payments) {
//            val arrayEntry = JSONObject()
//            arrayEntry.put("name", entry.name)
//            arrayEntry.put("value", entry.value)
//            arrayEntry.put("nextTime", entry.nextTime)
//            arrayEntry.put("lastTime", entry.lastTime)
//            arrayEntry.put("type", entry.type!!.ordinal)
//            entryArray.put(arrayEntry)
//        }
//        val dataJson = JSONObject()
//        dataJson.put("todayCash", todayCash)
//        dataJson.put("entries", entryArray)
//        baseContext.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE).use {
//            it?.write(dataJson.toString().toByteArray())
//        }
//    }

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