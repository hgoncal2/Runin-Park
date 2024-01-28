package com.ipt.runinpark.viewModel

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ipt.runinpark.R
import com.ipt.runinpark.model.APIResult
import com.ipt.runinpark.model.Group
import com.ipt.runinpark.model.Photo
import com.ipt.runinpark.model.Post
import com.ipt.runinpark.model.Run
import com.ipt.runinpark.model.Token
import com.ipt.runinpark.model.User
import com.ipt.runinpark.retrofit.RetrofitInit
import com.ipt.runinpark.ui.DashBoardFragment
import com.ipt.runinpark.ui.GroupsFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat



class UserViewModel() : ViewModel(){


    var user = MutableLiveData<User?>()
    var loggedIn = MutableLiveData<Boolean>(false)
    var selectedGroup = MutableLiveData<Group>()
    var selectedUser = MutableLiveData<User>()
    var userGroups = MutableLiveData<List<Group>>()
    var groupPosts = MutableLiveData<List<Post>>()
    var groupRuns = MutableLiveData<List<Run>>()
    var groupMembers = MutableLiveData<List<User>>()
    var allGroups= MutableLiveData<List<Group>>()
    var adapterGroups = mutableListOf<Group>()
    var _groupsFiltered = MutableStateFlow(0)
    lateinit var savedToken : Flow<Token>
    var groupsFiltered = _groupsFiltered.asStateFlow()
    var userOwnedGroups = mutableListOf<Group>()
    var userPosts = MutableLiveData<List<Post>>()

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
                    Toast.makeText(context,"Erro a dar login",Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if(response.code() == 403  ){
                        Toast.makeText(context,"Username ou password errada!", Toast.LENGTH_LONG).show()
                    }else{
                        token= response.body()!!
                        Toast.makeText(context,"Bem-Vindo ${username}!", Toast.LENGTH_LONG).show()

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

     fun getUserWithToken(token: String,context: Context?=null){
        val call = RetrofitInit().userService().getUserWithToken(token)
        call.enqueue(
            object : Callback<User> {
                override fun onFailure(call: Call<User>, t: Throwable) {
                    t.printStackTrace()
                    getUserWithToken(token)
                }
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    response.body()?.let {
                        it.token=Token(token)
                        Toast.makeText(context,"User autenticado com sucesso!",Toast.LENGTH_SHORT)
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
                    Toast.makeText(fragment.requireContext(),"Erro ao juntar-se a grupo!", Toast.LENGTH_LONG).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(fragment.requireContext(),"Erro ao juntar-se a grupo!", Toast.LENGTH_LONG).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
user.value?.let{loadUserGroups(it.userId)}

                        }else{
                            Toast.makeText(fragment.requireContext(),"Erro ao juntar-se a grupo!", Toast.LENGTH_LONG).show()

                        }



                    }

                }
            }
        )
    }


    fun leaveGroup(groupId: Int,fragment: Fragment){
        val call = RetrofitInit().userService().removeUserFromGroup(user.value?.token?.token, groupId)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(fragment.requireContext(),"Erro ao juntar-se a grupo!", Toast.LENGTH_SHORT).show()
                    user.value?.userId?.let { loadUserGroups(it) }
                    replaceFragment(fragment,GroupsFragment())

                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(fragment.requireContext(),"Erro ao juntar-se a grupo!", Toast.LENGTH_SHORT).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_SHORT).show()
                            if(user.value?.userId == selectedGroup.value?.ownerId){
                                replaceFragment(fragment,GroupsFragment())
                            }
                            user.value?.let{loadUserGroups(it.userId)}

                        }else{
                            Toast.makeText(fragment.requireContext(),"${result?.description}", Toast.LENGTH_SHORT
                            ).show()

                        }



                    }

                }
            }
        )
    }
    fun createGroup(name:String,city : String,fragment: Fragment){
        val call = RetrofitInit().groupService().createGroup(user.value?.token?.token,name,city)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(fragment.requireContext(),"Error ao criar grupo!", Toast.LENGTH_SHORT).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {

                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_SHORT).show()
                            user.value?.let{loadUserGroups(it.userId)}





                        }else{
                            Toast.makeText(fragment.requireContext(),"${result?.description}", Toast.LENGTH_SHORT
                            ).show()





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
                    Toast.makeText(fragment.requireContext(),"Entrar", Toast.LENGTH_LONG).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(fragment.requireContext(),"Username ou password errada!", Toast.LENGTH_LONG).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
                            removeFragment(fragment)
                        }else{
                            Toast.makeText(fragment.requireContext(),"Erro!", Toast.LENGTH_LONG).show()

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
                    Toast.makeText(fragment.requireContext(),"Entrar", Toast.LENGTH_LONG).show()


                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    if(response.code() == 403){
                        Toast.makeText(fragment.requireContext(),"Username ou password errada!", Toast.LENGTH_LONG).show()
                    }else{
                        val result : APIResult? = response.body()
                        if(result?.code=="200"){
                            Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
                            setUser(user)
                        }else{
                            Toast.makeText(fragment.requireContext(),"${result?.description}", Toast.LENGTH_LONG).show()

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
    fun loadPosts(groupId : Int){

        val call = RetrofitInit().postService().getPosts(groupId)
        call.enqueue(
            object : Callback<List<Post>> {
                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {

                    groupPosts.value = response.body()


                }
            }
        )
    }
    fun loadRuns(groupId : Int){

        val call = RetrofitInit().runService().getRuns(groupId)
        call.enqueue(
            object : Callback<List<Run>> {
                override fun onFailure(call: Call<List<Run>>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<List<Run>>, response: Response<List<Run>>) {

                    groupRuns.value = response.body()


                }
            }
        )
    }
    fun loadGroupMembers(groupId : Int){

        val call = RetrofitInit().groupService().getGroupMembers(groupId)
        call.enqueue(
            object : Callback<List<User>> {
                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {

                    groupMembers.value = response.body()


                }
            }
        )
    }

    fun removeUserFromGroup(token:Token,groupId : Int,userId : Int,fragment: Fragment){

        val call = RetrofitInit().groupService().removeUserFromGroup(token.token,groupId,userId)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    val result = response.body()
                    if(result?.code=="200"){
                        Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
                        loadGroupMembers(groupId)

                    }else{
                        Toast.makeText(fragment.requireContext(),"${result?.description}", Toast.LENGTH_LONG).show()

                    }



                }
            }
        )
    }

    fun deletePost(token:Token,groupId : Int,postId : Int,fragment: Fragment,desc : String,userId: Int?=null){

        val call = RetrofitInit().postService().deletePost(token.token,groupId,postId)
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    val result = response.body()
                    if(result?.code=="200"){
                        Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
                        if(desc == "group"){
                            loadPosts(groupId)
                        } else{
                            if (userId != null) {
                                loadUserPosts(userId)
                            }
                        }


                    }else{
                        Toast.makeText(fragment.requireContext(),"${result?.description}", Toast.LENGTH_LONG).show()

                    }



                }
            }
        )
    }


    fun loadUserPosts(userId : Int){

        val call = RetrofitInit().postService().getUserPosts(userId)
        call.enqueue(
            object : Callback<List<Post>> {
                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {

                    userPosts.value = response.body()


                }
            }
        )
    }
    fun createPost(groupId: Int, text: String? = null, file: File? =null){

        val call = file.let {

            if(it != null){
                RetrofitInit().postService().createPost(

                    image = file?.asRequestBody()
                        ?.let { MultipartBody.Part.createFormData("image", file.name, it) },user.value?.token?.token, groupId,text
                )
            }else{
                RetrofitInit().postService().createPostNoImg(user.value?.token?.token, groupId,text)
            }
        }
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {

                    selectedGroup.value?.groupId?.let { loadPosts(it) }


                }
            }
        )
    }

    fun createRun(groupId: Int, file: File? =null,distance:Double,hour:Int,minute:Int,second:Int,fragment: Fragment){

        val call = file.let {

            if(it != null){
                RetrofitInit().runService().createRun(

                    image = file?.asRequestBody()
                        ?.let { MultipartBody.Part.createFormData("image", file.name, it) },user.value?.token?.token, groupId,distance,hour,minute,second
                )
            }else{
                RetrofitInit().runService().createRunNoImg(user.value?.token?.token, groupId,distance,hour,minute,second)
            }
        }
        call.enqueue(
            object : Callback<APIResult> {
                override fun onFailure(call: Call<APIResult>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<APIResult>, response: Response<APIResult>) {
                    val result = response.body()
                    if(result?.code=="200"){
                        Toast.makeText(fragment.requireContext(),"${result.description}!", Toast.LENGTH_LONG).show()
                        selectedGroup.value?.groupId?.let { loadRuns(it) }


                    }else{
                        Toast.makeText(fragment.requireContext(),"${result?.description}", Toast.LENGTH_LONG).show()

                    }




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

    fun replaceDashboardFragment(frag: Fragment,fragment: Fragment,layout : FrameLayout ){
        val fragmentManager = frag.parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(layout.id,fragment)

        fragmentTransaction.commit()
    }

    fun removeFragment(fragment: Fragment){
        fragment.parentFragmentManager.popBackStack();
    }

     fun loadPic(path: String,imageView: ImageView,cache: Boolean,fragment: Fragment,drawable: Int){
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(com.ipt.runinpark.R.drawable.loading_spinning)
            .error(drawable)
            .circleCrop()
        Glide.with(fragment.requireContext()).load(path).diskCacheStrategy(
            DiskCacheStrategy.NONE).skipMemoryCache(cache).apply(options).timeout(10000).into(imageView)

    }
    fun uploadPhoto(file : File,view: ImageView,type : String = "user",groupId: Int,fragment: Fragment){

        val call =type.let {
            if(it == "user"){
                RetrofitInit().photoService().uploadPhoto(

                    image = MultipartBody.Part.createFormData("image",file.name,file.asRequestBody()),user.value?.token?.token
                )
            }else{
                RetrofitInit().photoService().uploadGroupPhoto(

                    image = MultipartBody.Part.createFormData("image",file.name,file.asRequestBody()),user.value?.token?.token,groupId
                )
            }
        }
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
                            fragment.requireContext(),
                            "Error uploading picture!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        response.body().let {photo ->
                            type.let {
                                if(it == "user"){
                                    user.value?.profilePhoto = photo?.path
                                }else{
                                    selectedGroup.value?.groupPhoto = photo?.path
                                }
                            }


                            val options: RequestOptions = RequestOptions()
                                .centerCrop()
                                .placeholder(com.ipt.runinpark.R.drawable.loading_spinning)
                                .error(com.ipt.runinpark.R.mipmap.ic_launcher_round)
                            Glide.with(DashBoardFragment().requireContext()).load(photo?.path).diskCacheStrategy(
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


