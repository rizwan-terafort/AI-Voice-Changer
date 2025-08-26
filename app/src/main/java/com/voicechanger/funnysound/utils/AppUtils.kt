package com.voicechanger.funnysound.utils

import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.singular.sdk.Singular
import com.voicechanger.funnysound.VoiceChangerApplication
import com.voicechanger.funnysound.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppUtils {

    fun getMain(activity: FragmentActivity?): MainActivity {
        return activity as MainActivity
    }

    var firebaseAnalytics: FirebaseAnalytics? = null




    fun firebaseUserAction(action: String, activityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Singular.event(action)
            VoiceChangerApplication.context?.let {
                if(NetworkUtils.isOnline(it)){
                    if (FirebaseApp.getApps(it).isEmpty()) {
                        FirebaseApp.initializeApp(it)
                    } else {
                        if (firebaseAnalytics == null) {
                            firebaseAnalytics = Firebase.analytics
                        }
                        firebaseAnalytics?.let { analytics ->
                            analytics.logEvent(action) {
                                param("Screen_Name", activityName)
                            }
                        }
                    }
                }

            }
        }
    }

}