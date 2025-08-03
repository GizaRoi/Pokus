package com.mobicom.mco.pokus.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.R

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val date: TextView = itemView.findViewById(R.id.date)
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
        val postContent: TextView = itemView.findViewById(R.id.postContent)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val timeSpent: TextView = itemView.findViewById(R.id.timeSpent)
        val todoContainer: LinearLayout = itemView.findViewById(R.id.todoContainer)
        val commentsContainer: LinearLayout = itemView.findViewById(R.id.commentsContainer)
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
        holder.postTitle.text = post.title
        holder.postContent.text = post.content
        holder.postImage.setImageResource(post.imageResId)
        holder.timeSpent.text = post.timeSpent

        // To-Do List
        holder.todoContainer.removeAllViews()
        post.todoList.forEach {
            val item = TextView(holder.itemView.context)
            item.text = it
            item.textSize = 14f
            item.setTextColor(Color.parseColor("#555555"))
            holder.todoContainer.addView(item)
        }

        // Comments
        holder.commentsContainer.removeAllViews()
        post.comments.forEach {
            val comment = TextView(holder.itemView.context)
            comment.text = it
            comment.textSize = 13f
            comment.setTextColor(Color.DKGRAY)
            holder.commentsContainer.addView(comment)
        }
    }

    override fun getItemCount(): Int = postList.size
}
