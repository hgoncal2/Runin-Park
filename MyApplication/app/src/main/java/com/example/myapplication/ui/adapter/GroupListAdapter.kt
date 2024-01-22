package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Group

class GroupListAdapter(private val groups: List<Group>,private val context: Context,private val userId : Int? = null,private val itemClickListener: (group : Group) -> Unit) :
    RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]

        if (userId != null) {
            holder.bindView(group,itemClickListener,userId)
        }else{
            holder.bindView(group,itemClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.group_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groups.size
    }
    class ViewHolder(itemView: View,) : RecyclerView.ViewHolder(itemView) {
        fun bindView(group: Group,itemClickListener : (group : Group) -> Unit,userId : Int? = null ) {

            val city: TextView = itemView.findViewById(R.id.group_item_city)
           // val id: TextView = itemView.findViewById(R.id.group_item_id)
            val nome: TextView = itemView.findViewById(R.id.group_item_nome)
            val owner: TextView = itemView.findViewById(R.id.owner)

         //   val createdDate: TextView = itemView.findViewById(R.id.group_item_createdDate)
           // val ownerId: TextView = itemView.findViewById(R.id.group_item_ownerId)

            city.text = "${group.city}"
            nome.text = "${group.name}"
            itemView.setOnClickListener{
                itemClickListener(group)
            }

            userId?.let {
                if(group.ownerId == it){
                    owner.visibility = View.VISIBLE
                }
            }





        }

    }
    }
