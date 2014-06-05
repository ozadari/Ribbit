package com.ozadari.ribbit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.sax.StartElementListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    public static String TAG =MainActivity.class.getSimpleName();

    public static  final int TAKE_PHOTO_REQUEST = 0;
    public static  final int TAKE_VIDEO_REQUEST = 1;
    public static  final int CHOOSE_PHOTO_REQUEST = 2;
    public static  final int CHOOSE_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_ViDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10;
    protected Uri mMediaUri;

    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch (i)
            {
                case 0: // Take Picture
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutPutMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri==null)
                    {
                        //Display Error
                        Toast.makeText(MainActivity.this,getString(R.string.error_external_storage),Toast.LENGTH_LONG).show();
                    }
                    else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1: // Take Video
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutPutMediaFileUri(MEDIA_TYPE_ViDEO);
                    if(mMediaUri==null)
                    {
                        //Display Error
                        Toast.makeText(MainActivity.this,getString(R.string.error_external_storage),Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT,mMediaUri);
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,7);
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
                        startActivityForResult(videoIntent,TAKE_VIDEO_REQUEST);
                    }
                    break;
                case 2: // Choose Picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent,CHOOSE_PHOTO_REQUEST);
                    break;
                case 3: // Choose Video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    Toast.makeText(MainActivity.this,getString(R.string.video_file_size_warning),Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseVideoIntent, CHOOSE_VIDEO_REQUEST);
                    break;
            }
        }

        private Uri getOutPutMediaFileUri(int mediaType) {
            //Checking if there is SDCard
            if(isExtStorageAvailable())
            {
                //get the uri and return it
                /**
                 *1. Get ext storage directory
                 *2. Create our subDirectory
                 *3. create file name
                 *4. Create the actual files
                 *5 create the files URI **/
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),appName);

                if(!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.e(TAG,"Failed to create directory");
                        return null;
                    }

                }

                File mediaFile;
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
                String path = mediaStorageDir.getPath()+File.separator;
                if(mediaType==MEDIA_TYPE_IMAGE)
                    mediaFile = new File(path+"IMG_"+timestamp+".jpg");
                else if(mediaType==MEDIA_TYPE_ViDEO)
                    mediaFile = new File(path+"VID_"+timestamp+".mp4");
                else
                    return null;

                Log.d(TAG,"File: " + Uri.fromFile(mediaFile));
                return Uri.fromFile(mediaFile);
            }
            return null;
        }
        private boolean isExtStorageAvailable()
        {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }
    };
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        ParseUser current = ParseUser.getCurrentUser();
        if (current == null) {
            //Switching to login activity
            navigateToLogin();
        }
        else
        {
            Log.i(TAG,"Logged in as"+current.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK) {
            //adding to the gallery
            if (requestCode == CHOOSE_PHOTO_REQUEST || requestCode == CHOOSE_VIDEO_REQUEST) {
                if (data == null)
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                else {
                    mMediaUri = data.getData();
                }

                Log.i(TAG,"Media URI:" + mMediaUri);
                if(requestCode == CHOOSE_VIDEO_REQUEST)
                {
                    //make sure the video is less then 10MB
                    InputStream inputStream=null;
                    int fileSize=0;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(MainActivity.this,getString(R.string.error_opening_file),Toast.LENGTH_LONG).show();
                        return;
                    }catch (IOException e)
                    {
                        Toast.makeText(MainActivity.this,getString(R.string.error_opening_file),Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                           /**Intentionally blank***/
                        }
                    }
                    if(fileSize>=FILE_SIZE_LIMIT)
                    {
                        Toast.makeText(this,getString(R.string.error_file_size_too_large),Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            } else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }
            Intent recipientsIntent = new Intent(this,RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);

            String fileType;
            if(requestCode==CHOOSE_PHOTO_REQUEST||requestCode==TAKE_PHOTO_REQUEST)
            {
                fileType = ParseConstants.TYPE_IMAGE;
            }
            else
                fileType = ParseConstants.TYPE_VIDEO;

            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE,fileType);

            startActivity(recipientsIntent);
        }
        else if(resultCode !=RESULT_CANCELED)
        {
            Toast.makeText(this,getString(R.string.general_error),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId())
        {
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;

            case R.id.action_edit_friends:
                Intent intent = new Intent(this,EditFriendsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices,mDialogListener);
                builder.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
}
