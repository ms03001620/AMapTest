package com.example.amaptest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.amaptest.databinding.FragmentLifeBinding

class FragmentLifeFragment : Fragment() {
    private lateinit var binding: FragmentLifeBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: $context")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: $context")
        printArguments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_life, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    fun printArguments() {
        arguments?.let {
            Log.d(TAG, "arguments :${it.get("id")}")
        } ?: run {
            Log.d(TAG, "arguments null")
        }
    }

    companion object {
        const val TAG = "FragmentLifeFragment"
    }

}