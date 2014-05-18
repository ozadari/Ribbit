package com.ozadari.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Oz Adari on 18/05/2014.
 */
public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "NIi4hV0NyZ7CpE0GoYhDadv9KyriHToFbYkHhJDa", "VBWN6LJPBTtqZY1YUfnLjmN4QYsTJDEuoM4KKNsJ");

    }

}
