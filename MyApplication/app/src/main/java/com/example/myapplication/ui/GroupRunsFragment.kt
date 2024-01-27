package com.example.myapplication.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGroupRunsBinding
import com.example.myapplication.dialogs.CreateRunDialog
import com.example.myapplication.viewModel.UserViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class GroupRunsFragment : Fragment() {

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var groupRunsBinding: FragmentGroupRunsBinding
    private lateinit var  addRunDialog : CreateRunDialog
    private  var  imgUri : Uri? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            //changeImage(uri)
           addImage(uri,addRunDialog)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         addRunDialog = object : CreateRunDialog(this.requireContext(),viewModel){}
        groupRunsBinding = FragmentGroupRunsBinding.inflate(inflater,container,false)
        groupRunsBinding.addRunBtn.setOnClickListener{


            if(!addRunDialog.isShowing){
                addRunDialog.show()
                val window: Window? = addRunDialog.window
                window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val  btn_sel =addRunDialog.findViewById<ImageButton>(R.id.btn_sel_foto_run)
                btn_sel?.setOnClickListener{
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }

            }



        }
        // Inflate the layout for this fragment
        return groupRunsBinding.root
    }
    private fun addImage(uri: Uri,addRunDialog: CreateRunDialog){
        val img = addRunDialog.findViewById<ImageView>(R.id.run_img_dlg)
        img.setImageURI(uri)
        img.visibility=View.VISIBLE
        imgUri = uri
    }


}