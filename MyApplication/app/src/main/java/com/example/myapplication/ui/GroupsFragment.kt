package com.example.myapplication.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGroupsBinding
import com.example.myapplication.model.Group
import com.example.myapplication.model.User
import com.example.myapplication.retrofit.RetrofitInit
import com.example.myapplication.ui.adapter.GroupListAdapter
import com.example.myapplication.viewModel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupsFragment : Fragment() {
   private lateinit var groupsBinding: FragmentGroupsBinding
   private  var groups : List<Group>? = null
    private val viewModel: UserViewModel by activityViewModels()
    private  var user: User? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        groupsBinding= FragmentGroupsBinding.inflate(inflater,container,false)

            loadGroups()
            user = viewModel.user.value
        if(user != null){
            groupsBinding.user.setText(user?.name)
            groupsBinding.user.visibility= View.VISIBLE
            groupsBinding.groupsViewList.layoutParams.height = LayoutParams.WRAP_CONTENT
        }else{
            groupsBinding.groupsViewList.layoutParams.height = LayoutParams.MATCH_PARENT
        }

        return groupsBinding.root
    }

    override fun onResume() {
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.menu?.getItem(1)?.setChecked(true)

        super.onResume()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun loadGroups(){

        val call = RetrofitInit().groupService().getGroups()
        call.enqueue(
            object : Callback<List<Group>> {
                override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                    t.printStackTrace()

                }
                override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {

                    groups = response.body()
                    val adapter = GroupListAdapter(groups!!,this@GroupsFragment.requireContext())
                    groupsBinding.groupsViewList.adapter=adapter
                    groupsBinding.groupsViewList.layoutManager= LinearLayoutManager(this@GroupsFragment.requireContext())
                    groupsBinding.groupsViewList.addItemDecoration(DividerItemDecoration(this@GroupsFragment.requireContext(), LinearLayoutManager.VERTICAL))



                }
            }
        )



    }
}