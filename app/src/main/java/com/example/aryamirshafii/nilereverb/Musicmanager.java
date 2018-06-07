package com.example.aryamirshafii.nilereverb;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Musicmanager {
    final String TAG = "Musicmanager";
    private ContentResolver mContentResolver;

    private List<Song> mItems = new ArrayList<Song>();
    private Random mRandom = new Random();
    private Context mContext;
    public Musicmanager(ContentResolver cr, Context context) {
        this.mContentResolver = cr;
        this.mContext = context;
    }


    /**
     * Loads music data. This method may take long, so be sure to call it asynchronously without
     * blocking the main thread.
     */
    public void prepare() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying media...");
        Log.i(TAG, "URI: " + uri.toString());
        // Perform a query on the content resolver. The URI we're passing specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        Cursor cur = mContentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
        Log.i(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
        if (cur == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return;
        }
        if (!cur.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            Log.e(TAG, "Failed to move cursor to first row (no query results).");
            return;
        }
        Log.i(TAG, "Listing...");
        // retrieve the indices of the columns where the ID, title, etc. of the song are
        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
        Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
        Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));
        // add each song to mItems
        do {
            Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            mItems.add(new Song(
                    cur.getLong(idColumn),
                    cur.getString(artistColumn),
                    cur.getString(titleColumn),
                    cur.getString(albumColumn),
                    cur.getLong(durationColumn)));
        } while (cur.moveToNext());
        Log.i(TAG, "Done querying media. MusicRetriever is ready.");
    }

    public void playHotelCalifornia(){
        if (mItems.size() <= 0){
            System.out.println("NO song found");
            return;
        }

        for(Song aSong : mItems){
            if(aSong.title.equals("Hotel California")){
                Uri myUri = aSong.getURI();
                System.out.println("My URI is " + myUri);
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try{

                     mediaPlayer.setDataSource(mContext, myUri);
                     mediaPlayer.prepare();

                }catch (IOException e) {
                e.printStackTrace();
                Log.e("IO","IO"+e);

                }


                //mediaPlayer.start();

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                @Override public void onPrepared(MediaPlayer player) {
                    player.start();
                }

                });

            }
        }
    }






}
