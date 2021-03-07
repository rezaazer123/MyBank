package fr.rezaazer123.mybank

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.netguru.kissme.Kissme
import org.json.JSONException
import org.json.JSONObject

class AccountsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)

        val storage = Kissme(name = "storage")
        var btn_refresh = findViewById(R.id.btn_refresh) as Button
        var txt_name = findViewById(R.id.txt_name) as TextView
        var txt_lastname = findViewById(R.id.txt_lastname) as TextView
        var ll_accounts = findViewById(R.id.ll_accounts) as LinearLayout

        if(isOnline(this@AccountsActivity)){
            refresh()
        }
        else{
            Toast.makeText(this@AccountsActivity, "No internet connexion, data may be out of date", Toast.LENGTH_LONG).show()

            txt_name.text = storage.getString(key = "client_name", defaultValue = "")
            txt_lastname.text = storage.getString(key = "client_lastname", defaultValue = "")

            val ac = storage.getString(key = "accounts", defaultValue = "")
            val accounts = parser(ac.toString())

            for(acc in accounts){
                val textView = TextView(this)
                textView.text = acc.id + " | " + acc.accountName + "\n" + acc.amount + " | " + acc.currency + "\n" + acc.iban + "\n"
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                textView.layoutParams = params
                ll_accounts.addView(textView)
            }
        }

        btn_refresh.setOnClickListener {
            if (isOnline(this@AccountsActivity)){
                ll_accounts.removeAllViews()
            }
            refresh()
        }

    }

    fun refresh(){
        val storage = Kissme(name = "storage")
        var txt_name = findViewById(R.id.txt_name) as TextView
        var txt_lastname = findViewById(R.id.txt_lastname) as TextView
        var ll_accounts = findViewById(R.id.ll_accounts) as LinearLayout

        val httpAsync = "https://60102f166c21e10017050128.mockapi.io/config/1"
            .httpGet()
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        val data = result.get()
                        try {
                            val obj = JSONObject(data)

                            txt_name.text = obj.getString("name")
                            txt_lastname.text = obj.getString("lastname")

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        httpAsync.join()
        storage.putString(key = "client_name", value = txt_name.text.toString())
        storage.putString(key = "client_lastname", value = txt_lastname.text.toString())

        var ac = ""
        var accounts =  mutableListOf<Account>()

        val httpAsync2 = "https://60102f166c21e10017050128.mockapi.io/accounts"
            .httpGet()
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        val data = result.get()

                        try {
                            ac = data
                            accounts = parser(data) as MutableList<Account>

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        httpAsync2.join()
        for(acc in accounts){
            val textView = TextView(this)
            textView.text = acc.id + " | " + acc.accountName + "\n" + acc.amount + " | " + acc.currency + "\n" + acc.iban + "\n"
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            textView.layoutParams = params
            ll_accounts.addView(textView)
        }
        storage.putString(key = "accounts", value = ac)
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    class Account (
        var id: String = "",
        var accountName: String = "",
        var amount: String = "",
        var iban: String = "",
        var currency: String = "")

    fun parser(str: String): List<Account> {
        val lStrings = str.split('{','}')
        val accounts = mutableListOf<Account>()
        var del = true

        for(s in lStrings){
            if (!del) {
                val obj = JSONObject("{"+s+"}")
                accounts.add(Account(obj.getString("id"),obj.getString("accountName"),obj.getString("amount"),obj.getString("iban"),obj.getString("currency")))
            }
            del = !del
        }

        return accounts
    }

    
}