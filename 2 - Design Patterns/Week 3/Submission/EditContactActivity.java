package com.example.sharingapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Editing a pre-existing contact consists of deleting the old contact and adding a new contact with the old
 * contact's id.
 * Note: You will not be able to edit contacts which are "active" borrowers
 */
public class EditContactActivity extends AppCompatActivity implements Observer {

    private ContactList contact_list = new ContactList();
    private ContactListController contact_list_controller = new ContactListController(contact_list);

    private Contact contact;
    private ContactController contact_controller;

    private EditText email;
    private EditText username;
    private Context context;
    private int pos;
    private boolean on_create_update = true;

    private String email_str = email.getText().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        Intent intent = getIntent();
        pos = intent.getIntExtra("position", 0);

        context = getApplicationContext();
        contact_list_controller.addObserver(this);
        contact_list_controller.loadContacts(context);
        on_create_update = false;
    }

    public boolean validateInput() {

        if (email_str.equals("")) {
            email.setError("Empty field!");
            return false;
        }
        if (!email_str.contains("@")) {
            email.setError("Must be an email address!");
            return false;
        }

        return true;
    };

    public void saveContact(View view) {

        if ( !validateInput() ) {
            return;
        }

        String username_str = username.getText().toString();

        // Check that username is unique AND username is changed (Note: if username was not changed
        // then this should be fine, because it was already unique.)
        if (!contact_list_controller.isUsernameAvailable(username_str) &&
                !(contact.getUsername().equals(username_str))){
            username.setError("Username already taken!");
            return;
        }

        // Reuse the contact id
        String id_str = contact_controller.getId();
        Contact updated_contact = new Contact(username_str, email_str, id_str);

        // Edit Contact: replace contact with updated contact
        boolean success = contact_list_controller.editContact(contact, updated_contact, context);
        if (!success) {
            return;
        }

        // End EditContactActivity
        finish();
    }

    public void deleteContact(View view) {

        // Delete contact
        boolean success = contact_list_controller.deleteContact(contact, context);
        if (!success) {
            return;
        }

        // End EditContactActivity
        finish();
    }

    /**
     * Called when the activity is destroyed, thus we remove this activity as a listener
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        contact_list_controller.removeObserver(this);
    }

    /**
     * Only need to update the view from the onCreate method
     */
    public void update(){

        if (on_create_update) {

            contact = contact_list_controller.getContact(pos);
            contact_controller = new ContactController(contact);

            username = (EditText) findViewById(R.id.username);
            email = (EditText) findViewById(R.id.email);

            // Update the view
            username.setText(contact_controller.getUsername());
            email.setText(contact_controller.getEmail());
        }
    }
}
