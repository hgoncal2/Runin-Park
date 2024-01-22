package com.example.myapplication.viewModel

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.model.APIResult
import com.example.myapplication.model.Group
import com.example.myapplication.model.Photo
import com.example.myapplication.model.Token
import com.example.myapplication.model.User
import com.example.myapplication.retrofit.RetrofitInit
import com.example.myapplication.ui.DashBoardFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat

class UserViewModel : ViewModel(){


    var user = MutableLiveData<User?>()
    var loggedIn = MutableLiveData<Boolean>(false)
    var selectedGroup = MutableLiveData<Group>()
    var userGroups = MutableLiveData<List<Group>>()

    var allGroups= MutableLiveData<List<Group>>()
    var adapterGroups = mutableListOf<Group>()
    var _groupsFiltered = MutableStateFlow(0)
    var groupsFiltered = _groupsFiltered.asStateFlow()

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    lateinit var token : Token



    fun setUser(newUser: User?){
        user.value = newUser
    }

    fun login(username: String,password:String,context: Context){
        val call = RetrofitInit().userService().login(username, password)
        call.enqueue(
            object : Callback<Token> {
                override fun onFailure(call: Call<Token>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(context,"Error Logging In",Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if(response.code() == 403  ){
                        Toast.makeText(context,"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                    }else{
                        token= response.body()!!
                        Toast.makeText(context,"Welcome ${username}!", Toast.LENGTH_LONG).show()

                        getUser(username)
                    }

                }
            }
        )
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
                        token= Token(null)
                        setUser(it)
                        setLoggedIn(true)

                    }



                }
            }
        )
    }

    fun joinGroup(groupId: Int,fragment: Fragment){
        val call = RetrofitInit().userService().addUserToGroup(user.value?.token?.token, groupId)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(fragment.requireContext(),"Error joining group!", Toast.LENGTH_LONG).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(fragment.requireContext(),"Error joining group!", Toast.LENGTH_LONG).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
user.value?.let{loadUserGroups(it.userId)}

                        }else{
                            Toast.makeText(fragment.requireContext(),"Error joining group!", Toast.LENGTH_LONG).show()

                        }



                    }

                }
            }
        )
    }
    fun register(username: String,password:String,fragment: Fragment){
        val call = RetrofitInit().userService().register(username, password)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(fragment.requireContext(),"Login", Toast.LENGTH_LONG).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(fragment.requireContext(),"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
                            removeFragment(fragment)
                        }else{
                            Toast.makeText(fragment.requireContext(),"Error!", Toast.LENGTH_LONG).show()

                        }



                    }

                }
            }
        )
    }
    fun updateUser(user:User,fragment: Fragment){
        val call= RetrofitInit().userService().updateUser(user.token?.token,user,user.username)
        call?.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(fragment.requireContext(),"Login", Toast.LENGTH_LONG).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(fragment.requireContext(),"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
                            setUser(user)
                        }else{
                            Toast.makeText(fragment.requireContext(),"Error!", Toast.LENGTH_LONG).show()

                        }



                    }

                }
            }
        )
    }



    fun loadGroups(dialog : String? = null){

        val call = RetrofitInit().groupService().getGroups()
        call.enqueue(
            object : Callback<List<Group>> {
                override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                    dialog?.let { allGroups.value = response.body()
                                            _groupsFiltered.value=1

                    }
                        ?: setAllGroups(response.body())


                }
            }
        )
    }

    fun loadUserGroups(userId : Int){

        val call = RetrofitInit().groupService().getUserGroups(userId)
        call.enqueue(
            object : Callback<List<Group>> {
                override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {

                    setUserGroups(response.body())



                }
            }
        )

    }
    fun setAllGroups(groups: List<Group>?){
        adapterGroups.clear()
        if (groups != null) {
            adapterGroups.addAll(groups)
        }
        this.allGroups.value = adapterGroups
    }
    fun setUserGroups(groups: List<Group>?){
        adapterGroups.clear()
        if (groups != null) {
            adapterGroups.addAll(groups)
        }
        this.userGroups.value = adapterGroups
    }


    fun setLoggedIn(loggedIn : Boolean){
        this.loggedIn.value = loggedIn
    }

    fun replaceFragment(currentFragment: Fragment,fragment: Fragment,tag:String = ""){
        val fragmentManager = currentFragment.parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frame_layout,fragment)

        fragmentTransaction.addToBackStack(null).commit()

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

     fun loadPic(path: String,imageView: ImageView,cache: Boolean,fragment: Fragment,drawable: Int){
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(com.example.myapplication.R.drawable.loading_spinning)
            .error(drawable)
            .circleCrop()
        Glide.with(fragment.requireContext()).load(path).diskCacheStrategy(
            DiskCacheStrategy.NONE).skipMemoryCache(cache).apply(options).timeout(10000).into(imageView)

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
                            "Error uploading pictutre!",
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

    public fun showKeyboard(fragment: Fragment){
        fragment.view?.requestFocus()
        val activity = fragment.activity
        val view = activity?.currentFocus
        val methodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        assert(view != null)
        methodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

    }
}


