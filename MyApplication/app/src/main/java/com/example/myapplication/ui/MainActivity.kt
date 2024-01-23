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



        binding.activityMainLayout.setOnClickListener{
            hideKeyboard(it)
        }

        viewModel.loggedIn.observe(this, Observer {
            if(it==true ){
                login()
            }else{
                replaceFragment(LoginFragment())

            }
        })

        binding.bottomNavView.setOnItemSelectedListener {

            when(it.itemId){
                R.id.item_groups -> replaceFragment(GroupsFragment())
                R.id.item_login -> replaceFragment(LoginFragment())
                R.id.item_dashboard -> replaceFragment(DashBoardFragment())
                R.id.item_my_groups -> replaceFragment(GroupsFragment())
                R.id.item_logout -> logout()
            }

            true
        }

    }

    private fun logout(){
        viewModel.selectedGroup.value = null
        viewModel.setLoggedIn(false)
        viewModel.setUser(null)
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav)
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Toast.makeText(this,"Logged Out Successful!",Toast.LENGTH_LONG).show()
        viewModel.userGroups.value = null
        viewModel.allGroups.value = null

        val int = intent
        finish()
        startActivity(int)



    }

    private fun login(){
        //replaceFragment(GroupsFragment())


        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.loggedin_bottom_nav)
       viewModel.allGroups.value=null
        replaceFragment(DashBoardFragment())


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

        fragmentTransaction.addToBackStack(null).commit()

    }
    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount==1){
            this.moveTaskToBack(true);
        }else{
            supportFragmentManager.popBackStack()
        }

    }
    private fun removeFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment).commit()




    }








}
