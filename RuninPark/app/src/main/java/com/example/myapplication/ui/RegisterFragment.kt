package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentRegisterBinding
import com.example.myapplication.viewModel.UserViewModel


class RegisterFragment : Fragment() {

    private lateinit var registerBinding: FragmentRegisterBinding
    private val viewModel: UserViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registerBinding= FragmentRegisterBinding.inflate(inflater,container,false)

        registerBinding.registerButton.setOnClickListener{
            viewModel.register(registerBinding.registerUsername.text.toString(),registerBinding.registerPassword.text.toString(),this)

        }

        return registerBinding.root
    }


}