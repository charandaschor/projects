package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventory.R;


public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper mDbHelper;

    //URI Matcher code for the content URI for the inventory table.
    private static final int INVENTORY = 100;

    //URI Matcher code for the content URI for a single inventory item in the inventory table.
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        final int match = sUriMatcher.match(uri);

        switch (match){
            case INVENTORY:
                //For the INVENTORY case, query the database with the given value of projection, selection
                //selectionArgs and sortOrder.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                //For the INVENTORY_ID case, extract the ID from the URI then perform the query on the database.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs =new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("cannot query unknown uri" + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return insertInventoryItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInventoryItem(Uri uri, ContentValues values){

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //variables for checking whether the values object contains legal values before insertion into database.
        boolean insertName = false;
        boolean insertQuantity = false;

        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
        if (name == null){
            throw new IllegalArgumentException("a name is required");
        }

        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        if (quantity == null || quantity < 0){
            throw new IllegalArgumentException("a quantity is required");
        }

        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);


        if (id == -1){
            Toast.makeText(getContext(), R.string.toast_error_adding_item, Toast.LENGTH_SHORT).show();
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                //For the INVENTORY case, call the updateInventory method passing the given params - uri, values, selection & selectionArgs.
                return updateInventory(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                //For the INVENTORY_ID case, extract the ID from the uri and then call the updateInventory Method.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        //If values contains no objects, return 0.
        if (values.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_NAME)){
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
            if (name == null){
                throw new IllegalArgumentException("a name is required");
            }
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_QUANTITY)){
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0){
                throw new IllegalArgumentException("a quantity is required");
            }
        }

        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                //For the INVENTORY case, delete all rows matching the selection and selectionArgs.
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                Log.i("InventoryProvider", "TEST : delete initiated");
                //For the INVENTORY_ID case, delete the row that matches the ID in the Uri.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown uri " + uri + " with match " + match);
        }
    }
}
