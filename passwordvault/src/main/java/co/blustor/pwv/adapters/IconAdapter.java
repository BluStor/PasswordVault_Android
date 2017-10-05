package co.blustor.pwv.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import co.blustor.pwv.R;
import co.blustor.pwv.utils.MyApplication;

public class IconAdapter extends BaseAdapter {
    private final Context mContext;

    public IconAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 69;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater != null) {
            View view;
            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.grid_icon, parent, false);
            } else {
                view = convertView;
            }

            ImageView iconImageView = view.findViewById(R.id.imageview_icon);
            iconImageView.setImageResource(MyApplication.getIcons().get(position));

            return view;
        }

        return null;
    }
}
