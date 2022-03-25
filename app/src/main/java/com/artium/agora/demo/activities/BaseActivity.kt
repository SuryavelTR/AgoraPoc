package com.artium.agora.demo.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.artium.agora.demo.R
import com.artium.agora.demo.databinding.ActivityBaseBinding

abstract class BaseActivity : AppCompatActivity() {

    private companion object {
        private const val APP_PERMISSION_REQUEST = 5500
    }

    private lateinit var baseBinding: ActivityBaseBinding
    protected lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(baseBinding.root)

        (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? NavHostFragment)?.let {
            navigationController = it.navController
            setSupportActionBar(baseBinding.actionBar)
            baseBinding.actionBar.setupWithNavController(
                it.navController,
                AppBarConfiguration(setOf(R.id.introFragment, R.id.preJoinCallFragment))
            )
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        requestCustomPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun requestCustomPermissions(permissions: Array<String>) {
        val permissionsRequired = mutableListOf<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsRequired.add(permission)
            }
        }

        if (permissionsRequired.isNotEmpty()) {
            requestPermissions(
                permissionsRequired.toTypedArray(),
                APP_PERMISSION_REQUEST
            )
        } else {
            onReceivedAllPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == APP_PERMISSION_REQUEST && grantResults.isNotEmpty()) {
            var isGranted = true
            for (grantIndex in grantResults.indices) {
                if (grantResults[grantIndex] == PackageManager.PERMISSION_GRANTED) {
                    isGranted = true
                } else {
                    isGranted = false
                    break
                }
            }

            if (isGranted) {
                onReceivedAllPermissions()
            }
        }
    }

    abstract fun onReceivedAllPermissions()
}