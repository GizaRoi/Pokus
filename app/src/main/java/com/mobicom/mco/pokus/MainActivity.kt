package com.mobicom.mco.pokus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobicom.mco.pokus.databinding.ActivityMainBinding
import com.mobicom.mco.pokus.home.HomeFragment
import com.mobicom.mco.pokus.search.SearchFragment
import com.mobicom.mco.pokus.todo.TodoFragment
import com.mobicom.mco.pokus.sessions.SessionsFragment
import com.mobicom.mco.pokus.profile.ProfileFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Default fragment* to change later with log in
        loadFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_todo -> loadFragment(TodoFragment())
                R.id.nav_sessions -> loadFragment(SessionsFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())

            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
