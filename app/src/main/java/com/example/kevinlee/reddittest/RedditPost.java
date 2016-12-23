package com.example.kevinlee.reddittest;

/**
 * Created by kevinlee on 12/20/16.
 */

public class RedditPost {

    private String title;
    private String user;
    private int score;
    private int comments;
    private String link;
    private String commentsLink;

    public RedditPost(String title, String user, int score, int comments, String link, String commentsLink) {
        this.title = title;
        this.user = user;
        this.score = score;
        this.comments = comments;
        this.link = link;
        this.commentsLink = commentsLink;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }

    public int getScore() {
        return score;
    }

    public int getComments() {
        return comments;
    }

    public String getLink() {
        return link;
    }

    public String getCommentsLink() {
        return commentsLink;
    }
}
