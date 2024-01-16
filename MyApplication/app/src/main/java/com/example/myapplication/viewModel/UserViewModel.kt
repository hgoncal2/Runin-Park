package com.example.myapplication.viewModel

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.example.myapplication.model.User

class UserViewModel : ViewModel(){

var user = MutableLiveData<User>()

    fun replaceFragment(frag: Fragment,fragment: Fragment){
        val fragmentManager = frag.parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()


    }

    fun removeFragment(fragment: Fragment){
        fragment.parentFragmentManager.popBackStack();
    }
}