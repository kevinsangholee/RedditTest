package com.example.kevinlee.reddittest;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by kevinlee on 12/20/16.
 */

public class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    public static ArrayList<RedditPost> extractRedditPosts(String JSON_URL, int threshold) {
        URL url = createURL(JSON_URL);

        String JSONResponse = null;
        try {
            JSONResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        ArrayList<RedditPost> posts = new ArrayList<RedditPost>();
        try {
            JSONObject root = new JSONObject(JSONResponse);
            JSONObject data = root.optJSONObject("data");
            JSONArray children = data.optJSONArray("children");
            for(int i = 0; i < children.length(); i++) {
                JSONObject currentPost = children.optJSONObject(i);
                JSONObject deeperData = currentPost.optJSONObject("data");
                String author = deeperData.getString("author");
                int score = deeperData.getInt("score");
                String title = deeperData.getString("title");
                int comments = deeperData.getInt("num_comments");
                String postURL = deeperData.getString("url");
                String commentsURL = "https://www.reddit.com" + deeperData.getString("permalink");
                if(score >= threshold) {
                    posts.add(new RedditPost(title, author, score, comments, postURL, commentsURL));
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing JSON results", e);
        }

        return posts;
    }

    /**
     * Creates a URL with given JSON website address.
     * @param stringURL
     * @return URL
     */
    private static URL createURL(String stringURL) {
        URL url = null;
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String JSONResponse = "";
        if (url == null) {
            return JSONResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                JSONResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving reddit posts", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return JSONResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}
