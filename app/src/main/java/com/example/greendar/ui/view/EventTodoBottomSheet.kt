package com.example.greendar.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.greendar.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventTodoBottomSheet():BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.bottom_sheet_dialog_event_todo, container, false)
    }



}