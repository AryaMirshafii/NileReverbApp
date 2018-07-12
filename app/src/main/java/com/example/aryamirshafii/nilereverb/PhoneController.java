package com.example.aryamirshafii.nilereverb;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;

public class PhoneController {

    private Context context;
    private HashMap contactMap;
    private String currentContact;

    public PhoneController(Context context){
        this.context = context;
        this.contactMap = new HashMap();
        getContactList();
    }
    private void getContactList() {
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Contact toAdd = new Contact(name.toLowerCase(),phoneNo);
                        contactMap.put(name.toLowerCase(), toAdd);
                        //System.out.println("Name: " + name.toLowerCase());
                        //System.out.println("Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }




    @SuppressWarnings("deprecation")
    public String text(String contactName, String message) {
        if(!contactMap.containsKey(contactName)){
            return "The  contact " + contactName + " doesn't exist";
        }

        if(contactMap.size() == 0){

            return "Please add some contacts in order to continue";
        }
        System.out.println("Texting " + contactName + "......");
        currentContact = contactName;
        Contact contact = (Contact) contactMap.get(contactName);
        String phoneNumber = contact.getPhoneNumber();


        System.out.println("The Phone Number is " + phoneNumber);
        System.out.println("The Message is" + message);
        PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(),0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
        return "Texted " + contactName + ", saying "  + message;
    }



    public String text(String message){
        if(currentContact == null){
            return "Please specify a contact";
        }
        return text(currentContact, message);
    }
    public boolean checkContact(String name){
        return contactMap.containsKey(name.toLowerCase());
    }

    public void setCurrentContact(String name){
        if(!checkContact(name) || !checkContact(name.toLowerCase())){
            return;
        }
        System.out.println("Setting current contact" + name);
        this.currentContact = name;

    }



}
