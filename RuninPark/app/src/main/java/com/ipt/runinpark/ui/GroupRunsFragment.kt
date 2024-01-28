package com.ipt.runinpark.ui

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import com.ipt.runinpark.R
import com.ipt.runinpark.databinding.FragmentGroupRunsBinding
import com.ipt.runinpark.dialogs.CreateRunDialog
import com.ipt.runinpark.model.Run
import com.ipt.runinpark.ui.adapter.RunListAdapter
import com.ipt.runinpark.viewModel.UserViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class GroupRunsFragment : Fragment() {

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var groupRunsBinding: FragmentGroupRunsBinding
    private lateinit var  addRunDialog : CreateRunDialog
    private var runsList = mutableListOf<Run>()
    private  var  imgUri : Uri? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {

            changeImage(uri)
           addImage(uri,addRunDialog)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Cria instância do dialog para criar corrida
         addRunDialog = object : CreateRunDialog(this.requireContext(),viewModel){}
        groupRunsBinding = FragmentGroupRunsBinding.inflate(inflater,container,false)
        val adapter = RunListAdapter(runsList,this@GroupRunsFragment.requireContext(),viewModel.user.value?.userId){
       //click listener para uma corrida,poderá ser útil no futuro
        }
        groupRunsBinding.groupRunsView.adapter=adapter
        groupRunsBinding.groupRunsView.layoutManager= LinearLayoutManager(this@GroupRunsFragment.requireContext())
        groupRunsBinding.groupRunsView.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
        viewModel.groupRuns.observe(viewLifecycleOwner, Observer {
            viewModel.groupRuns.value?.let {
                runsList.clear()
                runsList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })

        viewModel.userGroups.observe(viewLifecycleOwner, Observer {
            viewModel.selectedGroup.value?.let {_ ->
                //se utilizador não pertence ao grupo:
                if( it.contains(viewModel.selectedGroup.value) == false){
                    //esconde botão de adicionar corrida
                    groupRunsBinding.addRunBtn.visibility = View.GONE
                }else{
                    groupRunsBinding.addRunBtn.visibility = View.VISIBLE

                }
            }
        })

        groupRunsBinding.addRunBtn.setOnClickListener{
            if(!addRunDialog.isShowing) {
                //mostra dialog de criar corrida
                addRunDialog.show()

                val window: Window? = addRunDialog.window
                window?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                /*
              Vamos falar um bocado sobre a escolhe de estar a ir buscar os elementos do dialog aqui,e não na função que define o próprio dialog
              Isto acontece simplesmente porque não arranjei uma boa forma de chamar a Activity de escolher a fotografia dentro da classe
              abstracta do dialog.
                 */
                val hours = addRunDialog.findViewById<AppCompatEditText>(R.id.run_hours_dlg)
                val minutes = addRunDialog.findViewById<AppCompatEditText>(R.id.run_mins_dlg)
                val img = addRunDialog.findViewById<ImageView>(R.id.run_img_dlg)
                val seconds = addRunDialog.findViewById<AppCompatEditText>(R.id.run_secs_dlg)
                val distance = addRunDialog.findViewById<AppCompatEditText>(R.id.run_dist_dlg)
                val btnSel = addRunDialog.findViewById<ImageButton>(R.id.btn_sel_foto_run)
                //Escolher foto da galeria
                btnSel?.setOnClickListener {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                //Verifica se os campos obrigatórios(horas,mins,segs e distancia) estão preenchidos
                //E de seguida dá reset aos campos.Por alguma razão estes não estavam a ser "zerados"
                //quando o dialog é destruído,daí isso estar a ser feito aqui.
                addRunDialog.findViewById<Button>(R.id.create_run_btn).setOnClickListener {
                    if (distance.text?.isNotEmpty() == true && distance.text?.isNotBlank() == true && hours.text?.isNotEmpty() == true && minutes.text?.isNotEmpty() == true && seconds.text?.isNotEmpty() == true) {
                        viewModel.createRun(
                            viewModel.selectedGroup.value!!.groupId,
                            changeImage(imgUri),
                            distance.text.toString().toDouble(),
                            hours.text.toString().toInt(),
                            minutes.text.toString().toInt(),
                            seconds.text.toString().toInt(),
                            this
                        )
                        hours.setText("")
                        minutes.setText("")
                        seconds.setText("")
                        distance.setText("")
                        img.setImageResource(0)
                        addRunDialog.cancel()

                    } else {
                        Toast.makeText(
                            this@GroupRunsFragment.requireContext(),
                            "Por favor insira uma corrida válida!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        return groupRunsBinding.root
    }
    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.menu?.getItem(1)?.setChecked(true)
        super.onResume()
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

    private fun addImage(uri: Uri,addRunDialog: CreateRunDialog){
        val img = addRunDialog.findViewById<ImageView>(R.id.run_img_dlg)
        img.setImageURI(uri)
        img.visibility=View.VISIBLE
        imgUri = uri
    }


}