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
        val todoHeader: TextView = itemView.findViewById(R.id.todoHeader)
        val todoContainer: LinearLayout = itemView.findViewById(R.id.todoContainer)
        val commentsHeader: TextView = itemView.findViewById(R.id.commentsHeader)
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

        // Initially hidden
        holder.timeSpent.visibility = View.GONE
        holder.todoHeader.visibility = View.GONE
        holder.todoContainer.visibility = View.GONE
        holder.commentsHeader.visibility = View.GONE
        holder.commentsContainer.visibility = View.GONE

        // Add dummy todo items
        holder.todoContainer.removeAllViews()
        post.todoList.forEach {
            val todoItem = TextView(holder.itemView.context)
            todoItem.text = it
            holder.todoContainer.addView(todoItem)
        }

        // Add dummy comments
        holder.commentsContainer.removeAllViews()
        post.comments.forEach {
            val commentItem = TextView(holder.itemView.context)
            commentItem.text = it
            holder.commentsContainer.addView(commentItem)
        }

        // Toggle visibility on click
        holder.itemView.setOnClickListener {
            val visibility = if (holder.timeSpent.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            holder.timeSpent.visibility = visibility
            holder.todoHeader.visibility = visibility
            holder.todoContainer.visibility = visibility
            holder.commentsHeader.visibility = visibility
            holder.commentsContainer.visibility = visibility
        }

        holder.timeSpent.text = post.timeSpent
    }


    override fun getItemCount(): Int = postList.size
}
