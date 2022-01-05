package com.example.amaptest.plate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.amaptest.R
import com.example.amaptest.databinding.FragmentStationDetailDemoBinding
import com.example.amaptest.ui.main.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StationDetailBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentStationDetailDemoBinding

    private lateinit var plateInfo: PlateInfo

    override fun getTheme() = R.style.StationDetailDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_station_detail_demo, container, false
        )
        initArguments()
        initList()
        return binding.root
    }

    private fun initArguments(){
        plateInfo = arguments?.get(EXTRA_DATA_ARGUMENTS) as PlateInfo
    }

    private fun initList() {
        binding.list.layoutManager = LinearLayoutManager(context)
        val adapter = PlateSelectAdapter({

        })
        adapter.submitList(plateInfo.plates)

        binding.list.adapter = adapter

        val h = if (plateInfo.plates.size > 0) {
            100 * plateInfo.plates.size
        } else {
            LIST_HEIGHT_MIN
        }
        setListHeight(h)
    }


    private fun setListHeight(heightDp: Int) {
        val params: ViewGroup.LayoutParams = binding.list.getLayoutParams()
        params.height = heightDp.dp
        binding.list.layoutParams = params
    }


    companion object {
        const val TAG = "StationDetailBottomSheet"
        const val LIST_HEIGHT_MIN = 16
        const val EXTRA_DATA_ARGUMENTS = "data"
    }
}