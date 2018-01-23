package co.blustor.identity.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import co.blustor.identity.R

import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    data class AboutItem(val iconResource: Int, val titleResource: Int, val uri: String)

    val items = listOf(
        AboutItem(R.drawable.about_phone, R.string.about_phone, "tel:+13128408250"),
        AboutItem(R.drawable.about_email, R.string.about_email, "mailto:info@blustor.co"),
        AboutItem(R.drawable.about_address, R.string.about_address, "geo:0,0?q=401+North+Michigan+Avenue,Chicago,IL")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        title = "About"

        // Views

        recyclerViewAbout.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerViewAbout.layoutManager = linearLayoutManager

        recyclerViewAbout.adapter = AboutAdapter()
    }

    private inner class AboutAdapter : RecyclerView.Adapter<AboutAdapter.AboutViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_about, parent, false)
            return AboutViewHolder(view)
        }

        override fun onBindViewHolder(holder: AboutViewHolder, position: Int) {
            val item = items[position]
            holder.iconImageView.setImageResource(item.iconResource)
            holder.titleTextView.setText(item.titleResource)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        internal inner class AboutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            val iconImageView = itemView.findViewById(R.id.imageViewIcon) as ImageView
            val titleTextView = itemView.findViewById(R.id.textview_title) as TextView

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View) {
                val item = items[adapterPosition]

                val uri = Uri.parse(item.uri)
                val viewIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(viewIntent)
            }
        }
    }
}
