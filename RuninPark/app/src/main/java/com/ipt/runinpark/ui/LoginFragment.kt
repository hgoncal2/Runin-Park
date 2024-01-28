package com.ipt.runinpark.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ipt.runinpark.databinding.FragmentLoginBinding
import com.ipt.runinpark.model.Token
import com.ipt.runinpark.viewModel.UserViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var loginBinding: FragmentLoginBinding
    private  var token:Token? = null
    private val viewModel: UserViewModel by activityViewModels()






    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        loginBinding= FragmentLoginBinding.inflate(inflater,container,false)

        loginBinding.btnLogin.setOnClickListener{
            viewModel.login(loginBinding.username.text.toString(),loginBinding.password.text.toString(),this@LoginFragment.requireContext())
        }




        loginBinding.registerAccount.setOnClickListener{
            viewModel.replaceFragment(this,RegisterFragment(),"register")

        }

        return loginBinding.root
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(com.ipt.runinpark.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)

        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden){
            activity?.findViewById<BottomNavigationView>(com.ipt.runinpark.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)


        }

    }
    //super.onHiddenChanged(hidden)







}