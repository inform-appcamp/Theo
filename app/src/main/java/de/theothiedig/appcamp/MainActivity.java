package de.theothiedig.appcamp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS_REQUEST = 0;
    static final int MY_INTENT_PICK_CONTACT = 1;
    static final int MY_INTENT_CALL_CONTACT = 2;
    private String emergency_contact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setBackgroundColor(Color.RED);
        String[] pems = new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS};
        boolean pem = true;
        for (String s : pems) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), s) != PackageManager.PERMISSION_GRANTED) {
                pem = false;
                Log.w("Pem", "Permission " + s + " was not granted.");
            }
        }
        if (!pem)
            ActivityCompat.requestPermissions(this, pems, MY_PERMISSIONS_REQUEST);
        else Log.i("Pem", "Permissions was already granted");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean pem = true;
        for (int i = 0; i < grantResults.length; i++)
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                pem = false;
                Log.w("Pem", "Permission " + permissions[i] + " was not granted");
            }
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && pem) {
                    Log.i("Pem", "Permissions were granted");
                } else {
                    Toast.makeText(getApplicationContext(), "Diese App funktioniert nicht ohne die entsprechenden Berechtigungen.", Toast.LENGTH_LONG).show();
                    this.finish();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MY_INTENT_PICK_CONTACT: {
                if (resultCode == Activity.RESULT_OK) {
                    Uri contact = data.getData();
                    assert contact != null;
                    Cursor c = getContentResolver().query(contact, null, null, null, null);
                    String emergency_id;
                    String emergency_hasNumber;
                    StringBuilder emergency_number = new StringBuilder();
                    assert c != null;
                    if (c.moveToFirst()) {
                        emergency_id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        emergency_hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (Integer.valueOf(emergency_hasNumber) == 1) {
                            Cursor n = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + emergency_id,
                                    null, null);
                            assert n != null;
                            if (n.moveToFirst())
                                emergency_number.append(n.getString(n.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            n.close();
                        }
                    }
                    c.close();
                    emergency_contact = emergency_number.toString();
                    Toast.makeText(getApplicationContext(), "New Emergency Phone Number: " + emergency_contact, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onClickButton(View view) {
        if (this.emergency_contact != null) {
            Intent call_contact = new Intent(Intent.ACTION_CALL);
            call_contact.setData(Uri.parse("tel:" + this.emergency_contact.toString()));
            startActivityForResult(call_contact, MY_INTENT_CALL_CONTACT);
        } else
            Toast.makeText(getApplicationContext(), "Es muss ein Kontakt angegeben werden.", Toast.LENGTH_LONG).show();
    }

    public void onClickButton2(View view) {
        Intent get_contact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(get_contact, MY_INTENT_PICK_CONTACT);
    }
}
