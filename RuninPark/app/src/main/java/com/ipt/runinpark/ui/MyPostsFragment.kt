package com.ipt.runinpark.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ipt.runinpark.databinding.FragmentMyPostsBinding
import com.ipt.runinpark.model.Post
import com.ipt.runinpark.ui.adapter.PostListAdapter
import com.ipt.runinpark.viewModel.UserViewModel


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
                post -> viewModel.deletePost(
            viewModel.user.value!!.token!!,post.groupId,post.postId,this,"profile",post.userId
        )
        }
        dashBoardPostsBinding.myPostsView.adapter=adapter
        dashBoardPostsBinding.myPostsView.layoutManager= LinearLayoutManager(this@MyPostsFragment.requireContext())
        dashBoardPostsBinding.myPostsView.addItemDecoration(DividerItemDecoration(this.context, LinearLayoutManager.VERTICAL))
        viewModel.user.value?.let { viewModel.loadUserPosts(it.userId) }
        viewModel.userPosts.observe(viewLifecycleOwner, Observer {
            if(!it.isNullOrEmpty() ){
                dashBoardPostsBinding.noPostsCreated.visibility = View.GONE
                postsList.clear()
                postsList.addAll(it)
                adapter.notifyDataSetChanged()
            }else{

                dashBoardPostsBinding.noPostsCreated.visibility = View.VISIBLE

            }


        })






        return dashBoardPostsBinding.root
    }


}