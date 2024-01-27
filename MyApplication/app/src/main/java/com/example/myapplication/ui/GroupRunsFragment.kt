package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentGroupRunsBinding
import com.example.myapplication.dialogs.CreateRunDialog
import com.example.myapplication.viewModel.UserViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class GroupRunsFragment : Fragment() {
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var groupRunsBinding: FragmentGroupRunsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        groupRunsBinding = FragmentGroupRunsBinding.inflate(inflater,container,false)
        groupRunsBinding.addRunBtn.setOnClickListener{
            val addRunDialog = object : CreateRunDialog(this.requireContext(),viewModel,this@GroupRunsFragment){

            }
            if(!addRunDialog.isShowing){
                addRunDialog.show()
                val window: Window? = addRunDialog.window
                window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

            }

        }
        // Inflate the layout for this fragment
        return groupRunsBinding.root
    }


}