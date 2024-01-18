package com.example.myapplication.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.databinding.FragmentLoginBinding
import com.example.myapplication.model.Photo
import com.example.myapplication.model.Token
import com.example.myapplication.model.User
import com.example.myapplication.retrofit.RetrofitInit
import com.example.myapplication.viewModel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


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
            login(loginBinding.username.text.toString(),loginBinding.password.text.toString())
        }
        loginBinding.img.setOnClickListener{

            val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(pickImg)
        }

        loginBinding.registerAccount.setOnClickListener{
viewModel.replaceFragment(this,RegisterFragment(),"register")

        }

        return loginBinding.root
    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val imgUri = data?.data
                val path = imgUri?.path

                val path2 = Environment.getExternalStorageDirectory().path
                val myJpgPath = "/mnt/sdcard/Download/foto.jpg"
                val parcelFileDescriptor = imgUri?.let { it1 ->
                    this.requireContext().contentResolver.openFileDescriptor(
                        it1, "r", null)
                }
                val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                val file = File(this.requireContext().cacheDir, "dwadw")
                val outputStream = FileOutputStream(file)
                IOUtils.copy(inputStream, outputStream)







                uploadPhoto(file)

            }
        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(com.example.myapplication.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)

        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden){
            activity?.findViewById<BottomNavigationView>(com.example.myapplication.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)


        }

        }
        //super.onHiddenChanged(hidden)


    override fun onStart() {
        super.onStart()
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
private fun uploadPhoto(file : File){

    val call = RetrofitInit().photoService().uploadPhoto(

        image = MultipartBody.Part.createFormData("image",file.name,file.asRequestBody()),viewModel.user.value?.token?.token
    )
    call.enqueue(
        object : Callback<Photo> {
            override fun onFailure(call: Call<Photo>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@LoginFragment.context,"Error Uploading Image!",Toast.LENGTH_LONG).show()

            }
            override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                if(response.code() == 403){
                    Toast.makeText(this@LoginFragment.context,"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                }else{
                    response.body().let {
                        val options: RequestOptions = RequestOptions()
                            .centerCrop()
                            .placeholder(com.example.myapplication.R.drawable.loading_spinning)
                            .error(com.example.myapplication.R.mipmap.ic_launcher_round)
                        Glide.with(this@LoginFragment.requireContext()).load(it?.path).diskCacheStrategy(
                            DiskCacheStrategy.NONE).skipMemoryCache(true).apply(options).timeout(6000).into(loginBinding.imageView)

                    }

                }

            }
        }
    )
}
    private fun login(username: String,password:String){
        val call = RetrofitInit().userService().login(username, password)
        call.enqueue(
            object : Callback<Token> {
                override fun onFailure(call: Call<Token>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@LoginFragment.context,"Error Logging In",Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if(response.code() == 403  ){
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
                    getUser(username)
                }
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    response.body()?.let {
                        it.token=token
                        token=null
                        viewModel.setUser(it)
                        viewModel.setLoggedIn(true)

                    }



                }
            }
        )
    }
}