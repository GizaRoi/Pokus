package com.mobicom.mco.pokus.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.R
import android.content.Intent



class UserSearchAdapter(private val usernames: List<String>) :
    RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val username: TextView = view.findViewById(R.id.username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemuserresult, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val username = usernames[position]
        holder.username.text = username
        holder.userImage.setImageResource(R.drawable.profile)

        // Navigate to profile on click
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UserProfile::class.java)
            intent.putExtra("USERNAME", username)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = usernames.size
}
