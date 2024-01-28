package com.ipt.runinpark.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ipt.runinpark.R
import com.ipt.runinpark.model.Group
import com.ipt.runinpark.ui.GroupPageFragment
import com.ipt.runinpark.ui.adapter.AddGroupAdapter
import com.ipt.runinpark.viewModel.UserViewModel


abstract class AddGroupDialog(context: Context,private var groupList : List<Group>,private val viewModel: UserViewModel,private val frag: Fragment): Dialog(context) {
    //Inicia a variável adapter que irá ser usada para a recycler view dos grupos
    var adapter: AddGroupAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())


        val view = LayoutInflater.from(context).inflate(R.layout.add_group_dialog, null)


        setContentView(view)
        //Permite cancelar o dialog clicando fora da view deste
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        //Configuração da recycler view
        val recycler = view.findViewById<RecyclerView>(R.id.add_group_recycler_view)
        recycler.layoutManager = LinearLayoutManager(frag.requireContext())
        recycler.addItemDecoration(DividerItemDecoration(frag.requireContext(), LinearLayoutManager.VERTICAL))


        //Click listener a cada item da recycler view,para saber qual grupo foi selecionado
         adapter = AddGroupAdapter(groupList,context){
            cancel()
            viewModel.selectedGroup.value = it
            viewModel.replaceFragment(frag, GroupPageFragment())

        }
        recycler.adapter = adapter



    }




}