package com.ipt.runinpark.ui.adapter

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
import com.ipt.runinpark.R
import com.ipt.runinpark.model.Group
/* Este adapter,juntamente com vários outros usados, usa um item click listener que pode ser acedido
pelas classes que inicializam uma instância deste.Este método foi inspirado por esta thread:
https://stackoverflow.com/questions/49969278/recyclerview-item-click-listener-the-right-way
*/
class GroupListAdapter(private val groups: List<Group>,private val context: Context,private val userId : Int? = null,private val itemClickListener: (group : Group) -> Unit) :
    RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Associa um grupo a uma posição do array de grupos
        val group = groups[position]
        //Passa o ID do utilizador como argumento se este existir

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

            val city: TextView = itemView.findViewById(R.id.member_item_name)
            val nome: TextView = itemView.findViewById(R.id.member_item_username)
            val owner: TextView = itemView.findViewById(R.id.owner)
            val img : ImageView = itemView.findViewById(R.id.member_item_img)

            //Atribui valores dos campos às respetivas views
            city.text = "${group.city}"
            nome.text = "${group.name}"
            //Passa um grupo como valor do item click listener,de modo a ser acedido pela classe
            //que inicializa uma instância deste adapter
            itemView.setOnClickListener{
                itemClickListener(group)
            }
            //Se user for dono do grupo,adiciona ícone que o indica
            userId?.let {
                if(group.ownerId == it){
                    owner.visibility = View.VISIBLE
                }else{
                    owner.visibility = View.GONE
                }
            }
            //Usado GLIDE para inserir a fotografia do grupo

            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(com.ipt.runinpark.R.drawable.loading_spinning)
                .error(com.ipt.runinpark.R.drawable.default_groups)
                .circleCrop()
            Glide.with(this.itemView.context).load(group.groupPhoto).diskCacheStrategy(
                DiskCacheStrategy.ALL).skipMemoryCache(false).apply(options).timeout(6000).into(img)






        }

    }
    }
