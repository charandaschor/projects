package com.example.android.inventory;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private InventoryCursorAdapter mAdapter;
    private static final int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                intent.setData(null);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.inventory_list);
        mAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(mAdapter);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long Id) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Id));
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.delete_all_inventory:
                deleteAllInventory();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAllInventory(){
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Toast.makeText(this, getString(R.string.toast_rows_deleted, rowsDeleted), Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {InventoryEntry._ID, InventoryEntry.COLUMN_NAME, InventoryEntry.COLUMN_QUANTITY,
                                InventoryEntry.COLUMN_PRICE, InventoryEntry.COLUMN_IMAGE};
        switch (id){
            case LOADER_ID: return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
            default: return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
