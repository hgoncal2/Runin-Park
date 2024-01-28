package com.example.myapplication.ui

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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


class DashBoardFragment : Fragment() {

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var dashBoardBinding: FragmentDashboardBinding
    //Callback para activity de escolher imagem da galeria
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        //se for escolhida uma imagem
        if (uri != null) {
            changeImage(uri)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashBoardBinding= FragmentDashboardBinding.inflate(inflater,container,false)
        //Dar load da foto de perfil por defeito
        getProfilePicture()
        dashBoardBinding.profilePicture.setOnClickListener{
            //Popup de carregar nova fotografia
            showPopup(it)
        }

        //Observa mudanças no objeto User
        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let {
                dashBoardBinding.dashboardUsername.text = it.username
                viewModel.replaceDashboardFragment(this@DashBoardFragment,UserInfoFragment(),dashBoardBinding.dashboardPlaceholder)
                it.profilePhoto?.let { pic -> viewModel.loadPic(pic,dashBoardBinding.profilePicture,false,this,R.drawable.user_logged_in) }

            }



        })

        //Saber qual tab item da tab foi escolhido
        dashBoardBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.contentDescription){
                    "dashboard_user_info" -> viewModel.replaceDashboardFragment(this@DashBoardFragment,UserInfoFragment(),dashBoardBinding.dashboardPlaceholder)
                    "dashboard_groups" -> viewModel.replaceDashboardFragment(this@DashBoardFragment,DashBoardGroups(),dashBoardBinding.dashboardPlaceholder)
                    "dashboard_posts" -> viewModel.replaceDashboardFragment(this@DashBoardFragment,MyPostsFragment(),dashBoardBinding.dashboardPlaceholder)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }


        })
        return dashBoardBinding.root
        // Inflate the layout for this fragment

    }

    override fun onResume() {
        //Dá foco no item da dashboard na navigation bar
        activity?.findViewById<BottomNavigationView>(com.example.myapplication.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)
        super.onResume()
    }
    //Converte a imagem selecionada pelo uilizador num objeto do tipo File,e de seguida feito upload para API
    private fun changeImage(uri: Uri){
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
        uploadPhoto(file)
    }
//Mostra o popup que permite ao utilizador "carregar foto"
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
    /*
    private fun loadProfilePic(path: String,imageView: ImageView,cache: Boolean){
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(com.example.myapplication.R.drawable.loading_spinning)
            .error(R.drawable.user_logged_in)
            .circleCrop()
        Glide.with(this@DashBoardFragment.requireContext()).load(path).diskCacheStrategy(
            DiskCacheStrategy.NONE).skipMemoryCache(cache).apply(options).timeout(10000).into(imageView)

    }
    */

    //Função que faz upload da foto de perfil para a API
    private fun uploadPhoto(file : File){

        val call = RetrofitInit().photoService().uploadPhoto(

            image = MultipartBody.Part.createFormData("image",file.name,file.asRequestBody()),viewModel.user.value?.token?.token
        )
        call.enqueue(
            object : Callback<Photo> {
                override fun onFailure(call: Call<Photo>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@DashBoardFragment.context,"Erro ao carregar imagem!", Toast.LENGTH_LONG).show()

                }
                override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                    //Valida se o utilizador tem permissões para mudar foto de perfil
                    if(response.code() == 403){
                        Toast.makeText(this@DashBoardFragment.context,"Erro ao carregar imagem!Não autorizado!", Toast.LENGTH_LONG).show()
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
    //Insere a foto de perfil por defeito na view correspondente
    private fun getProfilePicture(){
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(com.example.myapplication.R.drawable.loading_spinning)
            .error(com.example.myapplication.R.mipmap.ic_launcher_round)
            .circleCrop()
        Glide.with(this@DashBoardFragment.requireContext()).load(R.drawable.default_groups).apply(options).timeout(6000).into(dashBoardBinding.profilePicture)
    }
}