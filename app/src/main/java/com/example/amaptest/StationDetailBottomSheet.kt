package com.example.amaptest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.amaptest.databinding.FragmentStationDetailDemoBinding
import com.example.amaptest.ui.main.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class StationDetailBottomSheet : BottomSheetDialogFragment() {
    lateinit var binding: FragmentStationDetailDemoBinding

    override fun getTheme() = R.style.StationDetailDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station_detail_demo, container, false)
        setListHeight(40)
        dialog?.setCanceledOnTouchOutside(true)
        return binding.root
    }

    private fun setListHeight(heightDp: Int){
        val params: ViewGroup.LayoutParams = binding.list.getLayoutParams()
        params.height = heightDp.dp
        binding.list.layoutParams = params
    }


    companion object {
        const val TAG = "StationDetailBottomSheet"
    }
}