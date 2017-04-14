package co.blustor.passwordvault.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.activities.EditEntryActivity;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultEntry;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.utils.MyApplication;

public class SearchFragment extends Fragment {
    private SearchResultAdapter mSearchResultAdapter = new SearchResultAdapter();
    private TextView mEmptyTextView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Views

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mSearchResultAdapter);

        mEmptyTextView = (TextView) view.findViewById(R.id.textview_empty);

        return view;
    }

    public void search(String query) {
        Vault vault = Vault.getInstance();

        if (query.isEmpty()) {
            mEmptyTextView.setText("Enter a group name or entry title.");
        } else {
            mEmptyTextView.setText("No results.");
        }

        mSearchResultAdapter.setResults(vault.findEntriesByTitle(query, true), query);
    }

    public void show() {
        View view = getView();
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        View view = getView();
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
        private String mLoweredQuery = "";
        private List<VaultEntry> mEntryResults = new ArrayList<>();

        @Override
        public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
            return new SearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SearchResultViewHolder holder, int position) {
            Vault vault = Vault.getInstance();

            VaultEntry entry = mEntryResults.get(position);
            VaultGroup group = vault.getGroupByUUID(entry.getGroupUUID());

            Drawable drawable = ContextCompat.getDrawable(getActivity(), MyApplication.getIcons().get(entry.getIconId()));
            holder.iconImageView.setImageDrawable(drawable);

            int highlightColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(highlightColor);

            String title = entry.getTitle();
            String loweredTitle = title.toLowerCase();

            Spannable titleSpannable = new SpannableString(title);
            if (loweredTitle.contains(mLoweredQuery)) {
                int start = loweredTitle.indexOf(mLoweredQuery);
                int end = start + mLoweredQuery.length();
                titleSpannable.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            holder.titleTextView.setText(titleSpannable, TextView.BufferType.SPANNABLE);

            List<String> path = group.getPath();
            path.add(group.getName());

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append("in ");

            for (String component : path) {
                String loweredComponent = component.toLowerCase();

                if (loweredComponent.contains(mLoweredQuery)) {
                    int start = spannableStringBuilder.length() + loweredComponent.indexOf(mLoweredQuery);
                    int end = start + mLoweredQuery.length();

                    spannableStringBuilder.append(component);
                    spannableStringBuilder.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                } else {
                    spannableStringBuilder.append(component);
                }
                
                spannableStringBuilder.append("/");
            }

            spannableStringBuilder.delete(spannableStringBuilder.length() - 1, spannableStringBuilder.length());

            holder.nameTextView.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
        }

        @Override
        public int getItemCount() {
            return mEntryResults.size();
        }

        void setResults(List<VaultEntry> entryResults, String loweredQuery) {
            mLoweredQuery = loweredQuery;

            mEntryResults.clear();
            mEntryResults.addAll(entryResults);

            if (mEntryResults.size() > 0) {
                mEmptyTextView.setVisibility(View.INVISIBLE);
            } else {
                mEmptyTextView.setVisibility(View.VISIBLE);
            }

            notifyDataSetChanged();
        }

        class SearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final ImageView iconImageView;
            final ImageView subIconImageView;
            final TextView titleTextView;
            final TextView nameTextView;

            SearchResultViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                iconImageView = (ImageView) itemView.findViewById(R.id.imageview_icon);
                subIconImageView = (ImageView) itemView.findViewById(R.id.imageview_subicon);
                titleTextView = (TextView) itemView.findViewById(R.id.textview_title);
                nameTextView = (TextView) itemView.findViewById(R.id.textview_name);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();

                VaultEntry entry = mEntryResults.get(position);

                Intent editEntryActivity = new Intent(getActivity(), EditEntryActivity.class);
                editEntryActivity.putExtra("uuid", entry.getUUID());

                ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), iconImageView, "entry");
                startActivity(editEntryActivity, activityOptions.toBundle());
            }
        }
    }
}
