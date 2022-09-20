package com.example.amaptest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.example.amaptest.databinding.ActivityFragmentsBinding

class FragmentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFragmentsBinding
    private var fragment: FragmentLifeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fragments)
        binding.btnFragmentAdd.setOnClickListener {
            fragment = createFragment()
            fragment?.let {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, it)
                    .commitNow()
            }
        }
        binding.btnFragmentRemove.setOnClickListener {
            fragment?.let {
                supportFragmentManager.beginTransaction().remove(it).commitNow()
            }
        }
    }

    private fun createFragment(): FragmentLifeFragment {
        return FragmentLifeFragment().also {
            it.arguments = bundleOf("id" to 100)
        }
    }
}