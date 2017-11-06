package co.blustor.identity.fragments;

import android.content.Intent;
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
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.blustor.identity.R;
import co.blustor.identity.activities.EditEntryActivity;
import co.blustor.identity.utils.MyApplication;
import co.blustor.identity.vault.Vault;
import co.blustor.identity.vault.VaultEntry;
import co.blustor.identity.vault.VaultGroup;

public class SearchFragment extends Fragment {
    private final SearchResultAdapter mSearchResultAdapter = new SearchResultAdapter();
    @Nullable
    private TextView mEmptyTextView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Views

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mSearchResultAdapter);

        mEmptyTextView = view.findViewById(R.id.textview_empty);

        return view;
    }

    public void search(String query) {
        Vault vault = Vault.getInstance();
        mSearchResultAdapter.setResults(vault.findEntriesByTitle(query), query);
    }

    public void show() {
        View view = getView();
        assert view != null;

        view.setVisibility(View.VISIBLE);
    }

    public void hide() {
        View view = getView();
        assert view != null;

        view.setVisibility(View.INVISIBLE);
    }

    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
        private final List<VaultEntry> mEntryResults = new ArrayList<>();
        private String mLoweredQuery = "";

        @Override
        public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
            return new SearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SearchResultViewHolder holder, int position) {
            VaultEntry entry = mEntryResults.get(position);

            Drawable drawable = ContextCompat.getDrawable(getActivity(), MyApplication.getIcons().get(entry.getIconId()));
            holder.iconImageView.setImageDrawable(drawable);

            int highlightColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(highlightColor);

            String title = entry.getTitle();
            String loweredTitle = title.toLowerCase(Locale.getDefault());

            Spannable titleSpannable = new SpannableString(title);
            if (loweredTitle.contains(mLoweredQuery)) {
                int start = loweredTitle.indexOf(mLoweredQuery);
                int end = start + mLoweredQuery.length();
                titleSpannable.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            holder.titleTextView.setText(titleSpannable, TextView.BufferType.SPANNABLE);

            Vault vault = Vault.getInstance();
            VaultGroup group = vault.getGroupByUUID(entry.getGroupUUID());

            if (group != null) {
                List<String> path = group.getPath();
                path.add(group.getName());

                StringBuilder stringBuilder = new StringBuilder("in ");

                int i = 0;
                for (String component : path) {
                    stringBuilder.append(component);
                    if (i < path.size() - 1) {
                        stringBuilder.append("/");
                    }
                    i += 1;
                }

                holder.nameTextView.setText(stringBuilder.toString());
            }
        }

        @Override
        public int getItemCount() {
            return mEntryResults.size();
        }

        void setResults(List<VaultEntry> entryResults, String loweredQuery) {
            mLoweredQuery = loweredQuery;

            mEntryResults.clear();
            mEntryResults.addAll(entryResults);

            if (mEmptyTextView != null) {
                if (mEntryResults.size() > 0) {
                    mEmptyTextView.setVisibility(View.INVISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                }
            }

            notifyDataSetChanged();
        }

        class SearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final ImageView iconImageView;
            final TextView titleTextView;
            final TextView nameTextView;

            SearchResultViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                iconImageView = itemView.findViewById(R.id.imageview_icon);
                titleTextView = itemView.findViewById(R.id.textview_title);
                nameTextView = itemView.findViewById(R.id.textview_name);
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
