package com.b_lam.resplash.util

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.b_lam.resplash.R

const val EXTRA_SHARED_ELEMENT_TRANSITION_SUPPORT = "shared_element_transition_support"

// contentId should be the transition name for any cc card image
const val EXTRA_CARD_ART_TRANSITION_NAME = "extra_offer_card_art_transition_name"

fun Window.applyArcMotionSharedEnterTransition() {
    sharedElementEnterTransition = TransitionInflater.from(context)
        .inflateTransition(R.transition.arc_motion_transition)
}

fun Intent.applySharedElementExtras(sharedElement: ImageView) {
    putExtra(EXTRA_CARD_ART_TRANSITION_NAME, ViewCompat.getTransitionName(sharedElement))
}

fun View.getSceneTransitionAnimationBundleForSharedElement(): Bundle? {
    val parent = this.parent
    val transitionName = ViewCompat.getTransitionName(this)
    return if (parent is View && transitionName != null) {
        this.getSceneTransitionAnimationBundleForSharedElement(
            transitionName,
            this.isFullyVisibleInView(parent)
        )
    } else {
        null
    }
}

fun View.getSceneTransitionAnimationBundleForSharedElement(
    transitionName: String,
    isVisible: Boolean
): Bundle? {
    return if (isVisible) {
        getActivity()?.let {
            ActivityOptionsCompat
                .makeSceneTransitionAnimation(it, this, transitionName).toBundle()
        }
    } else {
        null
    }
}

fun View.scheduleStartPostponedTransition() {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            viewTreeObserver.removeOnPreDrawListener(this)
            return true
        }
    })
}
