package com.example.myapplication.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDashBoardGroupsBinding
import com.example.myapplication.databinding.FragmentMyPostsBinding
import com.example.myapplication.model.Group
import com.example.myapplication.model.Post
import com.example.myapplication.ui.adapter.PostListAdapter
import com.example.myapplication.viewModel.UserViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [MyPostsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPostsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val viewModel: UserViewModel by activityViewModels()

    private lateinit var dashBoardPostsBinding: FragmentMyPostsBinding
    private var postsList = mutableListOf<Post>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        dashBoardPostsBinding= FragmentMyPostsBinding.inflate(inflater,container,false)

        val adapter = PostListAdapter(postsList,this@MyPostsFragment.requireContext(),viewModel.user.value?.userId){

        }
        dashBoardPostsBinding.myPostsView.adapter=adapter
        dashBoardPostsBinding.myPostsView.layoutManager= LinearLayoutManager(this@MyPostsFragment.requireContext())
        dashBoardPostsBinding.myPostsView.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
        viewModel.user.value?.let { viewModel.loadUserPosts(it.userId) }
        viewModel.userPosts.observe(viewLifecycleOwner, Observer {
            postsList.clear()
            postsList.addAll(it)
            adapter.notifyDataSetChanged()

        })





        return dashBoardPostsBinding.root
    }


}