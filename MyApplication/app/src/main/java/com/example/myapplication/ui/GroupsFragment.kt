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
import com.example.myapplication.dialogs.AddGroupDialog
import com.example.myapplication.model.Group
import com.example.myapplication.ui.adapter.GroupListAdapter
import com.example.myapplication.viewModel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    var groupsFiltered = mutableListOf<Group>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        groupsBinding= FragmentGroupsBinding.inflate(inflater,container,false)

        val adapter = GroupListAdapter(viewModel.adapterGroups,this@GroupsFragment.requireContext()){
            showGroupPage(it)
        }
        groupsBinding.groupsViewList.adapter=adapter
        groupsBinding.groupsViewList.layoutManager= LinearLayoutManager(this@GroupsFragment.requireContext())
        groupsBinding.groupsViewList.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))

        viewModel.loggedIn.observe(viewLifecycleOwner, Observer {
            if(viewModel.loggedIn.value == false){


                viewModel.allGroups.value?.let {}?:viewModel.loadGroups()
                groupsBinding.addCreateBtnsLayout.visibility = View.GONE

                //adicionar observer
            }else{
                viewModel.user.value?.userId?.let {user ->
                    viewModel.userGroups.value?.let {} ?: viewModel.loadUserGroups(user)
                    print("adawd")
                }
                groupsBinding.addCreateBtnsLayout.visibility = View.VISIBLE

                // viewModel.user.value?.let { loadUserGroups(it.userId) }
            }
        })



        val groupsDialog = object : AddGroupDialog(this.requireContext(),groupsFiltered,viewModel,this){

        }



groupsBinding.btnAddGroup.setOnClickListener{

    viewModel.user.value?.userId?.let { it1 -> viewModel.loadUserGroups(it1) }
GlobalScope.launch (Dispatchers.Main){
    viewModel.groupsFiltered.collect { value ->
        if(value==1){
            groupsFiltered.clear()
            viewModel.allGroups.value?.filterNot { viewModel.userGroups.value?.contains(it) == true }
                ?.let { it1 -> groupsFiltered.addAll(it1.toMutableList()) }

            groupsDialog.notifyAdapter()
            if(!groupsDialog.isShowing){
                groupsDialog.show()
                viewModel._groupsFiltered.value=0
            }
        }
    }
}
    viewModel.loadGroups("dialog")
    /*
        viewModel.loadGroups("dialog")
        viewModel.groupsFiltered.value= emptyList()

   viewModel.groupsFiltered.observe(viewLifecycleOwner,Observer{
       it?.let {
           if(viewModel.groupsFiltered.value?.isNotEmpty() == true){

           }





       }

   })*/
}
        viewModel.allGroups.observe(viewLifecycleOwner, Observer {

            if(viewModel.loggedIn.value == true){
                if(viewModel.allGroups.value?.size == 0){
                    if(groupsBinding.noGroups.visibility == View.GONE){
                        groupsBinding.noGroups.visibility= View.VISIBLE

                    }

                }else{
                    if(groupsBinding.noGroups.visibility == View.VISIBLE) groupsBinding.noGroups.visibility= View.GONE
                    adapter.notifyDataSetChanged()
                }

            }else{
                if(viewModel.allGroups.value?.size != 0){
                    adapter.notifyDataSetChanged()
                }
            }

        })
        viewModel.userGroups.observe(viewLifecycleOwner, Observer {

            if(viewModel.loggedIn.value == true){
                if(viewModel.userGroups.value?.size == 0){
                    if(groupsBinding.noGroups.visibility == View.GONE){
                        groupsBinding.noGroups.visibility= View.VISIBLE

                    }

                }else{
                    if(groupsBinding.noGroups.visibility == View.VISIBLE) groupsBinding.noGroups.visibility= View.GONE
                    adapter.notifyDataSetChanged()
                }

            }else{
                if(viewModel.userGroups.value?.size != 0){
                    adapter.notifyDataSetChanged()
                }
            }

        })



        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let {
                groupsBinding.user.setText(it.username)
                groupsBinding.user.visibility= View.VISIBLE
                groupsBinding.groupsViewList.layoutParams.height = LayoutParams.WRAP_CONTENT
            } ?: run {
                groupsBinding.user.visibility= View.GONE
            }



        })



        return groupsBinding.root
    }

    private fun showGroupPage(group: Group){
        viewModel.selectedGroup.value = group
        viewModel.replaceFragment(this,GroupPageFragment())
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






}