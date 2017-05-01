package co.blustor.pwv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import co.blustor.pwv.R;
import co.blustor.pwv.adapters.IconAdapter;

public class IconPickerActivity extends LockingActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iconpicker);

        setTitle("Select icon");

        GridView mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(new IconAdapter(this));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra("icon", position);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
