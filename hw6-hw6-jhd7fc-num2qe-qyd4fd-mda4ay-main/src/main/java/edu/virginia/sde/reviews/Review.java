package edu.virginia.sde.reviews;

import java.sql.Timestamp;

public class Review {
    private Integer courseID;
    private Integer UserID;
    private String comment;
    private Integer courseRating;
    private String TimeStamp;

    public Review(Integer courseID, Integer userID, String comment, Integer courseRating, String timeStamp) {
        this.courseID = courseID;
        this.UserID = userID;
        this.comment = comment;
        this.courseRating = courseRating;
        this.TimeStamp = timeStamp;
    }

    public Integer getCourseID() {
        return courseID;
    }

    public void setCourseID(Integer courseID) {
        this.courseID = courseID;
    }

    public Integer getUserID() {
        return UserID;
    }

    public void setUserID(Integer userID) {
        UserID = userID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getCourseRating() {
        return courseRating;
    }

    public void setCourseRating(Integer courseRating) {
        this.courseRating = courseRating;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }
}
