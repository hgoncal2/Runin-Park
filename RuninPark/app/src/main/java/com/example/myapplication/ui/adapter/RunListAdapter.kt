package com.example.myapplication.ui.adapter

import android.content.Context
import android.icu.text.SimpleDateFormat
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
        val view = LayoutInflater.from(context).inflate(R.layout.run_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return runs.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(run: Run,itemClickListener : (run : Run) -> Unit,userId : Int? = null) {

            val date: TextView = itemView.findViewById(R.id.run_item_createdDate)
            val img: ImageView = itemView.findViewById(R.id.run_item_img)
            val imgPost: ImageView = itemView.findViewById(R.id.run_item_photo)
            val distance: TextView = itemView.findViewById(R.id.run_item_distance)
            val time: TextView = itemView.findViewById(R.id.run_item_time)
            val user: TextView = itemView.findViewById(R.id.run_item_user)

            val delete: TextView = itemView.findViewById(R.id.run_item_delete)
            val score: TextView = itemView.findViewById(R.id.run_item_score_value)


            if(userId == run.userId){
                user.text = "Me"
            }else{
                user.text = run.username
            }
            date.text = SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(run.createdDate);
            distance.text = "Distância: ${run.distance} KMs"
            time.text = "Tempo: ${run.time} Horas"
            //De momento não é possível apagar corridas,mas poderá ser no futuro
            /*
            if(run.userId == userId ){
                delete.visibility = View.VISIBLE
            }else{
                delete.visibility = View.GONE
            }

             */
            score.text = run.rating.toString()
            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(com.example.myapplication.R.drawable.loading_spinning)
                .error(com.example.myapplication.R.drawable.user_logged_in)
                .circleCrop()
            Glide.with(this.itemView.context).load(run.userPhoto).diskCacheStrategy(
                DiskCacheStrategy.ALL).skipMemoryCache(false).apply(options).timeout(6000).into(img)
            //   val createdDate: TextView = itemView.findViewById(R.id.group_item_createdDate)
            // val ownerId: TextView = itemView.findViewById(R.id.group_item_ownerId)
            if(run.runPhoto != null){
                val options: RequestOptions = RequestOptions()
                    .centerCrop()
                Glide.with(this.itemView.context).load(run.runPhoto).diskCacheStrategy(
                    DiskCacheStrategy.ALL).skipMemoryCache(false).apply(options).timeout(6000).into(imgPost).also { imgPost.visibility=View.VISIBLE }
            }else{
                imgPost.visibility=View.GONE
            }




        }

    }
    }
