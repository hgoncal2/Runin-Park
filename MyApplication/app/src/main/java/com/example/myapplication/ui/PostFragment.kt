package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentPostBinding
import com.example.myapplication.model.Post
import com.example.myapplication.ui.adapter.PostListAdapter
import com.example.myapplication.viewModel.UserViewModel


class PostFragment : Fragment() {
   private lateinit var postFragmentBinding : FragmentPostBinding
    private var postsList = mutableListOf<Post>()
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        postFragmentBinding = FragmentPostBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment


        val adapter = PostListAdapter(postsList,this@PostFragment.requireContext(),viewModel.user.value?.userId){

        }
        postFragmentBinding.postsViewList.adapter=adapter
        postFragmentBinding.postsViewList.layoutManager= LinearLayoutManager(this@PostFragment.requireContext())
        postFragmentBinding.postsViewList.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
        viewModel.groupPosts.observe(viewLifecycleOwner, Observer {
            viewModel.groupPosts.value?.let {
                postsList.clear()
                postsList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.userGroups.observe(viewLifecycleOwner, Observer {
            viewModel.selectedGroup.value?.let {_ ->
                if( it.contains(viewModel.selectedGroup.value) == false){
                    postFragmentBinding.groupPost.visibility = View.GONE
                }else{
                    postFragmentBinding.groupPost.visibility = View.VISIBLE

                }
            }



        })


        return postFragmentBinding.root
    }




}