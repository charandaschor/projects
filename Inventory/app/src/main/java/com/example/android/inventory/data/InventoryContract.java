package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns{

        //Content URI for the "inventory" table.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //The MIME type for a list of inventory items.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //The MIME type for a single inventory item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        //The name of the database table.
        public static final String TABLE_NAME = "inventory";

        //The names of the columns in the database table.
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_PRICE = "Price";
        public static final String COLUMN_IMAGE = "Image";
        public static final String COLUMN_EMAIL = "Email";
    }
}
