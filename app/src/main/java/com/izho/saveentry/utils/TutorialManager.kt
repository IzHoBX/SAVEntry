package com.izho.saveentry.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.izho.saveentry.R
import okhttp3.internal.immutableListOf


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

        val viewPager = popupView.findViewById<ViewPager2>(viewPagerId)
        viewPager.adapter =
            TutorialPagerAdapter(
                activity
            )

        popupView.findViewById<Button>(R.id.next).setOnClickListener {
            if(viewPager.currentItem == viewPager.adapter!!.itemCount-1) {
                Prefs.setHasSeenTutorial(activity, true)
                popupWindow.dismiss()
            } else {
                if ((viewPager.adapter as TutorialPagerAdapter).showHow[viewPager.currentItem+1]) {
                    popupView.findViewById<Button>(R.id.show_me).visibility= View.VISIBLE
                } else {
                    popupView.findViewById<Button>(R.id.show_me).visibility= View.GONE
                }
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

        try {
            //Set the location of the window on the screen
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        } catch (e:WindowManager.BadTokenException) {//activity terminated before the popup is shown
            return
        }
    }

    class TutorialPagerAdapter(activity:AppCompatActivity) : FragmentStateAdapter(activity) {
        val showHow = immutableListOf(true, true, false)

        override fun getItemCount(): Int {
            return showHow.size
        }

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> WidgetTutorialFragment()
                1 -> TileTutorialFragment()
                2 -> AddToFavFragment()
                else -> throw ClassNotFoundException("Unknown type of fragment")
            }
        }
    }

    open class TutorialFragment : Fragment() {
        companion object {
            var showHow = true
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_tutorial, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
        }
    }

    class WidgetTutorialFragment : TutorialFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            view.findViewById<ImageView>(R.id.imageView).setImageDrawable(activity?.getDrawable(
                R.drawable.check_in_home
            ))
            super.onViewCreated(view, savedInstanceState)
        }
    }

    class TileTutorialFragment : TutorialFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            view.findViewById<ImageView>(R.id.imageView).setImageDrawable(activity?.getDrawable(
                R.drawable.check_in_lock
            ))
            super.onViewCreated(view, savedInstanceState)
        }
    }

    class AddToFavFragment : TutorialFragment() {
        companion object {
            var showHow = false
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            view.findViewById<ImageView>(R.id.imageView).setImageDrawable(activity?.getDrawable(
                R.drawable.add_to_fav
            ))
            super.onViewCreated(view, savedInstanceState)
        }
    }
}