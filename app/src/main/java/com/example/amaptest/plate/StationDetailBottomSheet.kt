package com.example.amaptest.plate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.amaptest.R
import com.example.amaptest.databinding.FragmentStationDetailDemoBinding
import com.example.amaptest.ui.main.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StationDetailBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentStationDetailDemoBinding

    private lateinit var plates: ArrayList<Plate>

    private var defaultVin: String? = null

    lateinit var adapter: PlateSelectAdapter

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
        initDefaultVin()
        initArguments()
        initList()
        initPlateManagerBtn()
        return binding.root
    }

    private fun initPlateManagerBtn() {
        binding.textPlateEdit.setOnClickListener {
            Toast.makeText(context, "Enter PlateCenter", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initDefaultVin(){
        defaultVin = null
    }

    private fun initArguments(){
        plates = arguments?.getParcelableArrayList(EXTRA_DATA_ARGUMENTS) ?: ArrayList<Plate>()
    }

    private fun initList() {
        binding.list.layoutManager = LinearLayoutManager(context)
        adapter = PlateSelectAdapter{
            //Toast.makeText(context, "${it.string}", Toast.LENGTH_SHORT).show()
            defaultVin = it.vin
            adapter.updateDefaultVin(defaultVin)
        }

        binding.list.adapter = adapter
        adapter.updatePlateInfo(plates)
        setListHeight(calcListHeight(plates.size))
    }

    private fun calcListHeight(size: Int): Int {
        val max = 5
        if (size == 0) {
            return LIST_HEIGHT_MIN
        }
        return if (size >= max) {
            LIST_ITEM_HEIGHT * max
        } else {
            LIST_ITEM_HEIGHT * size
        }
    }

    private fun setListHeight(heightDp: Int) {
        val params: ViewGroup.LayoutParams = binding.list.getLayoutParams()
        params.height = heightDp.dp
        binding.list.layoutParams = params
    }

    companion object {
        const val TAG = "StationDetailBottomSheet"
        const val LIST_HEIGHT_MIN = 0
        const val EXTRA_DATA_ARGUMENTS = "data"
        const val LIST_ITEM_HEIGHT = 64 //dp
    }
}