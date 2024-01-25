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
import com.example.myapplication.databinding.FragmentDashBoardGroupsBinding
import com.example.myapplication.model.Group
import com.example.myapplication.ui.adapter.AddGroupAdapter
import com.example.myapplication.viewModel.UserViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [DashBoardGroups.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashBoardGroups : Fragment() {
    private val viewModel: UserViewModel by activityViewModels()

    private lateinit var dashBoardGroupsBinding: FragmentDashBoardGroupsBinding
    private var groupList = mutableListOf<Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dashBoardGroupsBinding= FragmentDashBoardGroupsBinding.inflate(inflater,container,false)
        viewModel.user.value?.let { viewModel.loadUserGroups(it.userId) }

        val recycler =dashBoardGroupsBinding.recyclerDashboardGroups
        recycler.layoutManager = LinearLayoutManager(this.requireContext())
        recycler.addItemDecoration(DividerItemDecoration(this.requireContext(), LinearLayoutManager.VERTICAL))

        val adapter = AddGroupAdapter(groupList,this.requireContext()){

            viewModel.selectedGroup.value = it
            viewModel.replaceFragment(this, GroupPageFragment())
        }

        recycler.adapter = adapter
        viewModel.userGroups.observe(viewLifecycleOwner, Observer {
            if(!viewModel.userGroups.value?.filter { it.ownerId == viewModel.user.value?.userId }.isNullOrEmpty() ){
                dashBoardGroupsBinding.noGroupsCreated.visibility = View.GONE
                groupList.clear()
                groupList.addAll(viewModel.userGroups.value?.filter { it.ownerId == viewModel.user.value?.userId } as MutableList<Group>)
                adapter?.notifyDataSetChanged()

            }else{

                    dashBoardGroupsBinding.noGroupsCreated.visibility = View.VISIBLE

            }


        })
        return dashBoardGroupsBinding.root
    }


}