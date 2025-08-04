package com.mobicom.mco.pokus.home

import android.app.AlertDialog
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.mco.pokus.MainActivity
import com.mobicom.mco.pokus.R

class PostAdapter(private var postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val date: TextView = view.findViewById(R.id.date)
        val title: TextView = view.findViewById(R.id.postTitle)
        val content: TextView = view.findViewById(R.id.postDesc)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val timeLabel: TextView = view.findViewById(R.id.timeLabel)
        val time: TextView = view.findViewById(R.id.timeSpent)
        val todoLabel: TextView = view.findViewById(R.id.todoLabel)
        val todo: TextView = view.findViewById(R.id.todoList)
        val comments: TextView = view.findViewById(R.id.comments)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val commentButton: ImageView = view.findViewById(R.id.commentButton)
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
        holder.timeLabel.text = "Time:"
        holder.time.text = post.timeSpent.removePrefix("Time: ").trim()
        holder.todoLabel.text = "To Do:"
        holder.todo.text = post.todoList.joinToString("\n")

        // Format comments with usernames
//        val commentText = post.comments.mapIndexed { i, comment ->
//            val username = post.commentUsernames.getOrNull(i) ?: "user"
//            "$username: $comment"
//        }.joinToString("\n")
//        holder.comments.text = commentText

        // Like button toggle
        holder.likeButton.setImageResource(
            if (post.isLiked) R.drawable.liked else R.drawable.like
        )

        holder.likeButton.setOnClickListener {
            post.isLiked = !post.isLiked
            notifyItemChanged(position)
        }

        // Comment input dialog
//        holder.commentButton.setOnClickListener {
//            val context = holder.itemView.context
//            val input = EditText(context)
//            input.inputType = InputType.TYPE_CLASS_TEXT
//            input.hint = "Type your comment..."
//
//            AlertDialog.Builder(context)
//                .setTitle("Add Comment")
//                .setView(input)
//                .setPositiveButton("Post") { _, _ ->
//                    val commentText = input.text.toString().trim()
//                    if (commentText.isNotEmpty()) {
//                        post.comments.add(commentText)
//                        post.commentUsernames.add(MainActivity.currentUsername)
//                        notifyItemChanged(position)
//                    }
//                }
//                .setNegativeButton("Cancel", null)
//                .show()
//        }
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPostList: List<Post>) {
        postList = newPostList
        notifyDataSetChanged()
    }

}
