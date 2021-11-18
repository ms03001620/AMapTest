package com.example.amaptest

import android.R.attr
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import android.R.attr.dependency





class FrontSheetBehavior<V : View>(context: Context, attr: AttributeSet) :
    BottomSheetBehavior<V>(context, attr) {


    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        Log.d("_____", "layoutDependsOn child:$child, dependency:$dependency")
        dependency.z = 0.0f
        return dependency is MaterialButton
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: V,
        dependency: View
    ): Boolean {
        Log.d("_____", "onDependentViewChanged child:$child, dependency:$dependency")



        dependency.bringToFront()

        dependency.getParent().requestLayout();
        dependency.invalidate();

        return false
    }

}