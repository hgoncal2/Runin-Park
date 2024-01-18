package com.example.myapplication.viewModel

import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.model.Group
import com.example.myapplication.model.Photo
import com.example.myapplication.model.User
import com.example.myapplication.retrofit.RetrofitInit
import com.example.myapplication.ui.DashBoardFragment
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UserViewModel : ViewModel(){


var user = MutableLiveData<User?>()
    var loggedIn = MutableLiveData<Boolean>(false)
    var groups = MutableLiveData<List<Group>>()




    fun setUser(newUser: User?){
        user.value = newUser
    }
    fun setGroups(groups: List<Group>?){
        this.groups.value = groups
    }


    fun setLoggedIn(loggedIn : Boolean){
        this.loggedIn.value = loggedIn
    }

    fun replaceFragment(frag: Fragment,fragment: Fragment){

        val fragmentManager = frag.parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()

    }

    fun replaceDashboardFragment(frag: Fragment,fragment: Fragment){
        val fragmentManager = frag.parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.dashboard_placeholder,fragment)

        fragmentTransaction.commit()
    }

    fun removeFragment(fragment: Fragment){
        fragment.parentFragmentManager.popBackStack();
    }

     fun uploadPhoto(file : File,view: ImageView){

        val call = RetrofitInit().photoService().uploadPhoto(

            image = MultipartBody.Part.createFormData("image",file.name,file.asRequestBody()),user.value?.token?.token
        )
        call.enqueue(
            object : Callback<Photo> {
                override fun onFailure(call: Call<Photo>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        DashBoardFragment().requireContext(),
                        "Error Uploading Image!",
                        Toast.LENGTH_LONG
                    ).show()

                }

                override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                    if (response.code() == 403) {
                        Toast.makeText(
                            DashBoardFragment().requireContext(),
                            "Wrong Username or Password!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        response.body().let {
                            user.value?.profilePhoto = it?.path
                            val options: RequestOptions = RequestOptions()
                                .centerCrop()
                                .placeholder(com.example.myapplication.R.drawable.loading_spinning)
                                .error(com.example.myapplication.R.mipmap.ic_launcher_round)
                            Glide.with(DashBoardFragment().requireContext()).load(it?.path).diskCacheStrategy(
                                DiskCacheStrategy.NONE).skipMemoryCache(true).apply(options).timeout(6000).into(view)

                        }

                    }
                }
            }
        )
    }
}


