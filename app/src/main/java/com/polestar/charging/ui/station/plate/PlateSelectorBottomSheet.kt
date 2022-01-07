package com.polestar.charging.ui.station.plate

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ChargingPlateSelectorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.polestar.base.utils.logd
import com.polestar.charging.utils.ClickUtils


/**
 * 车牌选择对话框
 */
class PlateSelectorBottomSheet : BottomSheetDialogFragment() {
    private val vehiclesViewModel by lazy {
        ViewModelProvider(requireActivity(), ViewModelFactory())[VehiclesViewModel::class.java]
    }
    private lateinit var binding: ChargingPlateSelectorBinding
    private lateinit var adapter: PlateSelectorAdapter

    override fun getTheme() = R.style.ChargingPlateDetailDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.charging_plate_selector, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initPlateManagerBtn()
    }

    private fun initPlateManagerBtn() {
        ClickUtils.applySingleDebouncing(binding.textPlateEdit) {
            logd("textPlateEdit onClick", TAG)
           // navigation(ARouterConstants.MyCenterRouter.ROUTER_MY_CAR)
            dismissAllowingStateLoss()
        }
    }

    private fun initList() {
        val plates = vehiclesViewModel.getPlateList()
        binding.list.layoutManager = LinearLayoutManager(context)
        adapter = PlateSelectorAdapter {
            // save Plate
            vehiclesViewModel.selectPlate(it)
            // update UI
            adapter.updateDefaultVin(it.vin)
            // close Dialog
            binding.list.postDelayed({
                dismissAllowingStateLoss()
            }, DISMISS_DELAY_MS)
        }

        binding.list.adapter = adapter
        adapter.updatePlateInfo(plates, vehiclesViewModel.loadDefVin(plates))
        setListHeight(calcListHeight(plates.size))
    }

    private fun calcListHeight(size: Int): Int {
        // offset keep fixed list items big then total height with items
        val offset = 1
        val max = LIST_MAX_DISPLAY
        if (size == 0) {
            return LIST_MIN_DISPLAY
        }
        return if (size >= max) {
            LIST_ITEM_HEIGHT * max + offset
        } else {
            LIST_ITEM_HEIGHT * size + offset
        }
    }

    private fun setListHeight(heightDp: Int) {
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, heightDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        val params: ViewGroup.LayoutParams = binding.list.getLayoutParams()
        params.height = heightPx
        logd("setListHeight heightDp:$heightPx", TAG)
        binding.list.layoutParams = params
    }

    companion object {
        const val TAG = "PlateSelectorBottomSheet"
        const val DISMISS_DELAY_MS = 200L
        const val LIST_MAX_DISPLAY = 5
        const val LIST_MIN_DISPLAY = 0
        const val LIST_ITEM_HEIGHT = 64 //dp
    }
}