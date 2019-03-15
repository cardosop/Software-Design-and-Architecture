package com.example.sharingapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Add a new contact
 */
public class AddContactActivity extends AppCompatActivity {

    private ContactList contact_list = new ContactList();
    private ContactListController contact_list_controller = new ContactListController(contact_list);

    private Context context;

    private EditText username;
    private EditText email;

    private String username_str = username.getText().toString();
    private String email_str = email.getText().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);

        context = getApplicationContext();
        contact_list_controller.loadContacts(context);
    }

    public boolean validateInput() {
        if (username_str.equals("")) {
            username.setError("Empty field!");
            return false;
        }

        if (!email_str.contains("@")) {
            email.setError("Must be an email address!");
            return false;
        }

        if (!contact_list_controller.isUsernameAvailable(username_str)) {
            username.setError("Username already taken!");
            return false;
        }

        return true;
    };

    public void saveContact(View view) {

        if ( !validateInput() ) {
            return;
        }

        Contact contact = new Contact(username_str, email_str, null);

        // Add contact
        boolean success = contact_list_controller.addContact(contact, context);
        if (!success) {
            return;
        }

        // End AddContactActivity
        finish();
    }
}
