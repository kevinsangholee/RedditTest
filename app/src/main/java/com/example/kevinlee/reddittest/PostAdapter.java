package com.example.kevinlee.reddittest;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kevinlee on 12/20/16.
 */

public class PostAdapter extends ArrayAdapter<RedditPost> {

    public PostAdapter(Activity context, ArrayList<RedditPost> posts) {
        super(context, 0, posts);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        RedditPost currentPost = getItem(position);

        TextView scoreTextView = (TextView) listItemView.findViewById(R.id.scoreTextView);
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.titleTextView);
        TextView userTextView = (TextView) listItemView.findViewById(R.id.userTextView);
        TextView commentsTextView = (TextView) listItemView.findViewById(R.id.commentsTextView);

        int score = currentPost.getScore();
        scoreTextView.setText(Integer.toString(score));

        String title = currentPost.getTitle();
        titleTextView.setText(title);

        String user = currentPost.getUser();
        userTextView.setText("submitted by " + user);

        int comments = currentPost.getComments();
        commentsTextView.setText(comments + " comments");

        return listItemView;
    }
}
