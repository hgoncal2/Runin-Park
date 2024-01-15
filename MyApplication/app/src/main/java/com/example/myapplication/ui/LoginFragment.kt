package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding
import com.example.myapplication.model.Token
import com.example.myapplication.model.User
import com.example.myapplication.retrofit.RetrofitInit
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private lateinit var token:Token




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

               onBackPressed()
            }
        })
        // Inflate the layout for this fragment
        loginBinding= FragmentLoginBinding.inflate(inflater,container,false)

        loginBinding.btnLogin.setOnClickListener{
            login(loginBinding.username.text.toString(),loginBinding.password.text.toString())
        }
        return loginBinding.root
    }

    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)

        super.onResume()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun login(username: String,password:String){
        val call = RetrofitInit().userService().login(username, password)
        call.enqueue(
            object : Callback<Token> {
                override fun onFailure(call: Call<Token>, t: Throwable,) {
                    t.printStackTrace()
                    Toast.makeText(this@LoginFragment.context,"Login",Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if(response.code() == 403){
                        Toast.makeText(this@LoginFragment.context,"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                    }else{
                        token= response.body()!!
                        Toast.makeText(this@LoginFragment.context,"Welcome ${username}!", Toast.LENGTH_LONG).show()

                        getUser(username)
                    }

                }
            }
        )
    }

    private fun onBackPressed(){

       if(requireActivity().supportFragmentManager.backStackEntryCount==1)
           requireActivity().finish()
        else{
            requireActivity().onBackPressedDispatcher.onBackPressed()
       }
    }
    private fun getUser(username: String){
        val call = RetrofitInit().userService().getUser(username)
        call.enqueue(
            object : Callback<User> {
                override fun onFailure(call: Call<User>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body()
                    user?.token=token
                    print(user)

                }
            }
        )
    }
}