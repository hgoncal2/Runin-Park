package com.ipt.runinpark.ui

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
import com.ipt.runinpark.R
import com.ipt.runinpark.databinding.FragmentGroupsBinding
import com.ipt.runinpark.dialogs.AddGroupDialog
import com.ipt.runinpark.dialogs.CreateGroupDialog
import com.ipt.runinpark.model.Group
import com.ipt.runinpark.ui.adapter.GroupListAdapter
import com.ipt.runinpark.viewModel.UserViewModel
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
        //dialog para ver grupos
        val groupsDialog = object : AddGroupDialog(this.requireContext(),groupsFiltered,viewModel,this@GroupsFragment){

        }
        groupsBinding= FragmentGroupsBinding.inflate(inflater,container,false)

        val adapter = GroupListAdapter(viewModel.adapterGroups,this@GroupsFragment.requireContext(),viewModel.user.value?.userId){
            showGroupPage(it)
        }
        groupsBinding.groupsViewList.adapter=adapter
        groupsBinding.groupsViewList.layoutManager= LinearLayoutManager(this@GroupsFragment.requireContext())
        groupsBinding.groupsViewList.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))


        groupsBinding.btnCreateGroup.setOnClickListener{
            //dialog para criar grupo
            val createGroupDialog = object : CreateGroupDialog(this.requireContext(),viewModel,this@GroupsFragment){

            }
            if(!createGroupDialog.isShowing){
                createGroupDialog.show()

            }

        }
        viewModel.loggedIn.observe(viewLifecycleOwner, Observer {
            //Se não estiver autenticado:
            if(viewModel.loggedIn.value == false){
                //Carrega todos os grupos,e esconde botões que permite criar/vergrupos
                viewModel.allGroups.value?.let {}?:viewModel.loadGroups()
                groupsBinding.addCreateBtnsLayout.visibility = View.GONE

            }else{
                //Carrega os grupos a que um utilizador pertence
                viewModel.user.value?.userId?.let {user ->
                    viewModel.userGroups.value?.let {} ?: viewModel.loadUserGroups(user)
                    print("adawd")
                }
                groupsBinding.addCreateBtnsLayout.visibility = View.VISIBLE
            }
        })






//Quando clicado no botão para se juntar a um grupo,
groupsBinding.btnAddGroup.setOnClickListener{
    //Ao utilizador vão ser mostrados todos os grupos menos aqueles que ele já pertence
//a variável "groupsFiltered" será o resultado dessa equação
    groupsFiltered.clear()
    //Carregados todos os grupos do utilizador
    viewModel.user.value?.userId?.let { it1 -> viewModel.loadUserGroups(it1) }
//É necessário uma corotina pois estamos a dar "collect" do estado de uma variável do tipo Flow<Int>,que irá servir como semáforo
    //Esta variável é posta a 1 depois dos grupos terem sido carregados,e a 0 depois do processo abaixo(filtrar os grupos) estar concluído
GlobalScope.launch (Dispatchers.Main){
    context?.let { it1 ->
        viewModel.groupsFiltered.collect { value ->
        if(value==1){

            val idsList  = mutableListOf<Int>()
            val filteredList  = mutableListOf<Group>()
            for(i in viewModel.userGroups.value!!){
                idsList.add(i.groupId)
            }

            for(i in viewModel.allGroups.value!!){
                if(i.groupId !in idsList){
                    filteredList.add(i)
                }
            }
            groupsFiltered.clear()
            groupsFiltered.addAll(filteredList)
            groupsDialog.adapter?.notifyDataSetChanged()
            if(!groupsDialog.isShowing){
                groupsDialog.show()
                viewModel._groupsFiltered.value=0
            }
        }
    }
    }
}
    viewModel.loadGroups("dialog")
}
        //Mostra avisos se não houver grupos criados ou se o utilizador não tiver em nenhum grupo
        viewModel.allGroups.observe(viewLifecycleOwner, Observer {
            if(viewModel.loggedIn.value == true){
                if(viewModel.allGroups.value?.size == 0){
                    if(groupsBinding.noGroups.visibility == View.GONE){
                        groupsBinding.noGroups.visibility= View.VISIBLE
                    }
                }else{
                    if(groupsBinding.noGroups.visibility == View.VISIBLE) groupsBinding.noGroups.visibility= View.GONE

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
            activity?.findViewById<BottomNavigationView>(com.ipt.runinpark.R.id.bottomNavView)?.menu?.getItem(0)?.setChecked(true)


        }

    }






}