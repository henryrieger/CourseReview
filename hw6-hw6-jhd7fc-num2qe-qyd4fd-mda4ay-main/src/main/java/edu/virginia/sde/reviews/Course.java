package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Course {
    private final SimpleStringProperty subject;
    private final SimpleStringProperty title;
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty rating;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Course(String subject, String title, int number, int id) {
        this.id = id;
        this.subject = new SimpleStringProperty(subject);
        this.title = new SimpleStringProperty(title);
        this.number = new SimpleIntegerProperty(number);
        this.rating = null;

    }

    public Course(String subject, String title, int number, String avgRating, int id) {
        this.id = id;
        this.subject = new SimpleStringProperty(subject);
        this.title = new SimpleStringProperty(title);
        this.number = new SimpleIntegerProperty(number);
        this.rating = new SimpleStringProperty(avgRating);
    }

    public String getCourseSubject() { return subject.get(); }
    public SimpleStringProperty subjectProperty() { return subject; }

    public String getCourseTitle() { return title.get(); }
    public SimpleStringProperty titleProperty() { return title; }

    public int getCourseNumber() { return number.get(); }
    public SimpleIntegerProperty numberProperty() { return number; }

    public String getRating() { return rating.get(); }
    public SimpleStringProperty ratingProperty() { return rating; }



    @Override
    public String toString() {
        return "Course{" +
                "subject=" + subject +
                ", title=" + title +
                ", number=" + number +
                '}';
    }
}
