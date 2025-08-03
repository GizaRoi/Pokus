package com.mobicom.mco.pokus.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R

class SearchFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var cancelButton: TextView
    private lateinit var searchResults: RecyclerView
    private lateinit var adapter: UserSearchAdapter

    private val allUsernames = MainActivity.currentUsernames

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_search, container, false)

        searchInput = view.findViewById(R.id.searchInput)
        cancelButton = view.findViewById(R.id.cancelButton)
        searchResults = view.findViewById(R.id.searchResults)

        adapter = UserSearchAdapter(allUsernames)
        searchResults.layoutManager = LinearLayoutManager(requireContext())
        searchResults.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filtered = allUsernames.filter { it.lowercase().contains(query) }
                adapter = UserSearchAdapter(filtered)
                searchResults.adapter = adapter
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cancelButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }
}
