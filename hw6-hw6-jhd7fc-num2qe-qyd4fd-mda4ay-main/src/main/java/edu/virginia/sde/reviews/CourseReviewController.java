package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseReviewController {
    @FXML
    private Label errorLabel;
    @FXML
    private Label courseTitleLabel;
    @FXML
    private Label averageRating;
    @FXML
    private ToggleGroup ratingGroup;
    @FXML
    private TextField review;
    @FXML
    private Label saveWithoutComplete;
    @FXML
    private TableView<Review> reviewTableView;
    @FXML
    private TableColumn<Review, Integer> ratingColumn;

    @FXML
    private TableColumn<Review, String> reviewColumn;

    @FXML
    private TableColumn<Review, Timestamp> dateColumn;
    @FXML
    private Integer rating;
    private String reviewText;
    private int courseID;

    Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) throws SQLException {
        this.primaryStage = primaryStage;
    }
    @FXML
    public void initialize() throws SQLException {
        courseTitleLabel.setText(CourseName.getName());
        setAverageScore();
        Optional<Review> previousReview = alreadyHasReview();
        previousReview.ifPresent(this::preSelectReview);
        fillTable();
    }
    @FXML
    private void preSelectReview(Review curReview) {
        for (Toggle toggle : ratingGroup.getToggles()) {
            if (toggle instanceof RadioButton radioButton) {
                if (radioButton.getText().equals(curReview.getCourseRating().toString())) {
                    ratingGroup.selectToggle(radioButton);
                    break;
                }
            }
        }
        review.setText(curReview.getComment());
    }
    @FXML
    public Optional<Review> alreadyHasReview() throws SQLException {
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        Optional<Review> review = databaseDriver.getUserReview(UserVerification.getUserVerification().getUserId(), courseID);
        databaseDriver.disconnect();
        return review;
    }
    public void fillTable() {
        //error with ratingColumn
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("courseRating"));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("TimeStamp"));

        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        ObservableList<Review> tableReviews = FXCollections.observableArrayList();
        List<Review> allReviews = new ArrayList<>();

        try {
            allReviews = databaseDriver.getReviews(courseID);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        for (Review reviews: allReviews){
            tableReviews.add(new Review(reviews.getCourseID(), reviews.getUserID(), reviews.getComment(), reviews.getCourseRating(), reviews.getTimeStamp()));
        }
        reviewTableView.setItems(tableReviews);
        try {
            databaseDriver.disconnect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
    }

    @FXML
    public void handleErase() {
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
            databaseDriver.removeReview(UserVerification.getUserVerification().getUserId(), courseID);
            databaseDriver.commit();
            databaseDriver.disconnect();
            initialize();
        } catch (SQLException e) {
            //TO DO
        }
    }
    @FXML
    public void setAverageScore() throws SQLException {
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            //TO DO
        }
        List<Course> courses = databaseDriver.searchCourses(CourseName.getSubject(), CourseName.getTitle(), String.valueOf(CourseName.getNumber()));
        Course curCourse = courses.get(0);
        int id = databaseDriver.getCourseId(curCourse);
        courseID = id;
        double average = databaseDriver.getAverageReviewScore(id);
        if (average < 0) {
            averageRating.setText("Average Rating: N/A");
        } else {
            averageRating.setText(String.format("Average Rating: %.2f", average));
        }
        databaseDriver.disconnect();
    }

    @FXML
    public void handleRatingScore() throws SQLException {
        RadioButton selectedButton = (RadioButton) ratingGroup.getSelectedToggle();
        if (selectedButton != null) {
            String selectedButtonText = selectedButton.getText();
            rating = Integer.parseInt(selectedButtonText);
        } else {
            saveWithoutComplete.setText("Please add a rating for the course");
            initialize();
        }
    }

    @FXML
    public void handleReviewText() {
        reviewText = review.getText();
    }

    @FXML
    public void handleSave() throws SQLException {
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            //TO DO
        }
        handleReviewText(); handleRatingScore();
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        String time = curTime.toString();
        try {
            databaseDriver.insertReview(databaseDriver.nextPrimaryReviewID(), courseID, UserVerification.getUserVerification().getUserId(), reviewText, rating, time);
            saveWithoutComplete.setText("");
            databaseDriver.commit(); databaseDriver.disconnect();
        } catch (SQLException e) {
            //to do
        } catch (NullPointerException e) {
            databaseDriver.disconnect();
        }
        initialize();
    }

    public void switchToCoursePage() {
        try {
            var fxmlLoader = new FXMLLoader(CourseSearchController.class.getResource("course-search.fxml"));
            Parent root = fxmlLoader.load();
            Scene secondScene = new Scene(root);
            Stage stage = (Stage) reviewTableView.getScene().getWindow();
            stage.setScene(secondScene);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("IO Exception: " + e.getMessage());
        }
    }
}
