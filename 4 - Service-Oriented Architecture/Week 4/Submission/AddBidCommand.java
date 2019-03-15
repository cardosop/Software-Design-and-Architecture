package com.example.sharingapp;

import android.content.Context;
import java.util.concurrent.ExecutionException;

/**
 * Command to add a bid
 */
public class AddBidCommand extends Command {

    private BidList bid_list;
    private Bid bid;
    private Context context;

    public AddBidCommand(Bid bid) { this.bid = bid; }

    // Save bid locally
    /*public void execute(){
        bid_list.addBid(bid);
        super.setIsExecuted(bid_list.saveBids(context));
    }*/

    // Save the item remotely to server
    public void execute(){
        ElasticSearchManager.AddBidTask add_bid_task = new ElasticSearchManager.AddBidTask();
        add_bid_task.execute(bid);

        // use get() to access the return of AddBidTask. i.e. AddBidTask returns a Boolean to
        // indicate if the bid was successfully saved to the remote server
        try {
            if(add_bid_task.get()) {
                bid_list.addBid(bid);
                super.setIsExecuted(true);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            super.setIsExecuted(false);
        }
    }
}
