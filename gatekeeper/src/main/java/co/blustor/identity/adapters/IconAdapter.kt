package co.blustor.identity.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import co.blustor.identity.R
import co.blustor.identity.utils.MyApplication

import kotlinx.android.synthetic.main.grid_icon.view.*

class IconAdapter(private val mContext: Context) : BaseAdapter() {

    override fun getCount(): Int {
        return MyApplication.icons.size()
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: layoutInflater.inflate(R.layout.grid_icon, parent, false)

        view.imageViewIcon.setImageResource(MyApplication.icons.get(position))

        return view
    }
}
