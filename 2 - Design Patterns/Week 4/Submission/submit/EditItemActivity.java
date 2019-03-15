package com.example.sharingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Editing a pre-existing item consists of deleting the old item and adding a new item with the old
 * item's id.
 * Note: invisible EditText is used to setError for status. For whatever reason we cannot .setError to
 * the status Switch so instead an error is set to an "invisible" EditText.
 */
public class EditItemActivity extends AppCompatActivity{

    private ItemList item_list = new ItemList();
    private Item item;
    private Context context;

    private ContactList contact_list = new ContactList();

    private Bitmap image;
    private int REQUEST_CODE = 1;
    private ImageView photo;

    private EditText title;
    private EditText maker;
    private EditText description;
    private EditText length;
    private EditText width;
    private EditText height;
    private Spinner borrower_spinner;
    private TextView  borrower_tv;
    private Switch status;
    private EditText invisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        title = (EditText) findViewById(R.id.title);
        maker = (EditText) findViewById(R.id.maker);
        description = (EditText) findViewById(R.id.description);
        length = (EditText) findViewById(R.id.length);
        width = (EditText) findViewById(R.id.width);
        height = (EditText) findViewById(R.id.height);
        borrower_spinner = (Spinner) findViewById(R.id.borrower_spinner);
        borrower_tv = (TextView) findViewById(R.id.borrower_tv);
        photo = (ImageView) findViewById(R.id.image_view);
        status = (Switch) findViewById(R.id.available_switch);
        invisible = (EditText) findViewById(R.id.invisible);

        invisible.setVisibility(View.GONE);

        context = getApplicationContext();
        item_list.loadItems(context);
        contact_list.loadContacts(context);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, contact_list.getAllUsernames());
        borrower_spinner.setAdapter(adapter);

        Intent intent = getIntent();   // Get intent from ItemsFragment
        int pos = intent.getIntExtra("position", 0);

        item = item_list.getItem(pos);

        Contact contact = item.getBorrower();
        if (contact != null){
            int contact_pos = contact_list.getIndex(contact);
            borrower_spinner.setSelection(contact_pos);
        }

        title.setText(item.getTitle());
        maker.setText(item.getMaker());
        description.setText(item.getDescription());
        length.setText(item.getLength());
        width.setText(item.getWidth());
        height.setText(item.getHeight());

        String status_str = item.getStatus();
        if (status_str.equals("Borrowed")) {
            status.setChecked(false);
        } else {
            borrower_tv.setVisibility(View.GONE);
            borrower_spinner.setVisibility(View.GONE);
        }

        image = item.getImage();
        if (image != null) {
            photo.setImageBitmap(image);
        } else {
            photo.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    public void addPhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public void deletePhoto(View view) {
        image = null;
        photo.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent intent){
        if (request_code == REQUEST_CODE && result_code == RESULT_OK){
            Bundle extras = intent.getExtras();
            image = (Bitmap) extras.get("data");
            photo.setImageBitmap(image);
        }
    }

    public void deleteItem(View view) {

        // Delete item
        DeleteItemCommand deleteItemCommand = new DeleteItemCommand(item_list, item, context);
        deleteItemCommand.execute();

        if (!deleteItemCommand.isExecuted()) return;

        // End EditItemActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void saveItem(View view) {

        String title_str = title.getText().toString();
        String maker_str = maker.getText().toString();
        String description_str = description.getText().toString();
        String length_str = length.getText().toString();
        String width_str = width.getText().toString();
        String height_str = height.getText().toString();

        Contact contact = null;
        if (!status.isChecked()) {
            String borrower_str = borrower_spinner.getSelectedItem().toString();
            contact = contact_list.getContactByUsername(borrower_str);
        }

        if (title_str.equals("")) {
            title.setError("Empty field!");
            return;
        }

        if (maker_str.equals("")) {
            maker.setError("Empty field!");
            return;
        }

        if (description_str.equals("")) {
            description.setError("Empty field!");
            return;
        }

        if (length_str.equals("")) {
            length.setError("Empty field!");
            return;
        }

        if (width_str.equals("")) {
            width.setError("Empty field!");
            return;
        }

        if (height_str.equals("")) {
            height.setError("Empty field!");
            return;
        }

        String id = item.getId(); // Reuse the item id
        Item updated_item = new Item(title_str, maker_str, description_str, image, id );
        updated_item.setDimensions(length_str, width_str, height_str);

        boolean checked = status.isChecked();
        if (!checked) {
            updated_item.setStatus("Borrowed");
            updated_item.setBorrower(contact);
        }

        // Edit item
        EditItemCommand editItemCommand = new EditItemCommand(item_list, item, updated_item, context);
        editItemCommand.execute();

        if (!editItemCommand.isExecuted()) return;

        // End EditItemActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Checked = Available
     * Unchecked = Borrowed
     */
    public void toggleSwitch(View view){
        if (status.isChecked()) {
            // Means was previously borrowed, switch was toggled to available
            borrower_spinner.setVisibility(View.GONE);
            borrower_tv.setVisibility(View.GONE);
            item.setBorrower(null);
            item.setStatus("Available");

        } else {
            // Means not borrowed
            if (contact_list.getSize()==0){
                // No contacts, need to add contacts to be able to add a borrower.
                invisible.setEnabled(false);
                invisible.setVisibility(View.VISIBLE);
                invisible.requestFocus();
                invisible.setError("No contacts available! Must add borrower to contacts.");
                status.setChecked(true); // Set switch to available

            } else {
                // Means was previously available
                borrower_spinner.setVisibility(View.VISIBLE);
                borrower_tv.setVisibility(View.VISIBLE);
            }
        }
    }
}
