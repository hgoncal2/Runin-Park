package com.example.myapplication.ui

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.model.Photo
import com.example.myapplication.retrofit.RetrofitInit
import com.example.myapplication.viewModel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
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
 * Use the [DashBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashBoardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var dashBoardBinding: FragmentDashboardBinding
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            changeImage(uri)
        } else {
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashBoardBinding= FragmentDashboardBinding.inflate(inflater,container,false)
        getProfilePicture()
        dashBoardBinding.profilePicture.setOnClickListener{
            showPopup(it)
        }
        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let {

                dashBoardBinding.dashboardUsername.text = it.username
                viewModel.replaceDashboardFragment(this@DashBoardFragment,UserInfoFragment(),dashBoardBinding.dashboardPlaceholder)
                it.profilePhoto?.let { pic -> viewModel.loadPic(pic,dashBoardBinding.profilePicture,false,this,R.drawable.user_logged_in) }


            }



        })


        dashBoardBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.contentDescription){
                    "dashboard_user_info" -> viewModel.replaceDashboardFragment(this@DashBoardFragment,UserInfoFragment(),dashBoardBinding.dashboardPlaceholder)
                    "dashboard_groups" -> viewModel.replaceDashboardFragment(this@DashBoardFragment,DashBoardGroups(),dashBoardBinding.dashboardPlaceholder)
                    "dashboard_posts" -> viewModel.replaceDashboardFragment(this@DashBoardFragment,MyPostsFragment(),dashBoardBinding.dashboardPlaceholder)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                print(tab?.id)
                print("")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                print(tab?.id)
                print("")
            }


        })
        return dashBoardBinding.root
        // Inflate the layout for this fragment

    }

    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(com.example.myapplication.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)

        super.onResume()
    }
    private fun changeImage(uri: Uri){
        val imgUri = uri
        val path = imgUri?.path
        val path2 = Environment.getExternalStorageDirectory().path
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

    private fun showPopup(view: View) {
        val popup = PopupMenu(this@DashBoardFragment.requireContext(), view)
        popup.inflate(R.menu.profile_popup_menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.view_profile -> {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))


                }

            }

            true
        })



        popup.show()
    }
    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden){
            activity?.findViewById<BottomNavigationView>(com.example.myapplication.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)


        }

    }


    private fun loadProfilePic(path: String,imageView: ImageView,cache: Boolean){
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(com.example.myapplication.R.drawable.loading_spinning)
            .error(R.drawable.user_logged_in)
            .circleCrop()
        Glide.with(this@DashBoardFragment.requireContext()).load(path).diskCacheStrategy(
            DiskCacheStrategy.NONE).skipMemoryCache(cache).apply(options).timeout(10000).into(imageView)

    }
    private fun uploadPhoto(file : File){

        val call = RetrofitInit().photoService().uploadPhoto(

            image = MultipartBody.Part.createFormData("image",file.name,file.asRequestBody()),viewModel.user.value?.token?.token
        )
        call.enqueue(
            object : Callback<Photo> {
                override fun onFailure(call: Call<Photo>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@DashBoardFragment.context,"Error Uploading Image!", Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                    if(response.code() == 403){
                        Toast.makeText(this@DashBoardFragment.context,"Wrong Username or Password!", Toast.LENGTH_LONG).show()
                    }else{
                        response.body().let {
                            viewModel.user.value?.profilePhoto=it?.path
                            val options: RequestOptions = RequestOptions()
                                .centerCrop()
                                .placeholder(com.example.myapplication.R.drawable.loading_spinning)
                                .error(com.example.myapplication.R.mipmap.ic_launcher_round)
                                .circleCrop()
                            Glide.with(this@DashBoardFragment.requireContext()).load(it?.path).diskCacheStrategy(
                                DiskCacheStrategy.NONE).skipMemoryCache(true).apply(options).timeout(6000).into(dashBoardBinding.profilePicture)

                        }

                    }

                }
            }
        )
    }
    private fun getProfilePicture(){
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(com.example.myapplication.R.drawable.loading_spinning)
            .error(com.example.myapplication.R.mipmap.ic_launcher_round)
            .circleCrop()
        Glide.with(this@DashBoardFragment.requireContext()).load(R.drawable.default_groups).apply(options).timeout(6000).into(dashBoardBinding.profilePicture)
    }
}