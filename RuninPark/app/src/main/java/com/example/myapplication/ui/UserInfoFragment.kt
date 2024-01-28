package com.example.myapplication.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.myapplication.databinding.FragmentUserInfoBinding
import com.example.myapplication.model.User
import com.example.myapplication.viewModel.UserViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserInfoFragment : Fragment() {
    private lateinit var newUser: User
    private lateinit var userInfoBinding: FragmentUserInfoBinding
    private val viewModel: UserViewModel by activityViewModels()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userInfoBinding = FragmentUserInfoBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment

        loadUserInfo()
        userInfoBinding.name.keyListener = null;
        userInfoBinding.lastName.keyListener = null;
        userInfoBinding.birthDate.keyListener = null;
        userInfoBinding.weight.keyListener = null;
        userInfoBinding.height.keyListener = null;
        userInfoBinding.address.keyListener = null;


        userInfoBinding.editName.setOnClickListener{
            userInfoBinding.name.inputType = InputType.TYPE_CLASS_TEXT

            userInfoBinding.name.setSelection(userInfoBinding.name.text.length)
            viewModel.showKeyboard(this)
            userInfoBinding.name.requestFocus()


        }
        userInfoBinding.editLastName.setOnClickListener{
            userInfoBinding.lastName.inputType = InputType.TYPE_CLASS_TEXT

            userInfoBinding.lastName.setSelection(userInfoBinding.lastName.text.length)
            viewModel.showKeyboard(this)
            userInfoBinding.lastName.requestFocus()

        }
        userInfoBinding.editBirthDate.setOnClickListener{
            userInfoBinding.birthDate.inputType = InputType.TYPE_CLASS_DATETIME

            userInfoBinding.birthDate.text?.let { it1 -> userInfoBinding.birthDate.setSelection(it1.length) }
            viewModel.showKeyboard(this)
            userInfoBinding.birthDate.requestFocus()
            userInfoBinding.birthDate.focusable= View.FOCUSABLE
        }


        userInfoBinding.editWeight.setOnClickListener{
            userInfoBinding.weight.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL

            userInfoBinding.weight.text?.let { it1 -> userInfoBinding.weight.setSelection(it1.length) }
            viewModel.showKeyboard(this)
            userInfoBinding.weight.requestFocus()

        }
        userInfoBinding.editHeight.setOnClickListener{
            userInfoBinding.height.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL

            userInfoBinding.height.text?.let { it1 -> userInfoBinding.height.setSelection(it1.length) }
            viewModel.showKeyboard(this)
            userInfoBinding.height.requestFocus()

        }

        userInfoBinding.editAddress.setOnClickListener{
            userInfoBinding.address.inputType = InputType.TYPE_CLASS_TEXT
            userInfoBinding.address.text?.let { it1 -> userInfoBinding.address.setSelection(it1.length) }
            viewModel.showKeyboard(this)
            userInfoBinding.address.requestFocus()
        }
        userInfoBinding.save.setOnClickListener{
            newUser=viewModel.user.value!!

            userInfoBinding.birthDate.text?.let {
                if(it.isNotEmpty()){
                    try{
                        newUser.birthDate=viewModel.dateFormatter.parse(it.toString())
                    }catch (e: java.text.ParseException){
                        Toast.makeText(this@UserInfoFragment.requireContext(),"Por favor insira uma data válida!",Toast.LENGTH_SHORT).show()
                    }

                }
            }
          //  newUser.birthDate=viewModel.dateFormatter.parse(userInfoBinding.birthDate.text.toString())
            userInfoBinding.name.text.let {
                if(it.isNotEmpty() && it.isNotBlank()){
                    newUser.name=it.toString()
                }
            }
            //newUser.name=userInfoBinding.name.text.toString()
            userInfoBinding.lastName.text.let {
                if(it.isNotEmpty() && it.isNotBlank()){
                    newUser.lastName=it.toString()
                }
            }
            //newUser.lastName=userInfoBinding.lastName.text.toString()
            userInfoBinding.weight.text.let{
                if(it.isNotEmpty() && it.isNotBlank()){
                    if(it.contains(',')){
                        newUser.weight = it.toString().replace(',','.').toDoubleOrNull()
                    }else{
                        newUser.weight = it.toString().toDoubleOrNull()
                    }

                    newUser.weight?.let {  } ?: Toast.makeText(this@UserInfoFragment.requireContext(),"Por favor insira um peso válido!",Toast.LENGTH_SHORT).show()
                }
            }
           // newUser.weight=userInfoBinding.weight.text.toString().toDoubleOrNull()
            //newUser.weight?.let {  } ?: Toast.makeText(this@UserInfoFragment.requireContext(),"Por favor insira um peso válido!",Toast.LENGTH_SHORT).show()
            userInfoBinding.height.text.let{
                if (it != null) {
                    if(it.isNotEmpty() && it.isNotBlank()){
                        newUser.height = it.toString().toDoubleOrNull()
                        newUser.height?.let {  } ?: Toast.makeText(this@UserInfoFragment.requireContext(),"Por favor insira uma altura válida!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
           // newUser.height=userInfoBinding.height.text.toString().toDoubleOrNull()
            //newUser.height?.let{ } ?: Toast.makeText(this@UserInfoFragment.requireContext(),"Por favor insira uma altura válida!",Toast.LENGTH_SHORT).show()
            newUser.address=userInfoBinding.address.text.toString()
viewModel.updateUser(newUser,this)




        }
        return userInfoBinding.root
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserInfo(){
        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let {

                it.name?.let { v -> userInfoBinding.name.setText(v) } ?: userInfoBinding.name.setText("")
                it.lastName?.let{v -> userInfoBinding.lastName.setText(v)} ?: userInfoBinding.lastName.setText("")
                it.birthDate?.let{v -> userInfoBinding.birthDate.setText(viewModel.dateFormatter.format(v))} ?: userInfoBinding.birthDate.setText("Valor não preenchido")
                it.weight?.let{v -> userInfoBinding.weight.setText("${v}")} ?: userInfoBinding.weight.setText("")
                it.height?.let{v -> userInfoBinding.height.setText("${v}")} ?: userInfoBinding.height.setText("")
                it.address?.let{v -> userInfoBinding.address.setText(v)} ?: userInfoBinding.address.setText("")
            }



        })


    }








}