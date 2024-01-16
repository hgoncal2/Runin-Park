package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentRegisterBinding
import com.example.myapplication.model.APIResult
import com.example.myapplication.retrofit.RetrofitInit
import com.example.myapplication.viewModel.UserViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {

    private lateinit var registerBinding: FragmentRegisterBinding
    private val viewModel: UserViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        registerBinding= FragmentRegisterBinding.inflate(inflater,container,false)

        registerBinding.registerButton.setOnClickListener{
register(registerBinding.registerUsername.text.toString(),registerBinding.registerPassword.text.toString())

        }

        return registerBinding.root
    }

    private fun register(username: String,password:String){
        val call = RetrofitInit().userService().register(username, password)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@RegisterFragment.context,"Login", Toast.LENGTH_LONG).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(this@RegisterFragment.context,"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(this@RegisterFragment.context,"${result.description}!", Toast.LENGTH_LONG).show()
                            viewModel.removeFragment(this@RegisterFragment)
                        }else{
                            Toast.makeText(this@RegisterFragment.context,"Error!", Toast.LENGTH_LONG).show()

                        }



                    }

                }
            }
        )
    }
}