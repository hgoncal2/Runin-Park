package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.viewModel.UserViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //replaceFragment(LoginFragment())


        binding.activityMainLayout.setOnClickListener{
            hideKeyboard(it)
        }

viewModel.loggedIn.observe(this, Observer {
    if(it==true ){
        login()
    }else{
        replaceFragment(LoginFragment(),"login")

    }
})

        binding.bottomNavView.setOnItemSelectedListener {

            when(it.itemId){
                R.id.item_groups -> replaceFragment(GroupsFragment(),"groups")
                R.id.item_login -> replaceFragment(LoginFragment(),"login")
                R.id.item_dashboard -> replaceFragment(DashBoardFragment(),"dashboard")
                R.id.item_my_groups -> replaceFragment(GroupsFragment(),"groups")
                R.id.item_logout -> logout()
            }

        true
        }

    }

    private fun logout(){
        viewModel.setLoggedIn(false)
        viewModel.setUser(null)
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav)
        removeFragment(DashBoardFragment())
        viewModel.loadGroups()
        Toast.makeText(this,"Logged Out Successful!",Toast.LENGTH_LONG).show()


    }

    private fun login(){

        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.loggedin_bottom_nav)
        replaceFragment(DashBoardFragment(),"dashboard")
        viewModel.user.value?.let { viewModel.loadUserGroups(it.userId) }

    }
    companion object{
        fun Context.hideKeyboard(view: View) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun replaceFragment(fragment: Fragment,tag:String = ""){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val  added_frags = fragmentManager.fragments;
        var added = false
        for(frag  in added_frags) {
            if(frag.tag == tag){
                added=true

                fragmentTransaction.show(frag)
                fragmentManager.popBackStack()

            } else{
                fragmentTransaction.hide(frag)
                fragmentTransaction.addToBackStack(null)


            }

        }

        if (!added) {
            fragmentTransaction.add(R.id.frame_layout, fragment,tag);
        }
        print(fragmentManager.backStackEntryCount)
        fragmentTransaction.addToBackStack(null).commit()

    }
    private fun removeFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment).commit()




    }








}