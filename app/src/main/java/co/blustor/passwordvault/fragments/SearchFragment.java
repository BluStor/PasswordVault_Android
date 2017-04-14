package co.blustor.passwordvault.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.activities.EditEntryActivity;
import co.blustor.passwordvault.activities.GroupActivity;
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
        if (query.isEmpty()) {
            mEmptyTextView.setText("Enter a group name or entry title.");
        } else {
            mEmptyTextView.setText("No results.");
        }

        Vault vault = Vault.getInstance();
        mSearchResultAdapter.setResults(vault.findGroupsByName(query), vault.findEntriesByTitle(query));
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
        private List<VaultGroup> mGroupResults = new ArrayList<>();
        private List<VaultEntry> mEntryResults = new ArrayList<>();

        @Override
        public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
            }
            return new SearchResultViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position < mGroupResults.size()) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public void onBindViewHolder(SearchResultViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                VaultGroup group = mGroupResults.get(position);

                holder.subIconImageView.setImageResource(MyApplication.getIcons().get(group.getIconId()));
                holder.titleTextView.setText(group.getName());
            } else {
                VaultEntry entry = mEntryResults.get(position - mGroupResults.size());

                Drawable drawable = ContextCompat.getDrawable(getActivity(), MyApplication.getIcons().get(entry.getIconId()));

                holder.iconImageView.setImageDrawable(drawable);
                holder.titleTextView.setText(entry.getTitle());
            }
        }

        @Override
        public int getItemCount() {
            return mGroupResults.size() + mEntryResults.size();
        }

        void setResults(List<VaultGroup> groupResults, List<VaultEntry> entryResults) {
            mGroupResults.clear();
            mGroupResults.addAll(groupResults);

            mEntryResults.clear();
            mEntryResults.addAll(entryResults);

            if (mGroupResults.size() > 0 || mEntryResults.size() > 0) {
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

            SearchResultViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                iconImageView = (ImageView) itemView.findViewById(R.id.imageview_icon);
                subIconImageView = (ImageView) itemView.findViewById(R.id.imageview_subicon);
                titleTextView = (TextView) itemView.findViewById(R.id.textview_title);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();

                int itemViewType = getItemViewType();
                if (itemViewType == 0) {
                    VaultGroup group = mGroupResults.get(position);

                    Intent groupActivity = new Intent(getActivity(), GroupActivity.class);
                    groupActivity.putExtra("uuid", group.getUUID());

                    startActivity(groupActivity);

                } else {
                    VaultEntry entry = mEntryResults.get(position - mGroupResults.size());

                    Intent editEntryActivity = new Intent(getActivity(), EditEntryActivity.class);
                    editEntryActivity.putExtra("uuid", entry.getUUID());

                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), iconImageView, "entry");
                    startActivity(editEntryActivity, activityOptions.toBundle());
                }
            }
        }
    }
}
