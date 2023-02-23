package com.example.greendar.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.greendar.databinding.BottomSheetDialogDailyTodoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DailyTodoBottomSheet():BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding = BottomSheetDialogDailyTodoBinding.inflate(inflater, container, false)

        return binding.root
    }




}