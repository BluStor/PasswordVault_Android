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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        title = "About"

        // Recycler view

        recyclerView.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        recyclerView.adapter = AboutAdapter()
    }

    private inner class AboutAdapter : RecyclerView.Adapter<AboutAdapter.AboutViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_about, parent, false)
            return AboutViewHolder(view)
        }

        override fun onBindViewHolder(holder: AboutViewHolder, position: Int) {
            when (position) {
                0 -> {
                    holder.iconImageView.setImageResource(R.drawable.phone)
                    holder.titleTextView.setText(R.string.about_phone)
                }
                1 -> {
                    holder.iconImageView.setImageResource(R.drawable.email)
                    holder.titleTextView.setText(R.string.about_email)
                }
                2 -> {
                    holder.iconImageView.setImageResource(R.drawable.location)
                    holder.titleTextView.setText(R.string.about_address)
                }
            }
        }

        override fun getItemCount(): Int {
            return 3
        }

        internal inner class AboutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            val iconImageView: ImageView
            val titleTextView: TextView

            init {
                itemView.setOnClickListener(this)

                iconImageView = itemView.findViewById(R.id.imageViewIcon)
                titleTextView = itemView.findViewById(R.id.textview_title)
            }

            override fun onClick(v: View) {
                val position = adapterPosition
                when (position) {
                    0 -> {
                        val uri = Uri.parse("tel:+13128408250")
                        val phoneCall = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(phoneCall)
                    }
                    1 -> {
                        val uri = Uri.parse("mailto:info@blustor.co")
                        val sendEmail = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(sendEmail)
                    }
                    2 -> {
                        val uri = Uri.parse("geo:0,0?q=401+North+Michigan+Avenue,Chicago,IL")
                        val openMap = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(openMap)
                    }
                }
            }
        }
    }
}
