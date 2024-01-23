package com.example.myapplication.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGroupPageBinding
import com.example.myapplication.model.Photo
import com.example.myapplication.retrofit.RetrofitInit
import com.example.myapplication.viewModel.UserViewModel
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * A simple [Fragment] subclass.
 * Use the [GroupPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupPageFragment : Fragment() {
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var groupPageBinding: FragmentGroupPageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        groupPageBinding = FragmentGroupPageBinding.inflate(layoutInflater,container,false)
        groupPageBinding.groupPhoto.setOnClickListener{
            showPopup(it)
        }
        viewModel.selectedGroup.observe(viewLifecycleOwner,{
            viewModel.selectedGroup.value?.let {
                groupPageBinding.groupName.text = it.name
                groupPageBinding.groupCity.text = it.city
                viewModel.selectedGroup.value?.groupPhoto?.let {
                    viewModel.loadPic(it,groupPageBinding.groupPhoto,true,this, R.drawable.default_groups)

                } ?: viewModel.loadPic("",groupPageBinding.groupPhoto,true,this, R.drawable.default_groups)


            }
            })


        groupPageBinding.btnJoinGroup.setOnClickListener{
            viewModel.selectedGroup.value?.groupId?.let { it1 -> viewModel.joinGroup(it1,this) }
        }
        groupPageBinding.btnLeaveGroup.setOnClickListener{
            viewModel.selectedGroup.value?.groupId?.let { it1 -> viewModel.leaveGroup(it1,this) }
        }
        viewModel.loggedIn.observe(viewLifecycleOwner) {
            if (!it) {
                groupPageBinding.notLoggedIn.visibility = View.VISIBLE
            } else {
                if(viewModel.user.value?.userId != viewModel.selectedGroup.value?.ownerId){
                    (groupPageBinding.groupPageTab.getTabAt(2)?.view as LinearLayout).visibility = View.GONE
                }

                groupPageBinding.notLoggedIn.visibility = View.GONE
                viewModel.userGroups.observe(viewLifecycleOwner, Observer {
                    viewModel.selectedGroup.value?.let {_ ->
                        if( it.contains(viewModel.selectedGroup.value) == false){
                            groupPageBinding.btnLeaveGroup.visibility = View.GONE
                            groupPageBinding.btnJoinGroup.visibility = View.VISIBLE
                            groupPageBinding.btnJoinGroup.setBackgroundColor(Color.parseColor("#34eb40"))
                        }else{
                            groupPageBinding.btnLeaveGroup.visibility = View.VISIBLE
                            groupPageBinding.btnLeaveGroup.setBackgroundColor(Color.parseColor("#e81010"))
                            groupPageBinding.btnJoinGroup.visibility = View.GONE

                        }
                }



                })
                if(viewModel.userGroups.value?.contains(viewModel.selectedGroup.value!!) == false){
                    groupPageBinding.btnJoinGroup.visibility = View.VISIBLE
                    groupPageBinding.btnJoinGroup.setBackgroundColor(Color.parseColor("#34eb40"))
                }else{
                    groupPageBinding.btnLeaveGroup.visibility = View.VISIBLE
                    groupPageBinding.btnLeaveGroup.setBackgroundColor(Color.parseColor("#e81010"))

                }

            }
        }



        // Inflate the layout for this fragment
        return groupPageBinding.root
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
                viewModel.selectedGroup.value?.groupId?.let { it1 ->
                    uploadPhoto(file)

                }

            }}

    private fun showPopup(view: View) {
        val popup = PopupMenu(this@GroupPageFragment.requireContext(), view)
        popup.inflate(R.menu.profile_popup_menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.view_profile -> {
                    val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                    changeImage.launch(pickImg)
                }

            }

            true
        })



        popup.show()
    }
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
                    Toast.makeText(this@GroupPageFragment.context,"Error Uploading Image!", Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                    if(response.code() == 403){
                        Toast.makeText(this@GroupPageFragment.context,"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                    }else{
                        response.body().let {
                            viewModel.selectedGroup.value?.groupPhoto=it?.path
                            val options: RequestOptions = RequestOptions()
                                .centerCrop()
                                .placeholder(com.example.myapplication.R.drawable.loading_spinning)
                                .error(com.example.myapplication.R.mipmap.ic_launcher_round)
                                .circleCrop()
                            Glide.with(this@GroupPageFragment.requireContext()).load(it?.path).diskCacheStrategy(
                                DiskCacheStrategy.NONE).skipMemoryCache(true).apply(options).timeout(6000).into(groupPageBinding.groupPhoto)

                        }

                    }

                }
            }
        )
    }
}

