package run.wallet.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

//import run.wallet.common.contacts.ContactsDb;


public class ActivityMan {


    public static void openAndroidContactsCreateNew(Activity activity, Person person) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if(person.getMainPhone()!=null)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, person.getMainPhone());
        if(person.getMainEmail()!=null)
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, person.getMainEmail());
        intent.putExtra("finishActivityOnSaveCompleted", true);
        activity.startActivity(intent);
    }
	public static void openAndroidContactsCreateNew(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
	    activity.startActivity(intent);
	}
	/*
	public static void openAndroidContactsWithPerson(Activity activity, Person person) {
	    Uri mSelectedContactUri =
	            Contacts.getLookupUri(Long.parseLong(person.getString(Person.STRING_PERSON_ID)), ContactsDb.getContactLookupKey(activity,person.getString(Person.STRING_PERSON_ID)));

	    // Creates a new Intent to edit a contact
	    Intent editIntent = new Intent(Intent.ACTION_EDIT);
	    editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    editIntent.putExtra("finishActivityOnSaveCompleted", true);
	    editIntent.setDataAndType(mSelectedContactUri, Contacts.CONTENT_ITEM_TYPE);
	    activity.startActivity(editIntent);
	}
	*/

	public static void openAndroidBrowserUrl(Activity activity, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}



}
