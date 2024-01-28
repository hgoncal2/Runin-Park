package com.ipt.runinpark.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ipt.runinpark.databinding.FragmentDashBoardGroupsBinding
import com.ipt.runinpark.model.Group
import com.ipt.runinpark.ui.adapter.GroupListAdapter
import com.ipt.runinpark.viewModel.UserViewModel


class DashBoardGroups : Fragment() {
    private val viewModel: UserViewModel by activityViewModels()

    private lateinit var dashBoardGroupsBinding: FragmentDashBoardGroupsBinding
    private var groupList = mutableListOf<Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dashBoardGroupsBinding= FragmentDashBoardGroupsBinding.inflate(inflater,container,false)
        //Carrega gruppos de um utilizador
        viewModel.user.value?.let { viewModel.loadUserGroups(it.userId) }

        val recycler =dashBoardGroupsBinding.recyclerDashboardGroups
        recycler.layoutManager = LinearLayoutManager(this.requireContext())
        recycler.addItemDecoration(DividerItemDecoration(this.requireContext(), LinearLayoutManager.VERTICAL))
        //cria adapter com um lista local
        val adapter = GroupListAdapter(groupList,this.requireContext(),
            viewModel.user.value?.userId
        ){
//Vai para a página de um grupo
            viewModel.selectedGroup.value = it
            viewModel.replaceFragment(this, GroupPageFragment())
        }

        recycler.adapter = adapter
        viewModel.userGroups.observe(viewLifecycleOwner, Observer {
            //filtra grupos de um utilizador para mostrar apenas aqueles criados por este
            if(!viewModel.userGroups.value?.filter { it.ownerId == viewModel.user.value?.userId }.isNullOrEmpty() ){
                //Desaparece aviso que o utilizador não tem grupos criados
                dashBoardGroupsBinding.noGroupsCreated.visibility = View.GONE
                //lista local é limpa
                groupList.clear()
                //e consequentemente adiciona-se a essa lista os grupos pretendidos
                groupList.addAll(viewModel.userGroups.value?.filter { it.ownerId == viewModel.user.value?.userId } as MutableList<Group>)
                //E avisa-se o adapter que o data set mudou,para ele dar refresh
                adapter?.notifyDataSetChanged()
            }else{
                    //Mostra aviso que o utilizador não tem grupos criados
                    dashBoardGroupsBinding.noGroupsCreated.visibility = View.VISIBLE

            }


        })
        return dashBoardGroupsBinding.root
    }


}