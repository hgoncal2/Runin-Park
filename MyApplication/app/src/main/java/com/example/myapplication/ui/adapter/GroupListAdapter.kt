package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Group

class GroupListAdapter(private val groups: List<Group>,private val context: Context) :
    RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: GroupListAdapter.ViewHolder, position: Int) {
        val group = groups[position]
        holder?.let {
            it.bindView(group)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.group_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groups.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(group: Group) {
            val city: TextView = itemView.findViewById(R.id.group_item_city)
           // val id: TextView = itemView.findViewById(R.id.group_item_id)
            val nome: TextView = itemView.findViewById(R.id.group_item_nome)
         //   val createdDate: TextView = itemView.findViewById(R.id.group_item_createdDate)
           // val ownerId: TextView = itemView.findViewById(R.id.group_item_ownerId)

            city.text = "${group.city}"
            nome.text = "${group.name}"


        }

    }
    }
