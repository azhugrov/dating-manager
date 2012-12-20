package com.zhygr.datingmanager;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import com.zhygr.datingmanager.R;
import com.zhygr.util.Consts;

import java.util.ArrayList;

public class HomeActivity extends Activity {
    //region Lifecycle Events
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Consts.ADD_CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {
            //returns a lookup URI to the contact just inserted
            Uri newContact = data.getData();
            TextView contactNameTextView = (TextView) findViewById(R.id.contactName);
            Long groupId = findGirlFriendGroup();
            if (groupId != null) {
                addContactToGirlfriendGroup(newContact, groupId);
            }
        }
    }
    //endregion


    //region Event Handlers
    public void addContact(View view) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
        intent.putExtra(Consts.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
        startActivityForResult(intent, Consts.ADD_CONTACT_REQUEST_CODE);
    }
    //endregion

    //region Helper

    /** Finds target group to which we should add given user. */
    protected Long findGirlFriendGroup() {
        String[] mProjection = {
                ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE
        };

        String mSelectionClause = String.format("%s = ?", ContactsContract.Groups.TITLE);
        String[] mSelectionArgs = { getResources().getString(R.string.girlfriend_group_name) };
        Cursor cursor = getContentResolver().query(
                            ContactsContract.Groups.CONTENT_URI,
                            mProjection,
                            mSelectionClause,
                            mSelectionArgs,
                            ""
        );
        if (cursor == null) {
            android.util.Log.e("contacts", "Could not access curson while performing lookup for group contact.");
        } else {
            android.util.Log.e("contacts", "Could not find a girlfriend group");
            if (cursor.getCount() > 1) {
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        }

        return null;
    }

    protected void addContactToGirlfriendGroup(Uri contact, Long groupId) {
        //first get contact id that will be used to find the raw contact.
        Long contactId = getContactId(contact);
        Long rawContactId = getRawContactId(contactId);

        //update group membership based on raw contact id and group id.
        //first we need to check that we don't have existing group assigned to a given contact


    }

    protected Pair<Boolean, Long> hasExistedGroupMembership(@Nonnull Long rawContactId) {
        Cursor c = getContentResolver()
                   .query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[] {ContactsContract.Data._ID, ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID },
                        ContactsContract.Data.RAW_CONTACT_ID+" = ? AND "+ContactsContract.Data.MIMETYPE+" = ?",
                        new String[] { String.valueOf(),  },
                        null
                        );
    }

    private Long getContactId(Uri contact) {
        return ContentUris.parseId(contact);
    }

    private Long getRawContactId(Long contactId) {
        String[] mProjection = {
                ContactsContract.RawContacts._ID
        };

        String mSelectionClause = String.format("%s = ?", ContactsContract.RawContacts.CONTACT_ID);
        String[] mSelectionArgs = { contactId.toString() };

        Cursor cursor = getContentResolver().query(
            ContactsContract.RawContacts.CONTENT_URI,
            mProjection,
            mSelectionClause,
            mSelectionArgs,
            null
        );

        if (cursor == null) {
            android.util.Log.e("contacts", "Could not fetch the raw contact data data.");
        } else {
            cursor.moveToFirst();
            return cursor.getLong(0);
        }

        return null;
    }
    //endregion

}