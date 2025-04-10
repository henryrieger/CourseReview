package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyReviewsController {

    @FXML
    private Label errorLabel;

    @FXML
    private TableView<ExtendedReview> MyReviewTable;

    @FXML
    private TableColumn<ExtendedReview, String> subjColumn;

    @FXML
    private TableColumn<ExtendedReview, Integer> courseNumColumn;

    @FXML
    private TableColumn<Review, Integer> ratingColumn;

    @FXML
    private TableColumn<Review, String> reviewColumn;

    @FXML
    private TableColumn<Review, Timestamp> dateColumn;

    @FXML
    private Integer rating;
    private String reviewText;
    private int userID;

    Stage primaryStage;

    private final DatabaseDriver databaseDriver = new DatabaseDriver("reviews.sqlite");


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void initialize() throws SQLException {
        fillTable();

        MyReviewTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showReviewScene(newSelection);
            }
        });
    }


    public void fillTable() {
        userID = UserVerification.getUserVerification().getUserId();

        //error with ratingColumn
        subjColumn.setCellValueFactory(new PropertyValueFactory<>("subj"));
        courseNumColumn.setCellValueFactory(new PropertyValueFactory<>("courseNum"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("courseRating"));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("TimeStamp"));
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        ObservableList<ExtendedReview> tableReviews = FXCollections.observableArrayList();
        List<Review> allReviews = new ArrayList<>();

        try {
            allReviews = databaseDriver.getReviewsByUser(userID);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        try {
            for (Review review : allReviews) {
                int courseID = review.getCourseID();
                String subj = databaseDriver.getCourseSubjFromCourseID(courseID);
                int courseNum = databaseDriver.getCourseNumFromCourseID(courseID);
                String courseTitle = databaseDriver.getCourseTitleFromCourseID(courseID);

                tableReviews.add(new ExtendedReview(courseID, review.getUserID(), review.getComment(), review.getCourseRating(), review.getTimeStamp(), subj, courseNum, courseTitle));
            }
        } catch(SQLException e){
            e.printStackTrace();
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        MyReviewTable.setItems(tableReviews);
        try {
            databaseDriver.disconnect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
    }




    private void showReviewScene(ExtendedReview selectedCourse) {
        try {
            CourseName.setName(selectedCourse.getSubj(), selectedCourse.getCourseTitle(), selectedCourse.getCourseNum());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("course-review.fxml"));
            Parent root = loader.load();
            Scene secondScene = new Scene(root);
            Stage stage = (Stage) MyReviewTable.getScene().getWindow();
            stage.setScene(secondScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void switchToCoursePage() {
        try {
            var fxmlLoader = new FXMLLoader(CourseSearchController.class.getResource("course-search.fxml"));
            Parent root = fxmlLoader.load();
            Scene secondScene = new Scene(root);
            Stage stage = (Stage) MyReviewTable.getScene().getWindow();
            stage.setScene(secondScene);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("IO Exception: " + e.getMessage());
        }
    }


}
