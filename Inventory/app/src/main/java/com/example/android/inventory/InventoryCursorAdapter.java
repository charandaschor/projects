package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;


public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.inventory_item_image);
        TextView nameView = (TextView) view.findViewById(R.id.inventory_item_name);
        TextView priceView = (TextView) view.findViewById(R.id.inventory_item_price);
        final TextView quantityView = (TextView) view.findViewById(R.id.inventory_item_quantity);

        byte[] image = cursor.getBlob(cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE));
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
        nameView.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_NAME)));
        priceView.setText("$" + cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE)));
        quantityView.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY)));

        final int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));
        Button saleButton = (Button) view.findViewById(R.id.inventory_sale_button);
        final Uri currentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int productQuantity = quantity;
                if (productQuantity > 0) {
                    productQuantity--;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_QUANTITY, productQuantity);

                    context.getContentResolver().update(currentUri, values, null, null);
                    context.getContentResolver().notifyChange(currentUri, null);
                }
            }
        });
    }
}
