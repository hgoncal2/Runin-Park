package com.ipt.runinpark.ui

import android.app.AlertDialog
import android.graphics.Color
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
import com.ipt.runinpark.R
import com.ipt.runinpark.databinding.FragmentGroupPageBinding
import com.ipt.runinpark.model.Photo
import com.ipt.runinpark.retrofit.RetrofitInit
import com.ipt.runinpark.viewModel.UserViewModel
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



class GroupPageFragment : Fragment() {
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var groupPageBinding: FragmentGroupPageBinding
    //Callback para activity de escolher imagem da galeria
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {

            changeImage(uri)
        } else {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        groupPageBinding = FragmentGroupPageBinding.inflate(layoutInflater,container,false)
        //Mostra o popup que permite ao utilizador "carregar foto"
        //Este popup só é mostrado ao owner do grupo
        groupPageBinding.groupPhoto.setOnClickListener{
            viewModel.user.value?.userId.let { id -> if(id == viewModel.selectedGroup.value?.ownerId) showPopup(it) }
        }
        //Dá reset ao posts de um grupo.
        viewModel.groupPosts.value=null

        //Preenche campos que identificam um grupo nas respetivas views
        viewModel.selectedGroup.observe(viewLifecycleOwner,{
            viewModel.selectedGroup.value?.let {
                groupPageBinding.groupName.text = it.name
                groupPageBinding.groupCity.text = it.city
                //Se não tiver foto de perfil,carrega a por defeito
                viewModel.selectedGroup.value?.groupPhoto?.let {
                    viewModel.loadPic(it,groupPageBinding.groupPhoto,true,this, R.drawable.default_groups)

                } ?: viewModel.loadPic("",groupPageBinding.groupPhoto,true,this, R.drawable.default_groups)


            }
            })
        groupPageBinding.groupPageTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.contentDescription){
                    "group_posts" -> viewModel.replaceDashboardFragment(this@GroupPageFragment,PostFragment(),groupPageBinding.groupPagePlaceholder).also { viewModel.selectedGroup.value?.groupId?.let { it1 ->
                        viewModel.loadPosts(it1)
                    } }
                    "group_members" -> viewModel.replaceDashboardFragment(this@GroupPageFragment,GroupMembersFragment(),groupPageBinding.groupPagePlaceholder).also { viewModel.selectedGroup.value?.groupId?.let { it1 ->
                        viewModel.loadGroupMembers(it1)
                    } }
                    "group_runs" -> viewModel.replaceDashboardFragment(this@GroupPageFragment,GroupRunsFragment(),groupPageBinding.groupPagePlaceholder).also { viewModel.selectedGroup.value?.groupId?.let { it1 ->
                        viewModel.loadRuns(it1)
                    } }
                }
            }


            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                print(tab?.id)
                print("")
            }


        })



//Permite ao utilizador juntar se a um grupo
        groupPageBinding.btnJoinGroup.setOnClickListener{
            viewModel.selectedGroup.value?.groupId?.let { it1 -> viewModel.joinGroup(it1,this) }
        }



//Permite ao utilizador sair de um grupo,com mensagem de confirmação
        groupPageBinding.btnLeaveGroup.setOnClickListener{
            val builder = AlertDialog.Builder(this.requireContext())
            builder.setMessage("Tem a certeza  que deseja sair do grupo?")
                .setCancelable(true)
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.selectedGroup.value?.groupId?.let { it1 -> viewModel.leaveGroup(it1,this) }
                }
                .setNegativeButton("No") { dialog , _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }


        viewModel.loggedIn.observe(viewLifecycleOwner) {
            if (!it) {
                //Se o utilizador não tiver autenticado,restringe visibiliade do grupo
                groupPageBinding.notLoggedIn.visibility = View.VISIBLE
            } else {
                //Mostra página de posts inicialmente,e carrega oists para grupo escolhido
                viewModel.replaceDashboardFragment(this@GroupPageFragment,PostFragment(),groupPageBinding.groupPagePlaceholder).also { viewModel.selectedGroup.value?.let { it1 ->
                    viewModel.loadPosts(it1.groupId)
                } }

                groupPageBinding.notLoggedIn.visibility = View.GONE
                viewModel.userGroups.observe(viewLifecycleOwner, Observer {
                    viewModel.selectedGroup.value?.let {_ ->
                        //Se utilizador não pertence ao grupo,mostra botão de juntar
                        if( it.contains(viewModel.selectedGroup.value) == false){
                            groupPageBinding.btnLeaveGroup.visibility = View.GONE
                            groupPageBinding.btnJoinGroup.visibility = View.VISIBLE
                            groupPageBinding.btnJoinGroup.setBackgroundColor(Color.parseColor("#34eb40"))
                        //Se utilizador já  pertence ao grupo,mostra botão de sair
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
        return groupPageBinding.root
    }
    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.menu?.getItem(1)?.setChecked(true)
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
        val popup = PopupMenu(this@GroupPageFragment.requireContext(), view)
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
    //Dá upload da foto de grupo
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
                    Toast.makeText(this@GroupPageFragment.context,"Erro ao carregar imagem!", Toast.LENGTH_LONG).show()
                }
                override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                    if(response.code() == 403){
                        Toast.makeText(this@GroupPageFragment.context,"Erro ao carregar imagem!Não autorizado!", Toast.LENGTH_LONG).show()
                    }else{
                        response.body().let {
                            viewModel.selectedGroup.value?.groupPhoto=it?.path
                            val options: RequestOptions = RequestOptions()
                                .centerCrop()
                                .placeholder(com.ipt.runinpark.R.drawable.loading_spinning)
                                .error(com.ipt.runinpark.R.drawable.default_groups)
                                .circleCrop()
                            Glide.with(this@GroupPageFragment.requireContext()).load(it?.path).diskCacheStrategy(
                                DiskCacheStrategy.ALL).skipMemoryCache(false).apply(options).timeout(6000).into(groupPageBinding.groupPhoto)

                        }

                    }

                }
            }
        )
    }
}


