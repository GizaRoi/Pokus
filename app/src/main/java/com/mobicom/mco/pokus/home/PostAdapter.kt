package com.mobicom.mco.pokus.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.R

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val date: TextView = view.findViewById(R.id.date)
        val title: TextView = view.findViewById(R.id.postTitle)
        val content: TextView = view.findViewById(R.id.postDesc)
        val image: ImageView = view.findViewById(R.id.postImage)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val time: TextView = view.findViewById(R.id.timeSpent)
        val todo: TextView = view.findViewById(R.id.todoList)
        val comments: TextView = view.findViewById(R.id.comments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itempost, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.name.text = post.name
        holder.date.text = post.date
        holder.title.text = post.title
        holder.content.text = post.content
        holder.image.setImageResource(post.imageResId)
        holder.profileImage.setImageResource(post.imageResId)
        holder.time.text = post.timeSpent
        holder.todo.text = post.todoList.joinToString("\n")
        holder.comments.text = post.comments.joinToString("\n")
    }

    override fun getItemCount(): Int = postList.size
}
