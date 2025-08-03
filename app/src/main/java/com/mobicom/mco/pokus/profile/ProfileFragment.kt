package com.mobicom.mco.pokus.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R
import android.content.Intent

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access values directly from MainActivity companion object
        view.findViewById<TextView>(R.id.username).text = MainActivity.currentUsername
        view.findViewById<TextView>(R.id.bio).text = MainActivity.currentBio
        view.findViewById<TextView>(R.id.link).text = MainActivity.currentLink

        view.findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }


}
