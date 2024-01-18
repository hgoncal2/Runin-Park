package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGroupsBinding
import com.example.myapplication.model.Group
import com.example.myapplication.ui.adapter.GroupListAdapter
import com.example.myapplication.viewModel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupsFragment : Fragment() {
   private lateinit var groupsBinding: FragmentGroupsBinding
   private  var groups : List<Group>? = null
    private val viewModel: UserViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        groupsBinding= FragmentGroupsBinding.inflate(inflater,container,false)

        val adapter = GroupListAdapter(viewModel.tgroups,this@GroupsFragment.requireContext())
        groupsBinding.groupsViewList.adapter=adapter
        groupsBinding.groupsViewList.layoutManager= LinearLayoutManager(this@GroupsFragment.requireContext())
        groupsBinding.groupsViewList.addItemDecoration(DividerItemDecoration(this@GroupsFragment.requireContext(), LinearLayoutManager.VERTICAL))

        if(viewModel.loggedIn.value == false){
            viewModel.loadGroups()

            //adicionar observer
        }else{
           // viewModel.user.value?.let { loadUserGroups(it.userId) }
        }



        viewModel.groups.observe(viewLifecycleOwner, Observer {

            if(viewModel.loggedIn.value == true){
                if(viewModel.groups.value?.size == 0){
                    if(groupsBinding.noGroups.visibility == View.GONE){
                        groupsBinding.noGroups.visibility= View.VISIBLE

                    }

                }else{
                    if(groupsBinding.noGroups.visibility == View.VISIBLE) groupsBinding.noGroups.visibility= View.GONE
                    GroupListAdapter(it,this@GroupsFragment.requireContext()).notifyDataSetChanged()

                }

            }else{
                if(viewModel.groups.value?.size != 0){
                    adapter.notifyDataSetChanged()
                }
            }

        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let {
                groupsBinding.user.setText(it.username)
                groupsBinding.user.visibility= View.VISIBLE
                groupsBinding.groupsViewList.layoutParams.height = LayoutParams.WRAP_CONTENT
                viewModel.loadUserGroups(it.userId)
            } ?: run {
                groupsBinding.user.visibility= View.GONE
            }



        })



        return groupsBinding.root
    }

    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.menu?.getItem(1)?.setChecked(true)

        super.onResume()
    }
    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden){
            activity?.findViewById<BottomNavigationView>(com.example.myapplication.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)


        }

    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }





}