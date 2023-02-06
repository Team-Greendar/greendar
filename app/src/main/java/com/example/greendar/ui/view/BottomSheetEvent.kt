package com.example.greendar.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.greendar.databinding.BottomSheetDialogEventTodoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetEvent():BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding = BottomSheetDialogEventTodoBinding.inflate(inflater, container, false)
        //여기서 행동 정의

        return binding.root
    }


}