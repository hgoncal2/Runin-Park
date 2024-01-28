package com.ipt.runinpark.ui

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import com.ipt.runinpark.R
import com.ipt.runinpark.databinding.FragmentPostBinding
import com.ipt.runinpark.model.Photo
import com.ipt.runinpark.model.Post
import com.ipt.runinpark.retrofit.RetrofitInit
import com.ipt.runinpark.ui.adapter.PostListAdapter
import com.ipt.runinpark.viewModel.UserViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class PostFragment : Fragment() {
   private lateinit var postFragmentBinding : FragmentPostBinding
    private var postsList = mutableListOf<Post>()
    private val viewModel: UserViewModel by activityViewModels()
    private  var  imgUri : Uri? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            //changeImage(uri)
            addImage(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }



    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        postFragmentBinding = FragmentPostBinding.inflate(inflater,container,false)


        val adapter = PostListAdapter(postsList,this@PostFragment.requireContext(),viewModel.user.value?.userId,viewModel.selectedGroup.value?.ownerId){
          post -> viewModel.deletePost(
            viewModel.user.value!!.token!!,post.groupId,post.postId,this,"group"
          )
        }
        postFragmentBinding.postsViewList.adapter=adapter
        postFragmentBinding.postsViewList.layoutManager= LinearLayoutManager(this@PostFragment.requireContext())
        postFragmentBinding.postsViewList.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
        viewModel.groupPosts.observe(viewLifecycleOwner, Observer {
            viewModel.groupPosts.value?.let {
                postsList.clear()
                postsList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })
        //selecionar uma foto para o post ao clicar
        postFragmentBinding.btnSelFoto.setOnClickListener{

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        }
        //publicar o post
        postFragmentBinding.btnSubmitPost.setOnClickListener{
            if((postFragmentBinding.postTextFrag.text.isNotEmpty() && postFragmentBinding.postTextFrag.text.isNotBlank()) || imgUri!=null){
                viewModel.createPost(viewModel.selectedGroup.value?.groupId!!,postFragmentBinding.postTextFrag.text.toString(),changeImage(imgUri))
                postFragmentBinding.postTextFrag.setText("")
                postFragmentBinding.postImage.setImageResource(0)
                postFragmentBinding.postImage.visibility=View.GONE
                imgUri=null
            }else{
                Toast.makeText(this@PostFragment.requireContext(),"Por favor insira um texto válido!",Toast.LENGTH_SHORT).show()
            }


        }
        viewModel.userGroups.observe(viewLifecycleOwner, Observer {
            viewModel.selectedGroup.value?.let {_ ->
                if( it.contains(viewModel.selectedGroup.value) == false){
                    postFragmentBinding.groupPost.visibility = View.GONE
                }else{
                    postFragmentBinding.groupPost.visibility = View.VISIBLE

                }
            }



        })


        return postFragmentBinding.root
    }

    //adiciona uma foto ao post
    private fun addImage(uri: Uri){
        postFragmentBinding.postImage.setImageURI(uri)
        postFragmentBinding.postImage.visibility=View.VISIBLE
        imgUri = uri
    }
    private fun changeImage(uri: Uri?) : File?{


if(uri == null){
    return null
}
        val imgUri = uri
        val path = imgUri.path

        val path2 = Environment.getExternalStorageDirectory().path
        val parcelFileDescriptor = imgUri?.let { it1 ->
            this.requireContext().contentResolver.openFileDescriptor(
                it1, "r", null)
        }
        val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
        val file = File(this.requireContext().cacheDir, "dwadw")
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)

        return file


}
    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.menu?.getItem(1)?.setChecked(true)
        super.onResume()
    }
    //dá upload na foto
    private fun uploadPhoto(file : File){

        val call = viewModel.selectedGroup.value?.groupId?.let {
            RetrofitInit().photoService().uploadGroupPhoto(

                image = MultipartBody.Part.createFormData("image",file.name,file.asRequestBody()),viewModel.user.value?.token?.token,
                it
            )
        }
        call?.enqueue(
            object : Callback<Photo> {
                override fun onFailure(call: Call<Photo>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@PostFragment.context,"Erro ao carregar imagem!", Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                    if(response.code() == 403){
                        Toast.makeText(this@PostFragment.context,"Não tem permissão!", Toast.LENGTH_LONG).show()
                    }else{
                        response.body().let {
                            viewModel.selectedGroup.value?.groupPhoto=it?.path

                        }

                    }

                }
            }
        )
    }

}

