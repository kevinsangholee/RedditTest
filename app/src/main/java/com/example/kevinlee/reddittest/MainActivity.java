package com.example.kevinlee.reddittest;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static android.view.View.GONE;

/**
 * TODO:
 * Add long press to reach comments
 * Add preferences section to sort by new/hot/top/number of posts/score threshold/etc.
 */

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<RedditPost>> {
    // String for url to visit
    private final String JSON_DEFAULT_URL = "https://www.reddit.com/r/pics";

    // Instance variables for elements
    private ListView postListView;
    private PostAdapter adapter;
    private TextView emptyView;
    private ProgressBar progressBar;
    private TextView indicatorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        emptyView = (TextView) findViewById(R.id.emptyView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        postListView = (ListView) findViewById(R.id.list);
        adapter = new PostAdapter(this, new ArrayList<RedditPost>());
        indicatorTextView = (TextView) findViewById(R.id.indicatorTextView);

        // Set the adapter
        postListView.setAdapter(adapter);
        // Set clicking on posts to link to reddit post
        postListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RedditPost current = adapter.getItem(position);
                String url = current.getLink();
                Uri webpage = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });
        postListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                RedditPost current = adapter.getItem(position);
                String commentsUrl = current.getCommentsLink();
                Uri webpage = Uri.parse(commentsUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
                return true;
            }
        });

        // Checks to see if there is an internet connection. If yes, continue with loading. If not, display no internet connection message.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().initLoader(0, null, this);
        } else {
            progressBar.setVisibility(GONE);
            emptyView.setText("No internet connection.");
        }

        // Lets refresh happen on swipe down
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAdapter();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    /**
     * On the loader creation, create a new post loader with current json url.
     * @param i
     * @param bundle
     * @return the Loader with posts
     */
    @Override
    public Loader<ArrayList<RedditPost>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String sortBy = sharedPrefs.getString(getString(R.string.settings_sort_by_key),
                getString(R.string.settings_sort_by_default));

        String minUpvote = sharedPrefs.getString(getString(R.string.settings_min_upvote_key),
                getString(R.string.settings_min_upvote_default));

        String JSONurl = sharedPrefs.getString("JSON_URL", JSON_DEFAULT_URL);
        String currentSub = sharedPrefs.getString("currentSubreddit", "pics");
        indicatorTextView.setText("/r/" + currentSub);

        Uri baseUri = Uri.parse(JSONurl);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        if(sortBy.equals(getString(R.string.settings_sort_by_top_day_value))
            || sortBy.equals(getString(R.string.settings_sort_by_top_hour_value))
            || sortBy.equals(getString(R.string.settings_sort_by_top_week_value))
            || sortBy.equals(getString(R.string.settings_sort_by_top_month_value))
            || sortBy.equals(getString(R.string.settings_sort_by_top_year_value))
            || sortBy.equals(getString(R.string.settings_sort_by_top_alltime_value))) {
            uriBuilder.appendPath("top");
            uriBuilder.appendPath(".json");
            uriBuilder.appendQueryParameter("t", sortBy);
        } else {
            uriBuilder.appendPath(sortBy);
            uriBuilder.appendPath(".json");
        }

        return new PostLoader(this, uriBuilder.toString(), Integer.parseInt(minUpvote));
    }

    /**
     * When load is finished, remove all buffers and add all the elements to the adapter. If there are
     * no posts or something went wrong, indicate that no posts were found.
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<RedditPost>> loader, ArrayList<RedditPost> data) {
        adapter.clear();
        progressBar.setVisibility(GONE);

        if(data != null && !data.isEmpty()) {
            emptyView.setVisibility(GONE);
            adapter.addAll(data);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No posts found.");
        }
    }

    /**
     * Clear the adapter once the loader has been reset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<ArrayList<RedditPost>> loader) {
        adapter.clear();
    }

    /**
     * Method for the search button click. Finds new posts with given subreddit.
     * @param v
     */
    public void searchSubreddit(View v) {
        EditText searchEditText = (EditText) findViewById(R.id.searchEditText);
        String subreddit = searchEditText.getText().toString();
        String JSONurl = "https://reddit.com/r/" + subreddit;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("JSON_URL", JSONurl).commit();
        editor.putString("currentSubreddit", subreddit).commit();
        indicatorTextView.setText("/r/" + subreddit);
        adapter.clear();
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(GONE);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
        refreshAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshAdapter() {
        getLoaderManager().restartLoader(0, null, this);
    }


}
