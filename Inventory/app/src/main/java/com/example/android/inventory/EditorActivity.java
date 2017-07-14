package com.example.android.inventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private ImageView mImageView;
    private EditText mEmailEditText;

    private static final int LOADER_ID = 0;
    private static final int GET_FROM_GALLERY = 1;

    private Uri mUri = null;

    private boolean mImageSelected = false;

    private boolean mInventoryHasChanged;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        LinearLayout orderMoreFromSupplierLayout = (LinearLayout) findViewById(R.id.order_more_from_supplier_layout);
        mUri = getIntent().getData();
        if (mUri == null){
            setTitle(R.string.title_add_inventory_item);
            invalidateOptionsMenu();
            orderMoreFromSupplierLayout.setVisibility(View.INVISIBLE);
        } else {
            setTitle(R.string.title_edit_inventory_item);
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
            orderMoreFromSupplierLayout.setVisibility(View.VISIBLE);
        }

        mNameEditText = (EditText) findViewById(R.id.inventory_item_name_edit_text);
        mPriceEditText = (EditText) findViewById(R.id.inventory_item_price_edit_text);
        mQuantityEditText = (EditText) findViewById(R.id.inventory_item_quantity_edit_text);
        mImageView = (ImageView) findViewById(R.id.editor_image);
        mEmailEditText = (EditText) findViewById(R.id.order_more_from_supplier_email);
        Button quantityIncrease = (Button) findViewById(R.id.quantity_increase);
        Button quantityDecrease = (Button) findViewById(R.id.quantity_decrease);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        quantityIncrease.setOnTouchListener(mTouchListener);
        quantityDecrease.setOnTouchListener(mTouchListener);

        Button addImageButton = (Button) findViewById(R.id.editor_add_image_button);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        if (mUri != null) {
            Cursor cursor = getContentResolver().query(mUri, null, null, null, null);
            if (cursor.moveToFirst()) {
                Button orderMoreButton = (Button) findViewById(R.id.order_more_quantity_button);
                final EditText orderMoreEditText = (EditText) findViewById(R.id.order_more_from_supplier_quantity);
                final String email = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_EMAIL));
                final String name = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME));
                orderMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String orderMessage = "New Order of " + name +
                                ". Quantity: " + orderMoreEditText.getText().toString().trim();
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, email);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "New Order");
                        intent.putExtra(Intent.EXTRA_TEXT, orderMessage);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });
            }
            cursor.close();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        quantityDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                if (quantity > 0){
                    quantity--;
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            }
        });


        quantityIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = 0;
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                    quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                }
                quantity++;
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap image = null;
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK){
            Uri selectedImage = data.getData();
            try {
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            }catch (IOException e){
                Toast.makeText(this, R.string.toast_problem_loading_image, Toast.LENGTH_SHORT).show();
            }
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, 200, 200, false);

        mImageView.setImageBitmap(scaledBitmap);
        mImageSelected = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.editor_save:
                if (mInventoryHasChanged){
                    saveInventory();
                } else {
                    finish();
                }
                return true;
            case R.id.editor_delete:
                if (mUri != null){
                    showDeleteConfirmationDialog();
                }
                return true;
            case android.R.id.home:
                if (!mInventoryHasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveInventory(){
        String name = mNameEditText.getText().toString().trim();
        int quantity;
        if (TextUtils.isEmpty(mQuantityEditText.getText().toString())){
            Toast.makeText(this, R.string.toast_provider_illegal_quantity, Toast.LENGTH_SHORT).show();
            return;
        } else {
            quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        }
        int price = 0;
        if (!TextUtils.isEmpty(mPriceEditText.getText().toString().trim())){
            price = Integer.parseInt(mPriceEditText.getText().toString().trim());
        } else {
            Toast.makeText(this, R.string.toast_no_price, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mImageSelected && mUri == null){
            Toast.makeText(this, R.string.toast_no_image, Toast.LENGTH_SHORT).show();
            return;
        }
        String email = mEmailEditText.getText().toString().trim();

        byte[] compressedImage = null;
        Bitmap image = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, stream);
        compressedImage = stream.toByteArray();

        if (name.equals("")){
            Toast.makeText(this, R.string.toast_provider_no_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity < 0 || mQuantityEditText.getText().toString().trim().equals("")){
            Toast.makeText(this, R.string.toast_provider_illegal_quantity, Toast.LENGTH_SHORT).show();
            return;
        }


        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, name);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryContract.InventoryEntry.COLUMN_EMAIL, email);
        values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, compressedImage);

        if (mUri == null){
            Uri uri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (uri == null){
                Toast.makeText(this, R.string.toast_error_adding_item, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_item_added, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(mUri, values, null, null);
            if (rowsUpdated == 0){
                Toast.makeText(this, R.string.toast_error_updating_inventory, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_inventory_updated, Toast.LENGTH_SHORT).show();

            }
        }
        finish();
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_message_delete_item);
        builder.setPositiveButton(R.string.alert_dialog_button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteInventory();
                finish();
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteInventory(){
        int rowsDeleted = getContentResolver().delete(mUri, null, null);
        if (rowsDeleted == 0){
            Toast.makeText(this, R.string.toast_error_deleting_item, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.toast_item_deleted, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_message_discard_changes);
        builder.setPositiveButton(R.string.alert_dialog_button_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.alert_dialog_button_keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mUri == null){
            MenuItem menuItem = menu.findItem(R.id.editor_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection  = {InventoryContract.InventoryEntry.COLUMN_NAME, InventoryContract.InventoryEntry.COLUMN_PRICE,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY, InventoryContract.InventoryEntry.COLUMN_EMAIL, InventoryContract.InventoryEntry.COLUMN_IMAGE};
        switch (id){
            case LOADER_ID: return new CursorLoader(EditorActivity.this, InventoryContract.InventoryEntry.CONTENT_URI,
                    projection, null, null, null);
            default: return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor cursor = getContentResolver().query(mUri, null, null, null, null);
        if (cursor.moveToNext()) {
            mNameEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME)));
            mPriceEditText.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE))));
            mQuantityEditText.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY))));
            mEmailEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_EMAIL)));
            byte[] image = cursor.getBlob(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE));
            if (image != null) {
                mImageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            }
        }
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("" + 0);
        mEmailEditText.setText("");
        mImageView.setImageBitmap(null);
    }
}
