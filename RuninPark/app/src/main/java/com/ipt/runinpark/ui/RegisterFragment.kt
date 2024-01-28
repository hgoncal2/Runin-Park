package com.ipt.runinpark.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ipt.runinpark.R
import com.ipt.runinpark.databinding.FragmentRegisterBinding
import com.ipt.runinpark.viewModel.UserViewModel


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
    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)
        super.onResume()
    }

}