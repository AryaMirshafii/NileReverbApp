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
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Pattern;

public class Musicmanager {
    final String TAG = "Musicmanager";
    private ContentResolver mContentResolver;

    private List<Song> mySongs = new ArrayList<Song>();

    private Context mContext;

    private stringSearch stringSearcher;
    private ArrayList<String> genres;
    private ArrayList<String> artists;
    private HashMap<String, Song[]> artistMap;
    private HashMap<String, Song[]> albumMap;

    private MediaPlayer mediaPlayer;



    private HashMap<String, Song[]> genreMap;
    public Musicmanager(ContentResolver cr, Context context) {
        this.mContentResolver = cr;
        this.mContext = context;
        stringSearcher = new stringSearch();
        genres = new ArrayList<>();
        artists = new ArrayList<>();
        artistMap = new HashMap<>();
        genreMap = new HashMap<>();
        mediaPlayer = new MediaPlayer();
        albumMap = new HashMap<>();
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

        int genreColumn = cur.getColumnIndex(MediaStore.Audio.Genres.NAME);

        Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
        Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));
        // add each song to mItems


        String currentArtist = cur.getString(artistColumn);
        ArrayList<Song> artistSongs = new ArrayList<>();

        String currentAlbum = cur.getString(albumColumn);
        ArrayList<Song> albumSongs = new ArrayList<>();



        do {
            Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            Song newSong = new Song(
                    cur.getLong(idColumn),
                    cur.getString(artistColumn),
                    cur.getString(titleColumn),
                    cur.getString(albumColumn),
                    cur.getLong(durationColumn));

            mySongs.add(newSong);







            if(!currentArtist.equals(cur.getString(artistColumn))){


                currentArtist = cur.getString(artistColumn);
                artistSongs.clear();
            }else{
                artistSongs.add(newSong);
            }

            if(!currentAlbum.equals(cur.getString(albumColumn))){


                currentAlbum = cur.getString(albumColumn);
                albumSongs.clear();
            }else{
                albumSongs.add(newSong);
            }




            artistMap.put(cur.getString(artistColumn).trim().toLowerCase().replaceAll("[^A-Za-z]+", "") ,
                    artistSongs.toArray(new Song[artistSongs.size()]));

            albumMap.put(cur.getString(albumColumn).trim().toLowerCase().replaceAll("[^A-Za-z]+", ""), albumSongs.toArray(new
                    Song[albumSongs.size()]));



        } while (cur.moveToNext());

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);



        Log.i(TAG, "Done querying media. MusicRetriever is ready.");
        System.out.println("The number of artists is: " + artistMap.size() + " But artists list size is" + artists.size());

    }

    public void playSong(String songName){
        songName = songName.replaceAll("[^A-Za-z]+", "").toLowerCase();
        System.out.println("The Song name is" + songName);
        if (mySongs.size() <= 0){
            System.out.println("NO song found");
            return;
        }
        int maxLength = 0;
        Uri theUri = Uri.parse("");
        for(Song aSong : mySongs){
            String songTitle = aSong.title.replaceAll("[^A-Za-z]+", "").toLowerCase();
            if(stringSearcher.lcs(songTitle,songName).length() == songName.length()){
                System.out.println("it is equal");
                theUri = aSong.getURI();
                maxLength = 0;
                playFile((theUri));
                return;


            }else if(stringSearcher.lcs(songTitle,songName).length() >maxLength){
                theUri = aSong.getURI();
                maxLength = stringSearcher.lcs(songTitle,songName).length();
            }




        }


        playFile((theUri));
        maxLength = 0;



    }

    private void playFile(Uri theUri){
        if(!theUri.equals(Uri.parse("")) ){

            //System.out.println("My URI is " + myUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


            try{

                mediaPlayer.setDataSource(mContext, theUri);
                mediaPlayer.prepare();

            }catch (IOException e) {
                e.printStackTrace();
                Log.e("IO","IO"+e);

            }




            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                @Override public void onPrepared(MediaPlayer player) {
                    player.start();
                }

            });
        }
    }

    /**
     * A helper method to test that the songs are being correcctly added to the hashmap.
     * @param key the key to use .get() on the hashmap
     */
    public void printTest(String key){
        key = key.trim().toLowerCase().replaceAll("[^A-Za-z]+", "");
        System.out.println("My album name key is " + key);

        Song[] songs = albumMap.get(key);

        for(Song aSong: songs){
            System.out.println("SONG IS " + aSong.title + "By + " + key + "from the album " + aSong.album);
        }

    }

    private void createGenres(){

    }


    public void playArtist(String artistName){

        artistName = artistName.replaceAll("[^A-Za-z]+", "").toLowerCase();

        if(!artistMap.containsKey(artistName)){
            System.out.println("The key doesnt exist");
            return;
        }
        Song[] songs = artistMap.get(artistName);
        Random random = new Random();
        Uri randomUri  =  songs[random.nextInt(songs.length )].getURI();
        playFile(randomUri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Uri theUri  =  songs[random.nextInt(songs.length)].getURI();


                //https://stackoverflow.com/questions/7816551/java-lang-illegalstateexception-what-does-it-mean
                mp.reset();
                try{

                    mp.setDataSource(mContext, theUri);
                    mp.prepare();

                }catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO","IO"+e);

                }




                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                    @Override public void onPrepared(MediaPlayer player) {
                        player.start();
                    }

                });


            }
        });
    }




    public void playAlbum(String albumName){

        albumName = albumName.replaceAll("[^A-Za-z]+", "").toLowerCase();

        if(!artistMap.containsKey(albumName)){
            System.out.println("The key doesnt exist");
            return;
        }
        Song[] songs = artistMap.get(albumName);
        Random random = new Random();
        Uri randomUri  =  songs[random.nextInt(songs.length )].getURI();
        playFile(randomUri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Uri theUri  =  songs[random.nextInt(songs.length)].getURI();


                //https://stackoverflow.com/questions/7816551/java-lang-illegalstateexception-what-does-it-mean
                mp.reset();
                try{

                    mp.setDataSource(mContext, theUri);
                    mp.prepare();

                }catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO","IO"+e);

                }




                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                    @Override public void onPrepared(MediaPlayer player) {
                        player.start();
                    }

                });


            }
        });
    }








}
