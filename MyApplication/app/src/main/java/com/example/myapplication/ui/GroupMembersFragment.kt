package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentGroupMembersBinding
import com.example.myapplication.model.User
import com.example.myapplication.ui.adapter.MembersListAdapter
import com.example.myapplication.viewModel.UserViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupMembersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupMembersFragment : Fragment() {
    private lateinit var groupMembersFragmentBinding: FragmentGroupMembersBinding
    private var groupMembers = mutableListOf<User>()
    private val viewModel: UserViewModel by activityViewModels()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        groupMembersFragmentBinding = FragmentGroupMembersBinding.inflate(inflater,container,false)
        val adapter = MembersListAdapter(groupMembers,this@GroupMembersFragment.requireContext(),viewModel.selectedGroup.value?.ownerId,viewModel.user.value?.userId){
            user,desc ->
                if(desc == "profile"){
                    viewModel.selectedUser.value=user.also { viewModel.replaceFragment(this,UserProfileFragment()) }
                }

        }
        groupMembersFragmentBinding.groupMembersView.adapter=adapter
        groupMembersFragmentBinding.groupMembersView.layoutManager= LinearLayoutManager(this@GroupMembersFragment.requireContext())
        groupMembersFragmentBinding.groupMembersView.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
        viewModel.groupMembers.observe(viewLifecycleOwner, Observer {
            viewModel.groupMembers.value?.let {
                groupMembers.clear()
                groupMembers.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })

        return groupMembersFragmentBinding.root
    }


}