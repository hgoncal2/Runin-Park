package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGroupPageBinding
import com.example.myapplication.viewModel.UserViewModel

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
        viewModel.selectedGroup.observe(viewLifecycleOwner,{
            groupPageBinding.groupName.text = it.name
        })
        groupPageBinding = FragmentGroupPageBinding.inflate(layoutInflater,container,false)



        // Inflate the layout for this fragment
        return groupPageBinding.root
    }


}