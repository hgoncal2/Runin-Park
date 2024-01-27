package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.model.User

class MembersListAdapter(private val members: List<User>, private val context: Context, private val groupOwnerId : Int? = null,private val currentUser : Int? = null, private val itemClickListener: (user : User,desc:String) -> Unit) :
    RecyclerView.Adapter<MembersListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = members[position]

        if (groupOwnerId != null) {
            holder.bindView(user,itemClickListener,groupOwnerId,currentUser)
        }else{
            holder.bindView(user,itemClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.member_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return members.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(user: User,itemClickListener : (user : User,desc: String) -> Unit,ownerId : Int? = null,currentUser : Int? = null) {



            val username: TextView = itemView.findViewById(R.id.member_item_username)
            val img: ImageView = itemView.findViewById(R.id.member_item_img)
            val name: TextView = itemView.findViewById(R.id.member_item_name)
            val exclude: TextView = itemView.findViewById(R.id.member_item_exclude)
            val owner: TextView = itemView.findViewById(R.id.member_item_owner)

            user.userId.let { if(it == currentUser){username.text="Me"} else{username.text = user.username} }
            name.text = user.name
            if(user.userId == ownerId){
                owner.visibility = View.VISIBLE
            }else{
                owner.visibility = View.GONE
            }
            if(currentUser == ownerId && user.userId != ownerId){
                exclude.visibility = View.VISIBLE
            }else{
                exclude.visibility = View.GONE
            }

            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(com.example.myapplication.R.drawable.loading_spinning)
                .error(com.example.myapplication.R.drawable.user_logged_in)
                .circleCrop()
            Glide.with(this.itemView.context).load(user.profilePhoto).diskCacheStrategy(
                DiskCacheStrategy.ALL).skipMemoryCache(false).apply(options).timeout(6000).into(img)
            img.setOnClickListener{
                itemClickListener(user,"profile")
            }
            exclude.setOnClickListener{
                itemClickListener(user,"exclude")
            }








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
