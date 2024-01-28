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
import com.ipt.runinpark.databinding.FragmentGroupMembersBinding
import com.ipt.runinpark.model.User
import com.ipt.runinpark.ui.adapter.MembersListAdapter
import com.ipt.runinpark.viewModel.UserViewModel


class GroupMembersFragment : Fragment() {
    private lateinit var groupMembersFragmentBinding: FragmentGroupMembersBinding
    private var groupMembers = mutableListOf<User>()
    private val viewModel: UserViewModel by activityViewModels()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        groupMembersFragmentBinding = FragmentGroupMembersBinding.inflate(inflater,container,false)
        //Existe um click listener para duas possibilidades:
        //1 - É clicado na foto de perfil de um utilizador,que deverá redirecionar para a página de perfil desse utilizador
        //2- é clicado no item de excluir um utilizador,e consequentemente esse utilizador deverá ser excluido do grupo
        val adapter = MembersListAdapter(groupMembers,this@GroupMembersFragment.requireContext(),viewModel.selectedGroup.value?.ownerId,viewModel.user.value?.userId){
            user,desc ->
                if(desc == "profile"){
                    viewModel.selectedUser.value=user.also { viewModel.replaceFragment(this,UserProfileFragment()) }
                }
            if(desc == "exclude"){
                viewModel.selectedGroup.value?.groupId?.let { viewModel.user.value?.token?.let { it1 ->
                    viewModel.removeUserFromGroup(
                        it1,it,user.userId,this)
                } }
            }

        }
        groupMembersFragmentBinding.groupMembersView.adapter=adapter
        groupMembersFragmentBinding.groupMembersView.layoutManager= LinearLayoutManager(this@GroupMembersFragment.requireContext())
       //Adiciona linha horizontal que separa cada item da lista
        groupMembersFragmentBinding.groupMembersView.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
       //Se os membros do grupo mudarem,é atualizada a lista
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