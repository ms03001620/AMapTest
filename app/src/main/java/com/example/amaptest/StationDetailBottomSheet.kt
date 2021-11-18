package com.example.amaptest

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.amaptest.databinding.FragmentStationDetailBinding
import com.example.amaptest.databinding.FragmentStationDetailDemoBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StationDetailBottomSheet : BottomSheetDialogFragment() {
    lateinit var binding: FragmentStationDetailDemoBinding

    override fun getTheme(): Int {
        return R.style.StationDetailDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station_detail_demo, container, false)


       // (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED

        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(p0: DialogInterface?, p1: Int, p2: KeyEvent?): Boolean {
                return false
            }
        })

        return binding.root
    }

    companion object {
        const val TAG = "StationDetailBottomSheet"
    }
}