package edu.virginia.sde.reviews;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;

import java.awt.*;

public class ExtendedReview extends Review {
    private final SimpleStringProperty subj;
    private final SimpleIntegerProperty courseNum;
    private final SimpleStringProperty courseTitle;

    public ExtendedReview(Integer courseID, Integer userID, String comment, Integer courseRating, String timeStamp, String subj, Integer courseNum, String courseTitle) {
        super(courseID, userID, comment, courseRating, timeStamp);
        this.subj = new SimpleStringProperty(subj);
        this.courseNum = new SimpleIntegerProperty(courseNum);
        this.courseTitle = new SimpleStringProperty(courseTitle);
    }

    public String getSubj() {
        return subj.get();
    }

    public int getCourseNum() {
        return courseNum.get();
    }

    public String getCourseTitle() {
        return courseTitle.get();
    }
}



