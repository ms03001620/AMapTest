package com.example.amaptest.pager

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.amaptest.R

class LiveFragment : Fragment() {

    lateinit var root: View

    lateinit var arg: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arg = arguments?.getString("key", "null") ?: "nil"
        Log.d("LiveFragment,ViewPager2", "LiveFragment onCreateView  $arg")
        root = inflater.inflate(R.layout.fragment_main, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root.findViewById<TextView>(R.id.text_status).text = "status:$arg"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LiveFragment,ViewPager2", "LiveFragment onDestroyView :$arg")
    }


}
