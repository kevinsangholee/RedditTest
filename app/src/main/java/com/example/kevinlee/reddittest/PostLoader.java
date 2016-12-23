package com.example.kevinlee.reddittest;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by kevinlee on 12/21/16.
 */

public class PostLoader extends AsyncTaskLoader<ArrayList<RedditPost>> {

    private String url;
    private int upvoteThreshold;

    public PostLoader(Context context, String url, int threshold) {
        super(context);
        this.url = url;
        upvoteThreshold = threshold;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<RedditPost> loadInBackground() {
        return QueryUtils.extractRedditPosts(url, upvoteThreshold);
    }
}
