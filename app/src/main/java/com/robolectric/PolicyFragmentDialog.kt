package com.robolectric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ChargingPolicyDialogBinding


class PolicyFragmentDialog : AppCompatDialogFragment() {

    private val vehiclesViewModel by lazy {
        ViewModelProvider(requireActivity(), ViewModelFactory())[VehiclesViewModel::class.java]
    }

    private lateinit var binding: ChargingPolicyDialogBinding

    //override fun getTheme() = R.style.ChargingWidthHeight
    //override fun getTheme() = android.R.style.Theme_NoTitleBar_Fullscreen

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.charging_policy_dialog, container, false
        )
        binding.textHello.text = arguments?.getString("title") ?: ""
        return binding.root
    }

    companion object {
        fun newInstance(title: String): PolicyFragmentDialog {
            val fragment = PolicyFragmentDialog()
            fragment.arguments = Bundle().also {
                it.putString("title", title)
            }
            return fragment
        }
        const val TAG = "PolicyFragmentDialog"
    }
}