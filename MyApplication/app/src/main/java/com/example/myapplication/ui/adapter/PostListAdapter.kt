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
import com.example.myapplication.model.Post

class PostListAdapter(private val posts: List<Post>,private val context: Context, private val userId : Int? = null, private val itemClickListener: (post : Post) -> Unit) :
    RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]

        if (userId != null) {
            holder.bindView(post,itemClickListener,userId)
        }else{
            holder.bindView(post,itemClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(post: Post,itemClickListener : (post : Post) -> Unit,userId : Int? = null) {

            val date: TextView = itemView.findViewById(R.id.post_item_createdDate)
            val img: ImageView = itemView.findViewById(R.id.post_item_img)
        val text: TextView = itemView.findViewById(R.id.post_item_text)
         val user: TextView = itemView.findViewById(R.id.post_item_user)

            date.text = post.createdDate.toString()
            if(userId == post.userId){
                user.text = "Me"
            }else{
                user.text = post.username
            }
    text.text = post.text

            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(com.example.myapplication.R.drawable.loading_spinning)
                .error(com.example.myapplication.R.drawable.user_logged_in)
                .circleCrop()
            Glide.with(this.itemView.context).load(post.profilePhoto).diskCacheStrategy(
                DiskCacheStrategy.ALL).skipMemoryCache(false).apply(options).timeout(6000).into(img)
         //   val createdDate: TextView = itemView.findViewById(R.id.group_item_createdDate)
           // val ownerId: TextView = itemView.findViewById(R.id.group_item_ownerId)

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
