package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.Token
import com.example.myapplication.model.User
import com.example.myapplication.retrofit.RetrofitInit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    lateinit var token:Token
    private lateinit var user: User
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.username.setOnFocusChangeListener{v,hasFocus->
            if(hasFocus) hideKeyboard(v)
        }
        binding.activityMainLayout.setOnClickListener{
            hideKeyboard(it)
        }
        binding.btnLogin.setOnClickListener{
            hideKeyboard(it)
            login(binding.username.text.toString(),binding.password.text.toString())
        }






    }
   private fun Context.hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun login(username: String,password:String){
        val call = RetrofitInit().userService().login(username, password)
        call.enqueue(
            object : Callback<Token> {
                override fun onFailure(call: Call<Token>, t: Throwable,) {
                    t.printStackTrace()
                    Toast.makeText(this@MainActivity,"Wrong Username or Password!",Toast.LENGTH_LONG).show()

                }
                override fun onResponse( call: Call<Token>, response: Response<Token>) {
                    if(response.code() == 403){
                        Toast.makeText(this@MainActivity,"Wrong Username or Password!",Toast.LENGTH_LONG).show()
                    }else{
                        token= response.body()!!
                        Toast.makeText(this@MainActivity,"Welcome ${username}!",Toast.LENGTH_LONG).show()

                        getUser(username)
                    }

                }
            }
        )
    }
    private fun getUser(username: String){
        val call = RetrofitInit().userService().getUser(username)
        call.enqueue(
            object : Callback<User> {
                override fun onFailure(call: Call<User>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse( call: Call<User>, response: Response<User>) {
                    val user = response.body()
                    user?.token=token
                    print(user)

                }
            }
        )
    }




}