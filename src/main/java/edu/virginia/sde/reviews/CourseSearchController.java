package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.parseUnsignedInt;


public class CourseSearchController {

    @FXML
    private TableView<Course> coursesTableView;

    @FXML
    private TableColumn<Course, String> subjectColumn;

    @FXML
    private TableColumn<Course, Integer> numberColumn;

    @FXML
    private TableColumn<Course, String> titleColumn;

    @FXML
    public TableColumn<Course, Double> avgColumn;

    @FXML
    private TextField subjectField;

    @FXML
    private TextField numberField;

    @FXML
    private TextField titleField;

    @FXML
    private TextField newSubjectField;

    @FXML
    private TextField newNumberField;

    @FXML
    private TextField newTitleField;

    @FXML
    private Label errorLabel;


    Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        // Your initialization code here
        fillTable();
        coursesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showReviewScene(newSelection);
            }
        });
    }


    private void showReviewScene(Course selectedCourse) {
        try {
            CourseName.setName(selectedCourse.getCourseSubject(), selectedCourse.getCourseTitle(), selectedCourse.getCourseNumber());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("course-review.fxml"));
            Parent root = loader.load();

            Scene secondScene = new Scene(root);
            Stage stage = (Stage) coursesTableView.getScene().getWindow();
            stage.setScene(secondScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillTable() {
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        avgColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));


        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        ObservableList<Course> tableCourses = FXCollections.observableArrayList();
        List<Course> allCourses;
        List<Course> finalCourses = new ArrayList<>();
        String ratingText = "";

        try {
            allCourses = databaseDriver.getCourses();
            for (Course course: allCourses){
                double average = databaseDriver.getAverageReviewScore(course.getId());
                if (average < 0) {
                    ratingText = ("Average Rating: N/A");
                } else {
                    ratingText = String.format("%.2f", average);
                }
                finalCourses.add(new Course(course.getCourseSubject(), course.getCourseTitle(), course.getCourseNumber(), ratingText, course.getId()));
            }
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        for (Course courses: finalCourses){
            tableCourses.add(new Course(courses.getCourseSubject(), courses.getCourseTitle(), courses.getCourseNumber(), courses.getRating(), courses.getId()));
        }
        coursesTableView.setItems(tableCourses);
        try {
            databaseDriver.disconnect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
    }

    public void updateTable(List<Course> allCourses){
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        avgColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        List<Course> finalCourses = new ArrayList<>();
        String ratingText;
        try {
            databaseDriver.connect();
            for (Course course: allCourses){
                double average = databaseDriver.getAverageReviewScore(course.getId());
                if (average < 0) {
                    ratingText = ("Average Rating: N/A");
                } else {
                    ratingText = String.format("%.2f", average);
                }
                finalCourses.add(new Course(course.getCourseSubject(), course.getCourseTitle(), course.getCourseNumber(), ratingText, course.getId()));
            }
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }

        ObservableList<Course> tableCourses = FXCollections.observableArrayList();
        for (Course courses: finalCourses){
            tableCourses.add(new Course(courses.getCourseSubject(), courses.getCourseTitle(), courses.getCourseNumber(), courses.getRating(), courses.getId()));
        }

        coursesTableView.setItems(tableCourses);
        try {
            databaseDriver.disconnect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        String subject = subjectField.getText();
        String number = numberField.getText();
        String title = titleField.getText();
        searchCourses(databaseDriver, subject, title, number);
        try {
            databaseDriver.disconnect();
        } catch (SQLException e){
            errorLabel.setText("SQL error: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }

        String subject = newSubjectField.getText();
        String number = newNumberField.getText();
        String title = newTitleField.getText();

        if (subject.isEmpty() || number.isEmpty() || !title.isEmpty()) {
            errorLabel.setText("All Blanks Must Be Filled");
        }

        if (!subject.isEmpty() && !number.isEmpty() && !title.isEmpty()) {
            if (validateInput(subject, number, title)) {
                if (!courseExists(databaseDriver, subject, title, number)) {
                    errorLabel.setText("Added");
                    Course newCourse = new Course(subject.toUpperCase(), title, Integer.parseInt(number), "", 0);
                    createNewCourse(databaseDriver, newCourse);
                }
                else {
                    errorLabel.setText("Course Already Exists");
                }
            }
        }


        try {
            databaseDriver.commit();
            databaseDriver.disconnect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        fillTable();
    }

    @FXML
    private void handleMyReviews() {
        try {
            fillTable();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("my-reviews.fxml"));
            Parent root = loader.load();
            Scene secondScene = new Scene(root);
            Stage stage = (Stage) coursesTableView.getScene().getWindow();
            stage.setScene(secondScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("log-in.fxml"));
            Parent root = loader.load();
            Scene secondScene = new Scene(root);
            Stage stage = (Stage) coursesTableView.getScene().getWindow();
            stage.setScene(secondScene);

            var controller = (LogInController) loader.getController();
            controller.setPrimaryStage(stage);

            var userVerification = UserVerification.getUserVerification();
            userVerification.logOutUser();

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createNewCourse(DatabaseDriver databaseDriver, Course newCourse) {
        try {
            databaseDriver.createNewCourse(newCourse);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }

    }

    public void searchCourses(DatabaseDriver databaseDriver, String subject, String title, String number){
        try{
            List<Course> foundClasses = databaseDriver.searchCourses(subject, title, number);
            updateTable(foundClasses);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
    }
    public boolean courseExists(DatabaseDriver databaseDriver, String subject, String title, String number){
        List<Course> foundClasses = new ArrayList<Course>();
        try{
            foundClasses = databaseDriver.searchCourses(subject, title, number);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        return !foundClasses.isEmpty();
    }

    private boolean validateInput(String subject, String number, String title) {
        // Check Subject
        if (!subject.matches("[a-zA-Z]{2,4}")) {
            errorLabel.setText("Invalid Subject: Must be 2-4 letters.");
            return false;
        }

        // Check Number
        if (!number.matches("\\d{4}")) {
            errorLabel.setText("Invalid Number: Must be exactly 4 digits.");
            return false;
        }

        // Check Title
        if (title.length() < 1 || title.length() > 50) {
            errorLabel.setText("Invalid Title: Must be 1-50 characters long.");
            return false;
        }
        return true;
    }

}
