package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.Token
import com.example.myapplication.model.User


class MainActivity : AppCompatActivity() {
    lateinit var token:Token
    private lateinit var user: User
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(LoginFragment())


        binding.activityMainLayout.setOnClickListener{
            hideKeyboard(it)
        }


        binding.bottomNavView.setOnItemSelectedListener {

            when(it.itemId){
                R.id.item_groups -> replaceFragment(GroupsFragment())
                R.id.item_login -> replaceFragment(LoginFragment())
            }

        true
        }




    }

    companion object{
        fun Context.hideKeyboard(view: View) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()


    }








}