package com.deigo.faker

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CardViewAdapter(private val context: Context?, private val cardViewItems: Array<CardViewItems>): RecyclerView.Adapter<CardViewAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView? = itemView.findViewById(R.id.imageViewInCard)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_item, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cardViewItems.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {


        val item = cardViewItems[position]
        if (item.imageResourceId != null) {
            holder.imageView?.setImageResource(item.imageResourceId)
        } else {
            holder.imageView?.setBackgroundColor(item.backgroundColor!!)
        }

        holder.imageView?.setOnClickListener {
            Toast.makeText(context, "$item Clicked", Toast.LENGTH_SHORT).show()
            FloatingService.image = BitmapDrawable.createFromPath(R.drawable.black_snow_fall_ss.toString())
        }
    }
}