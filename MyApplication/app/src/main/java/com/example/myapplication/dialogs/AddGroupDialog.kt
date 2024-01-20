package com.example.myapplication.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Group
import com.example.myapplication.ui.GroupPageFragment
import com.example.myapplication.ui.GroupsFragment
import com.example.myapplication.ui.adapter.AddGroupAdapter
import com.example.myapplication.viewModel.UserViewModel


abstract class AddGroupDialog(context: Context,private var groupList : List<Group>,private val viewModel: UserViewModel,private val frag: Fragment): Dialog(context) {
     var adapter: AddGroupAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())


        val view = LayoutInflater.from(context).inflate(R.layout.add_group_dialog, null)


        setContentView(view)


        setCanceledOnTouchOutside(true)

setOnDismissListener{
    cancel()
}
        setOnCancelListener {
            cancel()
        }
        setCancelable(true)
        // Set up the RecyclerView in the dialog
        val recycler = view.findViewById<RecyclerView>(R.id.add_group_recycler_view)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))



        var adapter = AddGroupAdapter(groupList,context){
            cancel()
            viewModel.selectedGroup.value = it
            viewModel.replaceFragment(frag, GroupPageFragment())

        }
        view.findViewById<RecyclerView>(R.id.add_group_recycler_view).adapter = adapter
    }
}