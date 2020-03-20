package com.internshala.echo.Fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.internshala.echo.R

/**
 * A simple [Fragment] subclass.
 *
 */
class AboutUsFragment : Fragment() {

    var detailsText: TextView? = null
    var appVersion: TextView? = null
    var devEmail: TextView? = null
    var myActivity: Activity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "About Us"
        if (container != null) {
            container.removeAllViews()
        }
        val view = inflater.inflate(R.layout.fragment_about_us, container, false)

        detailsText = view?.findViewById(R.id.detailsText)
        appVersion = view?.findViewById(R.id.appVersion)
        devEmail = view?.findViewById(R.id.devEmail)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        detailsText?.setText("An enthusiastic mobile app developer with a zeal to explore!")
        appVersion?.setText("App Version: 1.0")
        devEmail?.setText("Reach out at: getoxdev@gmail.com")
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }
}
