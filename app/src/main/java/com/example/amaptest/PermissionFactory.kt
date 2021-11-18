package com.example.amaptest

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class PermissionFactory {

    companion object {
        fun createPermissionLauncher(fragment: ComponentActivity, successLambda: () -> Unit): ActivityResultLauncher<Array<String>> {
            return fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
                if (allGrants.values.all { it }) {
                    successLambda()
                } else {
                    with(allGrants.keys.toString() + allGrants.values.toString()) {
                        Toast.makeText(fragment, this, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}