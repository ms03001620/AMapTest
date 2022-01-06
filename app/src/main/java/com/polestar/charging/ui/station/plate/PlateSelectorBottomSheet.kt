package com.polestar.charging.ui.station.plate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.amaptest.R
import com.example.amaptest.databinding.FragmentStationDetailDemoBinding
import com.example.amaptest.logd
import com.example.amaptest.ui.main.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 车牌选择对话框
 */
class PlateSelectorBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentStationDetailDemoBinding
    private lateinit var plates: ArrayList<Plate>
    private lateinit var adapter: PlateSelectorAdapter
    private var defaultVin: String? = null

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDefaultVin()
        initArguments()
        initList()
        initPlateManagerBtn()
    }

    private fun initPlateManagerBtn() {
        binding.textPlateEdit.setOnClickListener {
            logd("textPlateEdit onClick", TAG)
        }
    }

    private fun loadDefaultVin() {
        defaultVin = null
    }

    private fun saveDefaultVin(plate: Plate) {
        defaultVin = plate.vin
    }

    private fun initArguments() {
        plates = arguments?.getParcelableArrayList(EXTRA_DATA_ARGUMENTS) ?: ArrayList()
    }

    private fun initList() {
        binding.list.layoutManager = LinearLayoutManager(context)
        adapter = PlateSelectorAdapter {
            saveDefaultVin(it)
            adapter.updateDefaultVin(it.vin)
        }

        binding.list.adapter = adapter
        adapter.updatePlateInfo(plates)
        setListHeight(calcListHeight(plates.size))
    }

    private fun calcListHeight(size: Int): Int {
        val max = LIST_MAX_DISPLAY
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
        const val TAG = "PlateSelectorBottomSheet"
        const val LIST_MAX_DISPLAY = 5
        const val LIST_HEIGHT_MIN = 0
        const val EXTRA_DATA_ARGUMENTS = "data"
        const val LIST_ITEM_HEIGHT = 64 //dp
    }
}