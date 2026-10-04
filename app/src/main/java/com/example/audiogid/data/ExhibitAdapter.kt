package com.example.audiogid.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.audiogid.R

class ExhibitAdapter(
    exhibits: List<Exhibit>,
    private val onDownloadClick: (Exhibit) -> Unit
) : RecyclerView.Adapter<ExhibitAdapter.ExhibitViewHolder>() {

    private var exhibits: List<Exhibit> = exhibits

    class ExhibitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageExhibit)
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textAuthor: TextView = view.findViewById(R.id.textAuthor)
        val buttonDownload: Button = view.findViewById(R.id.buttonDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExhibitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exhibit, parent, false)
        return ExhibitViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExhibitViewHolder, position: Int) {
        val exhibit = exhibits[position]
        holder.textTitle.text = exhibit.title
        holder.textAuthor.text = exhibit.author

        when (exhibit.title){
            "Звёздная ночь" -> {
                holder.imageView.setImageResource(R.drawable.starry_night)
            }
            "Крик" -> {
                holder.imageView.setImageResource(R.drawable.scream)
            }
            "Мона Лиза" -> {
                holder.imageView.setImageResource(R.drawable.mona_liza)
            }
            "Девушка с жемчужной серёжкой" -> {
                holder.imageView.setImageResource(R.drawable.girl_pearl_earring)
            }
            "Герника" -> {
                holder.imageView.setImageResource(R.drawable.gernika)
            }

        }

        if (exhibit.localPath != null) {
            holder.buttonDownload.text = "Открыть"
        } else {
            holder.buttonDownload.text = "Скачать"
        }
        holder.buttonDownload.setOnClickListener { onDownloadClick(exhibit) }
    }

    override fun getItemCount(): Int = exhibits.size

    fun submitList(newExhibits: List<Exhibit>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = exhibits.size
            override fun getNewListSize(): Int = newExhibits.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean =
                exhibits[oldPos].id == newExhibits[newPos].id

            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean =
                exhibits[oldPos] == newExhibits[newPos]
        })
        exhibits = newExhibits
        diff.dispatchUpdatesTo(this)
    }
}
