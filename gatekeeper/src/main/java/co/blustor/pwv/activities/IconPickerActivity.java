package co.blustor.pwv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.GridView;

import co.blustor.pwv.R;
import co.blustor.pwv.adapters.IconAdapter;

public class IconPickerActivity extends LockingActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iconpicker);

        setTitle("Select icon");

        GridView mGridView = findViewById(R.id.gridview);
        mGridView.setAdapter(new IconAdapter(this));
        mGridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent data = new Intent();
            data.putExtra("icon", position);
            setResult(RESULT_OK, data);
            finish();
        });
    }
}
