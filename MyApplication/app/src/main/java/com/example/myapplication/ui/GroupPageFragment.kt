package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
        groupPageBinding = FragmentGroupPageBinding.inflate(layoutInflater,container,false)

        viewModel.loadPic("",groupPageBinding.groupPhoto,true,this,R.drawable.default_groups)
        viewModel.selectedGroup.observe(viewLifecycleOwner,{
            groupPageBinding.groupName.text = it.name
            groupPageBinding.groupCity.text = it.city


        })
        viewModel.loggedIn.observe(viewLifecycleOwner) {
            if (!it) {
                groupPageBinding.notLoggedIn.visibility = View.VISIBLE
            } else {
                if(viewModel.user.value?.userId != viewModel.selectedGroup.value?.ownerId){
                    (groupPageBinding.groupPageTab.getTabAt(2)?.view as LinearLayout).visibility = View.GONE
                }

                groupPageBinding.notLoggedIn.visibility = View.GONE
                if(viewModel.userGroups.value?.contains(viewModel.selectedGroup.value!!) == false){
                    groupPageBinding.btnJoinGroup.visibility = View.VISIBLE
                }

            }
        }



        // Inflate the layout for this fragment
        return groupPageBinding.root
    }



}