package com.ozadari.ribbit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends ListActivity {

    private static final String TAG = RecipientsActivity.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected MenuItem mSendMenuItem;
    protected Uri mMediaUri;
    protected String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mMediaUri = getIntent().getData();
        mFileType=getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
    }


    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                mFriends = parseUsers;

                if(e==null) {
                    String[] usernames = new String[mFriends.size()];
                    int i = 0;

                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(), android.R.layout.simple_list_item_checked, usernames);
                    setListAdapter(adapter);
                }
                else
                {
                    Log.e(TAG, e.getMessage().toString());
                    AlertDialog.Builder alert = new AlertDialog.Builder(RecipientsActivity.this);
                    alert.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(R.string.ok, null);
                    alert.create().show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipients,menu);
        mSendMenuItem= menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_send:
                ParseObject message = createMessage();
                if(message == null)
                {
                    //Error
                    new AlertDialog.Builder(this).setMessage(R.string.error_selecting_file).setTitle(R.string.error_selecting_file_title)
                                                 .setPositiveButton(R.string.ok, null).create().show();
                }
                else
                {
                    send(message);
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //success!
                if(e==null)
                    Toast.makeText(RecipientsActivity.this, getString(R.string.success_message), Toast.LENGTH_LONG).show();
                else
                    new AlertDialog.Builder(RecipientsActivity.this).setMessage(R.string.error_sending_message).setTitle(R.string.error_selecting_file_title)
                            .setPositiveButton(R.string.ok, null).create().show();
            }
        });
    }

    protected ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);

        message.put(ParseConstants.KEY_SENDER_ID,ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME,ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS,getRecipientsIds());
        message.put(ParseConstants.KEY_FILE_TYPE,mFileType);

        byte[] fileBytes=FileHelper.getByteArrayFromFile(this,mMediaUri);
        if(fileBytes==null)
        {
            return null;
        }
        else {
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }
            String fileName = FileHelper.getFileName(this,mMediaUri,mFileType);
            ParseFile file = new ParseFile(fileName,fileBytes);
            message.put(ParseConstants.KEY_FILE,file);
            return message;
        }
    }

    private ArrayList<String> getRecipientsIds() {
        ArrayList<String> recipientsIds = new ArrayList<String>();
        for(int i=0;i<getListView().getCount();i++)
        {
            if(getListView().isItemChecked(i))
            {
                recipientsIds.add(mFriends.get(i).getObjectId());
            }
        }
        return  recipientsIds;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(l.getCheckedItemCount()>0)
            mSendMenuItem.setVisible(true);
        else
            mSendMenuItem.setVisible(false);
    }
}
