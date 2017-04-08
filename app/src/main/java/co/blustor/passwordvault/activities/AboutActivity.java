package co.blustor.passwordvault.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.blustor.passwordvault.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle("About");

        // Views

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(new AboutAdapter());
    }

    private class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.AboutViewHolder> {

        @Override
        public AboutAdapter.AboutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
            }
            return new AboutAdapter.AboutViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AboutAdapter.AboutViewHolder holder, int position) {
            if (position == 0) {
                holder.iconImageView.setImageResource(R.drawable.phone);
                holder.titleTextView.setText("+1 312 840 8250");
            } else if (position == 1) {
                holder.iconImageView.setImageResource(R.drawable.email);
                holder.titleTextView.setText("info@blustor.co");
            } else if (position == 2) {
                holder.iconImageView.setImageResource(R.drawable.location);
                holder.titleTextView.setText("401 North Michigan Avenue\nChicago, IL 60611");
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        class AboutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final ImageView iconImageView;
            final TextView titleTextView;

            AboutViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                iconImageView = (ImageView) itemView.findViewById(R.id.imageview_icon);
                titleTextView = (TextView) itemView.findViewById(R.id.textview_title);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (position == 0) {
                    Uri uri = Uri.parse("tel:+13128408250");
                    Intent phoneCall = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(phoneCall);
                } else if (position == 1) {
                    Uri uri = Uri.parse("mailto:info@blustor.co");
                    Intent sendEmail = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(sendEmail);
                } else if (position == 2) {
                    Uri uri = Uri.parse("geo:0,0?q=401+North+Michigan+Avenue,Chicago,IL");
                    Intent openMap = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(openMap);
                }
            }
        }
    }
}
