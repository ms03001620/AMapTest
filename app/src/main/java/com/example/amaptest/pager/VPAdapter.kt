package com.example.amaptest.pager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.amaptest.R

class VPAdapter(val act: PagerActivity) : RecyclerView.Adapter<VPAdapter.BaseViewHolder?>() {
    val dataArray = mutableListOf<String>()
    val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.vp_item_layout, parent, false)
        return BaseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        //Log.d("ViewPager2", "onBindViewHolder ${position}")
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        Log.d("ViewPager2", "remove ${holder.layoutPosition}")

        fragmentMap.remove(holder.layoutPosition)?.apply {
            act.supportFragmentManager.beginTransaction().remove(this).commitNow()
        } ?: run {
            Log.d("ViewPager2", "not found fragment: ${holder.layoutPosition}")
        }
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder) {
        super.onViewAttachedToWindow(holder)
        Log.d("ViewPager2", "add ${holder.layoutPosition}")

        LiveFragment().apply {
            this.arguments = bundleOf().also { bundle: Bundle ->
                bundle.putString("key", holder.layoutPosition.toString())
            }
            fragmentMap[holder.layoutPosition] = this
            val id = View.generateViewId()
            holder.itemView.id = id
            act.supportFragmentManager.beginTransaction().replace(id, this).commitNow()
        }
    }

    //TODO leak release
    fun clear() {
        Log.d("ViewPager2", "clear")
        fragmentMap.clear()
    }

    fun setData(data: MutableList<String>){
        dataArray.addAll(data)
    }

    inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val textName = itemView.findViewById<TextView>(R.id.text_name)
    }
}
