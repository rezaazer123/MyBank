package fr.rezaazer123.mybank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.set

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result;
import com.netguru.kissme.Kissme
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val storage = Kissme(name = "storage")

        var txt_login_id = findViewById(R.id.txt_login_id) as EditText
        var btn_reset_id = findViewById(R.id.btn_reset_id) as Button
        var btn_login_id = findViewById(R.id.btn_login_id) as Button

        if (storage.getString(key = "id", defaultValue = "none") != "none"){
            try{
                txt_login_id.setText(storage.getString(key = "id", defaultValue = "none"))
            }catch (e: Exception){ }
        }
        else{
            storage.putString(key = "id", value = "none")
        }

        btn_reset_id.setOnClickListener {
            storage.putString(key = "id", value = "none")
            txt_login_id.setText("")
        }

        btn_login_id.setOnClickListener {
            if(txt_login_id.text.toString() != ""){
                if (txt_login_id.text.toString() == storage.getString(key = "id", defaultValue = "none")){
                    val intent = Intent(this, AccountsActivity::class.java)
                    startActivity(intent)
                }
                else if(!storage.contains(key = "id") || storage.getString(key = "id", defaultValue = "") == "none"){
                    storage.putString(key = "id", value = txt_login_id.text.toString())
                    val intent = Intent(this, AccountsActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this@MainActivity, "Wrong id", Toast.LENGTH_LONG).show()
                }
            }
            else {
                Toast.makeText(this@MainActivity, "Enter id", Toast.LENGTH_LONG).show()
            }



        }

    }

}
