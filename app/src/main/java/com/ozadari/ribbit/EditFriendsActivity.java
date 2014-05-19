package com.ozadari.ribbit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class EditFriendsActivity extends ListActivity {

    private static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected  List<ParseUser> mUsers;

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
