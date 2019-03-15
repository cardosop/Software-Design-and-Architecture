package com.example.sharingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * ItemFragmentAdapter is responsible for what information is displayed in ListView entries.
 */
public class ItemFragmentAdapter extends ArrayAdapter<Item> {
    private LayoutInflater inflater;
    private Fragment fragment;
    private Context context;

    public ItemFragmentAdapter(Context context, ArrayList<Item> items, Fragment fragment) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        ItemController item_controller = new ItemController(item);

        String title = "Title: " + item_controller.getTitle();
        String description = "Description: " + item_controller.getDescription();
        Bitmap thumbnail = item_controller.getImage();
        String status = "Status: " + item_controller.getStatus();

        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = inflater.from(context).inflate(R.layout.itemlist_item, parent, false);
        }

        TextView title_tv = (TextView) convertView.findViewById(R.id.title_tv);
        TextView status_tv = (TextView) convertView.findViewById(R.id.status_tv);
        TextView description_tv = (TextView) convertView.findViewById(R.id.description_tv);
        ImageView photo = (ImageView) convertView.findViewById(R.id.image_view);
        TextView bidder_tv = (TextView) convertView.findViewById(R.id.bidder_tv);
        TextView highest_bid_tv = (TextView) convertView.findViewById(R.id.highest_bid_tv);

        if (thumbnail != null) {
            photo.setImageBitmap(thumbnail);
        } else {
            photo.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        title_tv.setText(title);
        description_tv.setText(description);

        // AllItemFragments: itemlist_item shows title, description and status
        if (fragment instanceof AllItemsFragment ) {
            status_tv.setText(status);
        }

        // BorrowedItemsFragment/AvailableItemsFragment: itemlist_item shows title and description only
        if (fragment instanceof BorrowedItemsFragment || fragment instanceof AvailableItemsFragment
                || fragment instanceof BiddedItemsFragment) {
            status_tv.setVisibility(View.GONE);
        }

        //  BiddedItemsFragment: itemlist_item shows bidder and highest bid
        if (fragment instanceof BiddedItemsFragment) {
            BidList bid_list = new BidList();
            BidListController bid_list_controller = new BidListController(bid_list);
            bid_list_controller.getRemoteBids(parent.getContext());

            String bidder = "Bidder: " + bid_list_controller.getHighestBidder(item_controller.getId());
            String highest_bid = "Highest Bid: " + bid_list_controller.getHighestBid(item_controller.getId());

            bidder_tv.setVisibility(View.VISIBLE);
            highest_bid_tv.setVisibility(View.VISIBLE);
            bidder_tv.setText(bidder);
            highest_bid_tv.setText(highest_bid);
        }

        return convertView;
    }
}
