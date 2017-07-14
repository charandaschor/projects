package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "store.db";

    //SQL statement for creating the "store.db" database.
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ( " +
            InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            InventoryContract.InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            InventoryContract.InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
            InventoryContract.InventoryEntry.COLUMN_PRICE + "  INTEGER DEFAULT 0, " +
            InventoryContract.InventoryEntry.COLUMN_EMAIL + " TEXT, " +
            InventoryContract.InventoryEntry.COLUMN_IMAGE + " BLOB );";

    //SQL statement for deleting the "store.db" database.
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + InventoryContract.InventoryEntry.TABLE_NAME;

    public InventoryDbHelper (Context context){ super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
