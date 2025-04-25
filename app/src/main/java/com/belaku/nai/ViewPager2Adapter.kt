package com.belaku.nai

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.belaku.nai.R


internal class ViewPager2Adapter // Constructor of our ViewPager2Adapter class
    (private val ctx: Context, txContents: List<String>) :
    RecyclerView.Adapter<ViewPager2Adapter.ViewHolder>() {
    private val txContents = txContents

    // Array of images
    // Adding images from drawable folder



    // This method returns our layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(ctx).inflate(R.layout.images_holder, parent, false)
        return ViewHolder(view)
    }

    // This method binds the screen with the view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // This will set the images in imageview

        txIndicator1 = holder.txIndicator
        holder.txDesc.setMovementMethod(ScrollingMovementMethod())
        holder.txDesc.setText(txContents[position])
        holder.txDesc.setTextColor(MainActivity.clr1)
        holder.txIndicator.setTextColor(MainActivity.clr1)

        if(position == 0)
        holder.txIndicator.setText(" ◦ • • ")
        else if(position == 1)
            holder.txIndicator.setText(" • ◦ • ")
        else if(position == 2)
            holder.txIndicator.setText(" • • ◦ ")
    }

    // This Method returns the size of the Array
    override fun getItemCount(): Int {
        return txContents.size
    }

    // The ViewHolder class holds the view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txDesc: TextView = itemView.findViewById<TextView>(R.id.tx_holder)
        var txIndicator: TextView = itemView.findViewById(R.id.tx_indicator)
    }

    companion object {
        lateinit var txIndicator1: TextView
    }
}
