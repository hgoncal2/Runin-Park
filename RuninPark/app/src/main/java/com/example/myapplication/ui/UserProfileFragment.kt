package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentUserProfileBinding
import com.example.myapplication.viewModel.UserViewModel


class UserProfileFragment : Fragment() {
    private lateinit var userProfileFragmentBinding: FragmentUserProfileBinding
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userProfileFragmentBinding = FragmentUserProfileBinding.inflate(inflater,container,false)
        //impede o User de inserir dados nos campos
        userProfileFragmentBinding.profileName.keyListener = null;
        userProfileFragmentBinding.profileLastName.keyListener = null;
        userProfileFragmentBinding.profileBirthDate.keyListener = null;
        userProfileFragmentBinding.profileWeight.keyListener = null;
        userProfileFragmentBinding.profileHeight.keyListener = null;
        userProfileFragmentBinding.profileAddress.keyListener = null;

        viewModel.selectedUser.observe(viewLifecycleOwner,{
            viewModel.selectedUser.value?.let {
                it.let {
                    it.username?.let { v -> userProfileFragmentBinding.profileUsername.setText(v) } ?: userProfileFragmentBinding.profileUsername.setText("")
                    it.name?.let { v -> userProfileFragmentBinding.profileName.setText(v) } ?: userProfileFragmentBinding.profileName.setText("")
                    it.lastName?.let{v -> userProfileFragmentBinding.profileLastName.setText(v)} ?: userProfileFragmentBinding.profileLastName.setText("")
                    it.birthDate?.let{v -> userProfileFragmentBinding.profileBirthDate.setText(viewModel.dateFormatter.format(v))} ?: userProfileFragmentBinding.profileBirthDate.setText("")
                    it.weight?.let{v -> userProfileFragmentBinding.profileWeight.setText("${v}")} ?: userProfileFragmentBinding.profileWeight.setText("")
                    it.height?.let{v -> userProfileFragmentBinding.profileHeight.setText("${v}")} ?: userProfileFragmentBinding.profileHeight.setText("")
                    it.address?.let{v -> userProfileFragmentBinding.profileAddress.setText(v)} ?: userProfileFragmentBinding.profileAddress.setText("")
                    it.profilePhoto.let { loadProfilePic(it!!,userProfileFragmentBinding.profileImg,true) }
                }

            }
        })

        return userProfileFragmentBinding.root
    }
    //carrega a foto de perfil do user
    private fun loadProfilePic(path: String, imageView: ImageView, cache: Boolean){
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(com.example.myapplication.R.drawable.loading_spinning)
            .error(R.drawable.user_logged_in)
            .circleCrop()
        Glide.with(this@UserProfileFragment.requireContext()).load(path).diskCacheStrategy(
            DiskCacheStrategy.ALL).skipMemoryCache(cache).apply(options).timeout(10000).into(imageView)

    }

}