package com.example.sharingapp;

import android.os.AsyncTask;
import android.util.Log;

import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * For remote machine: SERVER = "http://34.202.206.222:8080"
 * -------------------------------------------------------------------------------------------------
 * curl -XDELETE 'http://34.202.206.222:8080/INDEX' - can be used to delete ALL objects on the server
 * (items, users, and bids) at that index
 * view an item at: http://34.202.206.222:8080/INDEX/items/item_id
 * view a user at: http://34.202.206.222:8080/INDEX/users/user_id
 * view a bid at: http://34.202.206.222:8080/INDEX/bids/bid_id
 * Where INDEX is replaced with the random number string you generate as per the assignment
 * instructions. Note: item_ids and user_ids are printed to the log (See the Android Monitor)
 * as each user/item is added.
 */

public class ElasticSearchManager {
    private static final String SERVER = "http://34.202.206.222:8080";
    private static final String INDEX = "127113579"; // TODO: MUST CHANGE THIS to random number string
    private static final String ITEM_TYPE = "items";
    private static final String USER_TYPE = "users";
    private static JestDroidClient client;

    /**
     * Returns all remote items from server
     */
    public static class GetItemListTask extends AsyncTask<Void,Void,ArrayList<Item>> {

        @Override
        protected ArrayList<Item> doInBackground(Void... params) {

            verifyConfig();
            ArrayList<Item> items = new ArrayList<>();
            String search_string = "{\"from\":0,\"size\":10000}";

            Search search = new Search.Builder(search_string).addIndex(INDEX).addType(ITEM_TYPE).build();
            try {
                SearchResult execute = client.execute(search);
                if (execute.isSucceeded()) {
                    items = (ArrayList<Item>) execute.getSourceAsObjectList(Item.class);
                    Log.i("ELASTICSEARCH","Item search was successful");
                } else {
                    Log.i("ELASTICSEARCH", "No items found");
                }
            } catch (IOException e) {
                Log.i("ELASTICSEARCH", "Item search failed");
                e.printStackTrace();
            }

            return items;
        }
    }

    /**
     * Add item to remote server
     */
    public static class AddItemTask extends AsyncTask<Item,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Item... params) {

            verifyConfig();
            Boolean success = false;
            Item item = params[0];

            String id = item.getId(); // Explicitly set the id to match the locally generated id
            Index index = new Index.Builder(item).index(INDEX).type(ITEM_TYPE).id(id).build();
            try {
                DocumentResult execute = client.execute(index);
                if(execute.isSucceeded()) {
                    Log.i("ELASTICSEARCH", "Add item was successful");
                    Log.i("ADDED ITEM", id);
                    success = true;
                } else {
                    Log.e("ELASTICSEARCH", "Add item failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
    }


    /**
     * Delete item from remote server using item_id
     */
    public static class RemoveItemTask extends AsyncTask<Item,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Item... params) {

            verifyConfig();
            Boolean success = false;
            Item item_to_delete = params[0];
            try {
                DocumentResult execute = client.execute(new Delete.Builder(item_to_delete.getId()).index(INDEX).type(ITEM_TYPE).build());
                if(execute.isSucceeded()) {
                    Log.i("ELASTICSEARCH", "Delete item was successful");
                    success = true;
                } else {
                    Log.e("ELASTICSEARCH", "Delete item failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
    }


    /**
     * Returns all remote users from server
     */
    public static class GetUserListTask extends AsyncTask<Void,Void,ArrayList<User>> {

        @Override
        protected ArrayList<User> doInBackground(Void... params) {
            verifyConfig();

            ArrayList<User> users = new ArrayList<>();
            String search_string = "{\"from\":0,\"size\":10000}";

            Search search = new Search.Builder(search_string).addIndex(INDEX).addType(USER_TYPE).build();
            try {
                SearchResult execute = client.execute(search);
                if (execute.isSucceeded()) {
                    List<User> remote_users = execute.getSourceAsObjectList(User.class);
                    users.addAll(remote_users);
                    Log.i("ELASTICSEARCH","User search was successful");
                } else {
                    Log.i("ELASTICSEARCH", "No users found");
                }
            } catch (IOException e) {
                Log.i("ELASTICSEARCH", "User search failed");
                e.printStackTrace();
            }

            return users;
        }
    }

    /**
     * Add user to remote server
     */
    public static class AddUserTask extends AsyncTask<User,Void,Boolean> {

        @Override
        protected Boolean doInBackground(User... params) {

            verifyConfig();
            Boolean success = false;
            User user = params[0];

            String id = user.getId(); // Explicitly set the id to match the locally generated id
            Index index = new Index.Builder(user).index(INDEX).type(USER_TYPE).id(id).build();
            try {
                DocumentResult execute = client.execute(index);
                if(execute.isSucceeded()) {
                    Log.i("ELASTICSEARCH", "User was successfully added");
                    Log.i("ADDED USER", id);
                    success = true;
                } else {
                    Log.e("ELASTICSEARCH", "User failed to be added");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
    }


    /**
     * Delete user from remote server using user_id
     */
    public static class RemoveUserTask extends AsyncTask<User,Void,Boolean> {

        @Override
        protected Boolean doInBackground(User... params) {

            verifyConfig();
            Boolean success = false;
            User user = params[0];
            try {
                DocumentResult execute = client.execute(new Delete.Builder(user.getId()).index(INDEX).type(USER_TYPE).build());
                if(execute.isSucceeded()) {
                    Log.i("ELASTICSEARCH", "User was successfully deleted");
                    success = true;
                } else {
                    Log.e("ELASTICSEARCH", "User delete failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
    }

    /**
     * Delete bid from remote server using user_id
     */

    public static class RemoveBidTask extends AsyncTask<Bid,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Bid... params) {

            verifyConfig();
            Boolean success = false;
            Bid bid = params[0];
            try {
                DocumentResult execute = client.execute(new Delete.Builder(bid.getBidId()).index(INDEX).type(USER_TYPE).build());
                if(execute.isSucceeded()) {
                    Log.i("ELASTICSEARCH", "Bid was successfully deleted");
                    success = true;
                } else {
                    Log.e("ELASTICSEARCH", "Bid Delete failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
    }

    /**
     * Add bid to remote server
     */
    public static class AddBidTask extends AsyncTask<Bid,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Bid... params) {

            verifyConfig();
            Boolean success = false;
            Bid bid = params[0];

            String id = bid.getBidId(); // Explicitly set the id to match the locally generated id
            Index index = new Index.Builder(bid).index(INDEX).type(USER_TYPE).id(id).build();
            try {
                DocumentResult execute = client.execute(index);
                if(execute.isSucceeded()) {
                    Log.i("ELASTICSEARCH", "Bid was successfully added");
                    Log.i("ADDED Bid", id);
                    success = true;
                } else {
                    Log.e("ELASTICSEARCH", "Bid failed to be added");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }
    }

    /**
     * Returns all remote bids from server
     */
    public static class GetBidListTask extends AsyncTask<Void,Void,ArrayList<Bid>> {

        @Override
        protected ArrayList<Bid> doInBackground(Void... params) {

            verifyConfig();
            ArrayList<Bid> bids = new ArrayList<>();
            String search_string = "{\"from\":0,\"size\":10000}";

            Search search = new Search.Builder(search_string).addIndex(INDEX).addType(USER_TYPE).build();
            try {
                SearchResult execute = client.execute(search);
                if (execute.isSucceeded()) {
                    bids = (ArrayList<Bid>) execute.getSourceAsObjectList(Bid.class);
                    Log.i("ELASTICSEARCH","Bid search was successful");
                } else {
                    Log.i("ELASTICSEARCH", "No Bids found");
                }
            } catch (IOException e) {
                Log.i("ELASTICSEARCH", "Bid search failed");
                e.printStackTrace();
            }

            return bids;
        }
    }

    // If no client, add one
    private static void verifyConfig() {
        if(client == null) {
            DroidClientConfig.Builder builder = new DroidClientConfig.Builder(SERVER);
            DroidClientConfig config = builder.build();
            JestClientFactory factory = new JestClientFactory();
            factory.setDroidClientConfig(config);
            client = (JestDroidClient) factory.getObject();
        }
    }
}
