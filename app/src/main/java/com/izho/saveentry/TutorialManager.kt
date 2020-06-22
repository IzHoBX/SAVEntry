package com.izho.saveentry

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2


const val TUTORIAL_POPUP_WIDTH = 900
const val TUTORIAL_POPUP_HEIGHT = 1855

class TutorialManager(val activity: AppCompatActivity) {
    fun showTutorial(viewPagerId:Int) {
        val popupView: View = activity.layoutInflater.inflate(R.layout.tutorial_popup, null)

        val width = TUTORIAL_POPUP_WIDTH
        val height = TUTORIAL_POPUP_HEIGHT

        //Make Inactive Items Outside Of PopupWindow
        val focusable = true

        //Create a window with our parameters
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        //Set the location of the window on the screen
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

        // to stop prevent dismissal due to touch outside window
        popupWindow.setTouchInterceptor(OnTouchListener { view, motionEvent ->
            if (motionEvent.x < 0 || motionEvent.x > view.width) return@OnTouchListener true
            if (motionEvent.y < 0 || motionEvent.y > view.height) true else false
        })

        val viewPager = popupView.findViewById<ViewPager2>(viewPagerId)
        viewPager.isUserInputEnabled = false
        viewPager.adapter = TutorialPagerAdapter(activity)

        popupView.findViewById<Button>(R.id.next).setOnClickListener {
            if(viewPager.currentItem == viewPager.adapter!!.itemCount-1) {
                Prefs.setHasSeenTutorial(activity, true)
                popupWindow.dismiss()
            } else {
                if (viewPager.currentItem == viewPager.adapter!!.itemCount-2) {
                    (it as Button).setText("Done")
                }
                viewPager.setCurrentItem(viewPager.currentItem+1, true)
            }
        }

        popupView.findViewById<Button>(R.id.show_me).setOnClickListener {
            val url:String
            url = when(viewPager.currentItem) {
                0 -> "https://www.youtube.com/watch?v=LWgY2CLdy6s"
                else -> "https://www.youtube.com/watch?v=FiprOQGPlXw"
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(activity, i, null)
        }

    }

    class TutorialPagerAdapter(activity:AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> WidgetTutorialFragment()
                1 -> TileTutorialFragment()
                else -> throw ClassNotFoundException("Unknown type of fragment")
            }
        }
    }

    open class TutorialFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_tutorial, container, false)
        }
    }

    class WidgetTutorialFragment : TutorialFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            view.findViewById<ImageView>(R.id.imageView).setImageDrawable(activity?.getDrawable(R.drawable.check_in_home))
            super.onViewCreated(view, savedInstanceState)
        }
    }

    class TileTutorialFragment : TutorialFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            view.findViewById<ImageView>(R.id.imageView).setImageDrawable(activity?.getDrawable(R.drawable.check_in_lock))
            super.onViewCreated(view, savedInstanceState)
        }
    }
}