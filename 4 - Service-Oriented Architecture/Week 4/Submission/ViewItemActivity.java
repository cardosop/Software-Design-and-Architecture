package com.example.sharingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewItemActivity extends AppCompatActivity implements Observer {

    private ItemList item_list = new ItemList();
    private ItemListController item_list_controller = new ItemListController(item_list);

    private Item item;
    private ItemController item_controller;

    private BidList bid_list = new BidList();
    private BidListController bid_list_controller = new BidListController(bid_list);

    private Context context;

    private UserList user_list = new UserList();
    private UserListController user_list_controller = new UserListController(user_list);

    private Bitmap image;
    private ImageView photo;

    private TextView title_tv;
    private TextView maker_tv;
    private TextView description_tv;
    private TextView length_tv;
    private TextView width_tv;
    private TextView height_tv;
    private TextView current_bid_right_tv;
    private TextView current_bid_left_tv;
    private EditText bid_amount;
    private Button save_bid_button;
    private Button return_item_button;

    private boolean on_create_update;

    private String title_str;
    private String maker_str;
    private String description_str;
    private String length_str;
    private String width_str;
    private String height_str;
    private String user_id;
    private String current_bid_amount_str;
    private String new_bid_amount_str;
    private String item_id;

    private Float new_bid_amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);

        title_tv = (TextView) findViewById(R.id.title_right_tv);
        maker_tv = (TextView) findViewById(R.id.maker_right_tv);
        description_tv = (TextView) findViewById(R.id.description_right_tv);
        length_tv = (TextView) findViewById(R.id.length_tv);
        width_tv = (TextView) findViewById(R.id.width_tv);
        height_tv = (TextView) findViewById(R.id.height_tv);
        photo = (ImageView) findViewById(R.id.image_view);
        current_bid_right_tv = (TextView) findViewById(R.id.current_bid_right_tv);
        current_bid_left_tv = (TextView) findViewById(R.id.current_bid_left_tv);

        bid_amount = (EditText) findViewById(R.id.bid_amount);

        save_bid_button = (Button) findViewById(R.id.save_bid_button);
        return_item_button = (Button) findViewById(R.id.return_item_button);

        Intent intent = getIntent(); // Get intent from BorrowedItemsActivity/SearchActivity
        item_id = intent.getStringExtra("item_id");
        user_id = intent.getStringExtra("user_id");

        context = getApplicationContext();

        on_create_update = false; // Suppress first call to update()
        user_list_controller.getRemoteUsers();

        on_create_update = true; // First call to update occurs now
        bid_list_controller.getRemoteBids(context);
        bid_list_controller.addObserver(this);
        item_list_controller.addObserver(this);
        item_list_controller.getRemoteItems();

        on_create_update = false; // Suppress any further calls to update()
    }

    @Override
    public void onBackPressed() {
        Intent borrow_intent = new Intent(this, BorrowedItemsActivity.class);
        borrow_intent.putExtra("user_id", user_id);
        startActivity(borrow_intent);
    }

    public void saveBid(View view) {
        title_str = title_tv.getText().toString();
        maker_str = maker_tv.getText().toString();
        description_str = description_tv.getText().toString();
        current_bid_amount_str = current_bid_right_tv.getText().toString();
        new_bid_amount_str = bid_amount.getText().toString();
        length_str = length_tv.getText().toString();
        width_str = width_tv.getText().toString();
        height_str = height_tv.getText().toString();

        if(!validateInput()){ // Current bid amount must be higher than the previous bid
            return;
        }

        String owner_id_str = item_controller.getOwnerId();
        String status_str = "Bidded";
        String minimum_bid_amount_str = item_controller.getMinBid().toString();
        String username = user_list_controller.getUsernameByUserId(user_id);

        Bid bid = new Bid(item_id, new_bid_amount, username);

        boolean success = bid_list_controller.addBid(bid);
        if (!success){
            return;
        }

        // Reuse the item id
        Item updated_item = new Item(title_str, maker_str, description_str,owner_id_str,
                minimum_bid_amount_str, image, item_id);
        ItemController updated_item_controller = new ItemController(updated_item);

        updated_item_controller.setStatus(status_str);
        updated_item_controller.setDimensions(length_str, width_str, height_str);

        success = item_list_controller.editItem(item, updated_item);
        if (!success){
            return;
        }

        // End ViewItemActivity
        item_list_controller.removeObserver(this);
        bid_list_controller.removeObserver(this);

        final Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("user_id", user_id);

        // Delay launch of SearchActivity to allow server enough time to process request
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Bid placed.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        }, 750);

    }

    public void update() {
        if (on_create_update){
            // For all status options we do the following
            item = item_list_controller.getItemById(item_id);
            item_controller = new ItemController(item);

            title_tv.setText(item_controller.getTitle());
            maker_tv.setText(item_controller.getMaker());
            description_tv.setText(item_controller.getDescription());

            length_tv.setText(item_controller.getLength());
            width_tv.setText(item_controller.getWidth());
            height_tv.setText(item_controller.getHeight());

            image = item_controller.getImage();
            if (image != null) {
                photo.setImageBitmap(image);
            } else {
                photo.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            String status = item_controller.getStatus();

            if (status.equals("Available") || status.equals("Bidded")) {
                Float highest_bid = bid_list_controller.getHighestBid(item_id);

                if (highest_bid == null) {
                    current_bid_right_tv.setText(item_controller.getMinBid().toString());
                } else {
                    current_bid_right_tv.setText(highest_bid.toString());
                }
            } else { // Borrowed
                current_bid_right_tv.setVisibility(View.GONE);
                current_bid_left_tv.setVisibility(View.GONE);
                bid_amount.setVisibility(View.GONE);
                save_bid_button.setVisibility(View.GONE);
                return_item_button.setVisibility(View.VISIBLE);

            }
        }
    }

    public void returnItem(View view) {

        title_str = title_tv.getText().toString();
        maker_str = maker_tv.getText().toString();
        description_str = description_tv.getText().toString();
        length_str = length_tv.getText().toString();
        width_str = width_tv.getText().toString();
        height_str = height_tv.getText().toString();
        String status = "Available";
        String owner_id = item_controller.getOwnerId();
        String minimum_bid = String.valueOf(item_controller.getMinBid());

        Item updated_item = new Item(title_str, maker_str, description_str, owner_id, minimum_bid, image, item_id);
        ItemController updated_item_controller = new ItemController(updated_item);
        updated_item_controller.setDimensions(length_str, width_str, height_str);
        updated_item_controller.setStatus(status);

        boolean success = item_list_controller.editItem(item, updated_item);
        if (!success){
            return;
        }

        // End ViewItemActivity
        item_list_controller.removeObserver(this);
        bid_list_controller.removeObserver(this);

        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_id", user_id);

        // Delay launch of MainActivity to allow server enough time to process request
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Item returned.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        }, 750);
    }

    public boolean validateInput(){
        if (new_bid_amount_str.equals("")) {
            bid_amount.setError("Enter Bid!");
            return false;
        }

        Float current_bid_amount = Float.valueOf(current_bid_amount_str);
        new_bid_amount = Float.valueOf(new_bid_amount_str);

        if (new_bid_amount <= current_bid_amount){
            bid_amount.setError("New bid amount must be higher than current bid amount!");
            return false;
        }

        return true;
    }
}
