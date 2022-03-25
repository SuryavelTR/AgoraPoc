package com.artium.agora.demo.activities

import com.artium.agora.demo.fragments.IntroFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {

    override fun onReceivedAllPermissions() {
        navigationController.navigate(IntroFragmentDirections.introToPreJoinFragment())
    }
}