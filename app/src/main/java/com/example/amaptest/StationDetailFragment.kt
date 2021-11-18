package com.example.amaptest

import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.amaptest.databinding.FragmentStationDetailBinding

class StationDetailFragment: Fragment() {
    lateinit var binding: FragmentStationDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station_detail, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "StationDetailBottomSheet"
    }

}