package com.example.myapplication.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.User

class UserViewModel : ViewModel(){

var user = MutableLiveData<User>()


}