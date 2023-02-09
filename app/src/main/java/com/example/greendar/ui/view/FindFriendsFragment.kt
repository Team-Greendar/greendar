package com.example.greendar.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.greendar.databinding.FragmentFindFriendsBinding

class FindFriendsFragment: Fragment() {
    private lateinit var binding:FragmentFindFriendsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFindFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }
}