package co.blustor.identity.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.blustor.identity.R
import co.blustor.identity.activities.EditEntryActivity
import co.blustor.identity.utils.MyApplication
import co.blustor.identity.vault.Vault
import co.blustor.identity.vault.VaultEntry
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*

class SearchFragment : Fragment() {

    private val searchResultAdapter = SearchResultAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val linearLayoutManager = LinearLayoutManager(activity)

        recyclerViewSearch.layoutManager = linearLayoutManager
        recyclerViewSearch.adapter = searchResultAdapter
    }

    fun search(query: String) {
        val entries = Vault.instance.findEntriesByTitle(query)
        searchResultAdapter.setResults(entries, query)
    }

    fun show() {
        view?.visibility = View.VISIBLE
    }

    fun hide() {
        view?.visibility = View.INVISIBLE
    }

    private inner class SearchResultAdapter : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {
        private val entryResults = mutableListOf<VaultEntry>()
        private var loweredQuery = ""

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
            return SearchResultViewHolder(view)
        }

        override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
            val entry = entryResults[position]
            val iconId = MyApplication.icons.get(entry.iconId)

            activity?.let {
                val drawable = ContextCompat.getDrawable(it, iconId)
                holder.iconImageView.setImageDrawable(drawable)

                val highlightColor = ContextCompat.getColor(it, R.color.colorPrimary)
                val foregroundColorSpan = ForegroundColorSpan(highlightColor)

                val title = entry.title
                val loweredTitle = title.toLowerCase(Locale.getDefault())

                val titleSpannable = SpannableString(title)
                if (loweredTitle.contains(loweredQuery)) {
                    val start = loweredTitle.indexOf(loweredQuery)
                    val end = start + loweredQuery.length
                    titleSpannable.setSpan(
                        foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                holder.titleTextView.setText(titleSpannable, TextView.BufferType.SPANNABLE)
            }

            val group = Vault.instance.getGroupByUUID(entry.groupUUID)

            group?.let {
                val path = it.path.toMutableList()
                path.add(it.name)

                val stringBuilder = StringBuilder("in ")

                var i = 0
                for (component in path) {
                    stringBuilder.append(component)
                    if (i < path.size - 1) {
                        stringBuilder.append("/")
                    }
                    i += 1
                }

                holder.nameTextView.text = stringBuilder.toString()
            }
        }

        override fun getItemCount(): Int {
            return entryResults.size
        }

        internal fun setResults(entryResults: List<VaultEntry>, loweredQuery: String) {
            this.loweredQuery = loweredQuery

            this.entryResults.clear()
            this.entryResults.addAll(entryResults)

            if (textViewEmpty != null) {
                if (this.entryResults.size > 0) {
                    textViewEmpty.visibility = View.INVISIBLE
                } else {
                    textViewEmpty.visibility = View.VISIBLE
                }
            }

            notifyDataSetChanged()
        }

        internal inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            val iconImageView: ImageView
            val titleTextView: TextView
            val nameTextView: TextView

            init {
                itemView.setOnClickListener(this)

                iconImageView = itemView.findViewById(R.id.imageViewIcon)
                titleTextView = itemView.findViewById(R.id.textview_title)
                nameTextView = itemView.findViewById(R.id.textview_name)
            }

            override fun onClick(v: View) {
                val position = adapterPosition

                val entry = entryResults[position]

                val editEntryActivity = Intent(activity, EditEntryActivity::class.java)
                editEntryActivity.putExtra("uuid", entry.uuid)

                activity?.let {
                    val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        it, iconImageView, "entry"
                    )

                    startActivity(editEntryActivity, activityOptions.toBundle())
                }
            }
        }
    }
}
