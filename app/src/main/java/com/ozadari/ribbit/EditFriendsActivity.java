package com.ozadari.ribbit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendsActivity extends ListActivity {

    private static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected  List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_friends);
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //sort in aacsening user name
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if(e==null)
                {
                    //Succsess
                    mUsers = parseUsers;
                    String[] usernames = new String[mUsers.size()];
                    int i = 0;

                    for(ParseUser user: mUsers)
                    {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter =new ArrayAdapter<String>(EditFriendsActivity.this,android.R.layout.simple_list_item_checked,usernames);
                    setListAdapter(adapter);
                    checkFriends();

                }
                else
                {
                    Log.e(TAG, e.getMessage().toString());
                    AlertDialog.Builder alert = new AlertDialog.Builder(EditFriendsActivity.this);
                    alert.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(R.string.ok, null);
                    alert.create().show();
                }
            }
        });
    }

    private void checkFriends() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(e==null)
                {
                    //list returned - look for a match
                    for(int i = 0; i<mUsers.size();i++)
                    {
                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend : parseUsers) {
                            if (friend.getObjectId().equals(user.getObjectId()))
                            {
                                getListView().setItemChecked(i,true);
                            }
                        }
                    }
                }
                else
                {
                    Log.e(TAG,e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l,View v,int position,long id)
    {
        CheckedTextView textView = (CheckedTextView)v;
        if(!textView.isChecked()) {
            //adding friend!
            textView.setChecked(true);
            super.onListItemClick(l, v, position, id);
            mFriendsRelation.add(mUsers.get(position));
        }
        else {
            //Here we need to delete friend, meanwhile i jusst uncheck it:
            ((CheckedTextView) v).setChecked(false);
            mFriendsRelation.remove(mUsers.get(position));
        }
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                    Log.e(TAG, e.getMessage());
            }
        });
    }
}
