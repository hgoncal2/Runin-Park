package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.GroupPageFragment
import com.example.myapplication.R
import com.example.myapplication.model.Group
import com.example.myapplication.ui.MainActivity

class GroupListAdapter(private val groups: List<Group>,private val context: Context,private val itemClickListener: (group : Group) -> Unit) :
    RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]

        holder.bindView(group,itemClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.group_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groups.size
    }
    class ViewHolder(itemView: View,) : RecyclerView.ViewHolder(itemView) {
        fun bindView(group: Group,itemClickListener : (group : Group) -> Unit) {

            val city: TextView = itemView.findViewById(R.id.group_item_city)
           // val id: TextView = itemView.findViewById(R.id.group_item_id)
            val nome: TextView = itemView.findViewById(R.id.group_item_nome)

         //   val createdDate: TextView = itemView.findViewById(R.id.group_item_createdDate)
           // val ownerId: TextView = itemView.findViewById(R.id.group_item_ownerId)

            city.text = "${group.city}"
            nome.text = "${group.name}"
            itemView.setOnClickListener{
                itemClickListener(group)
            }





        }

    }
    }
