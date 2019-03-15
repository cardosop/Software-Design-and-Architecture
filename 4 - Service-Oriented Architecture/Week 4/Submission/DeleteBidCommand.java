package com.example.sharingapp;

import android.content.Context;
import java.util.concurrent.ExecutionException;

/**
 * Command to delete a bid
 */
public class DeleteBidCommand extends Command {

    private BidList bid_list;
    private Bid bid;
    private Context context;

    public DeleteBidCommand(Bid bid) { this.bid = bid; }

    // Delete the bid remotely from server
    public void execute() {
        ElasticSearchManager.RemoveBidTask remove_bid_task = new ElasticSearchManager.RemoveBidTask();
        remove_bid_task.execute(bid);

        // use get() to access the return of RemoveBidTask. i.e. RemoveBidTask returns a Boolean to
        // indicate if the bid was successfully deleted from the remote server
        try {
            if(remove_bid_task.get()) {
                bid_list.removeBid(bid);
                super.setIsExecuted(true);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            super.setIsExecuted(false);
        }
    }
}