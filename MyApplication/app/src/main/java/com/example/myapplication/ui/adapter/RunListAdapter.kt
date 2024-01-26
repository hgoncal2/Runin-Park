package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Run

class RunListAdapter(private val runs: List<Run>, private val context: Context, private val userId : Int? = null, private val itemClickListener: (run : Run) -> Unit) :
    RecyclerView.Adapter<RunListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val run = runs[position]

        if (userId != null ) {
            holder.bindView(run,itemClickListener,userId)
        }else{
            holder.bindView(run,itemClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return runs.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(run: Run,itemClickListener : (run : Run) -> Unit,userId : Int? = null) {

            val date: TextView = itemView.findViewById(R.id.post_item_createdDate)
            val img: ImageView = itemView.findViewById(R.id.post_item_img)
            val imgPost: ImageView = itemView.findViewById(R.id.post_item_photo)
            val text: TextView = itemView.findViewById(R.id.post_item_text)
            val user: TextView = itemView.findViewById(R.id.post_item_user)
            val groupName: TextView = itemView.findViewById(R.id.post_item_group_name)
            val delete: TextView = itemView.findViewById(R.id.post_item_delete)




/*
            itemView.setOnClickListener{
                itemClickListener(post)
            }

            userId?.let {
                if(post.userId == it){
                   // owner.visibility = View.VISIBLE
                }
            }




*/



        }

    }
    }
