package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.SQLException;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;


public class LogInController {

    @FXML
    private Label verificationTitleLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Hyperlink toggleLabel;

    private Boolean assumeLogin = true;

    Stage primaryStage;

    @FXML
    private void initialize() {
        // Set up event handler for Enter key press
        usernameField.setOnKeyPressed(this::handleEnterKey);
        passwordField.setOnKeyPressed(this::handleEnterKey);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void toggleLogInSignUp() {
        assumeLogin = !assumeLogin;
        if (assumeLogin) {
            verificationTitleLabel.setText("Log In");
            toggleLabel.setText("Sign Up");
        } else {
            verificationTitleLabel.setText("Sign Up");
            toggleLabel.setText("Log In");
        }
        clearLogInFields();
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSubmit();
        }
    }

    public void handleSubmit() {
        var databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        String username = usernameField.getText();
        String password = passwordField.getText();
        Boolean verificationSuccess = false;
        if (assumeLogin) {
            verificationSuccess = handleLogin(databaseDriver, username, password);
        } else {
            verificationSuccess = handleSignUp(databaseDriver, username, password);
        }

        if (!verificationSuccess) {
            try {
                databaseDriver.rollback();
                databaseDriver.disconnect();
            } catch (SQLException e) {
                errorLabel.setText("SQL error: " + e.getMessage());
            }
        }

        if (verificationSuccess) {
            int userId = getUserId(databaseDriver, username, password);
            UserVerification userVerification = UserVerification.getUserVerification();
            userVerification.setUserId(userId);
            if (assumeLogin) {
                switchToCoursePage();
            } else {
                toggleLogInSignUp();
            }
            try {
                databaseDriver.commit();
                databaseDriver.disconnect();
            } catch (SQLException e) {
                errorLabel.setText("SQL error: " + e.getMessage());
            }
        }

    }

    public void clearLogInFields() {
        passwordField.setText("");
        usernameField.setText("");
    }

    public void switchToCoursePage() {
        try {
            var fxmlLoader = new FXMLLoader(CourseSearchController.class.getResource("course-search.fxml"));
            var newScene = new Scene(fxmlLoader.load());
            primaryStage.setScene(newScene);
            primaryStage.show();
        } catch (IOException e) {
            errorLabel.setText("IO Exception: " + e.getMessage());
        }
    }

    public Boolean handleLogin(DatabaseDriver databaseDriver, String username, String password) {
        if(isLoginValid(databaseDriver, username, password)) {
            return true;
        } else {
            errorLabel.setText("Username and password combination is incorrect");
            return false;
        }
    }

    public Boolean handleSignUp(DatabaseDriver databaseDriver, String username, String password) {
        if (doesUsernameExist(databaseDriver, username)) {
            errorLabel.setText("Username already exists");
            return false;
        }
        if (!isPasswordValid(password)) {
            errorLabel.setText("Password must be at least 8 characters");
            return false;
        }
        if (username == "") {
            errorLabel.setText("Username cannot be empty");
            return false;
        }
        createNewUser(databaseDriver, username, password);
        return true;
    }

    private Boolean isLoginValid(DatabaseDriver databaseDriver, String username, String password) {
        try {
            return databaseDriver.isLoginValid(username, password);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        return false;
    }

    public void createNewUser(DatabaseDriver databaseDriver, String username, String password) {
        try {
            databaseDriver.createNewUser(username, password);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
    }

    public int getUserId(DatabaseDriver databaseDriver, String username, String password) {
        int userId = 0;
        try {
            userId = databaseDriver.getUserId(username, password);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        return userId;
    }

    private Boolean doesUsernameExist(DatabaseDriver databaseDriver, String username) {
        Boolean usernameExists = false;
        try {
            usernameExists = databaseDriver.doesUsernameExist(username);
        } catch (SQLException e) {
            errorLabel.setText("SQL error: " + e.getMessage());
        }
        return usernameExists;
    }

    private Boolean isPasswordValid(String password) {
        if (password.length() >= 8) {
            return true;
        }
        return false;
    }

    @FXML
    private void closeApplication() {
        Platform.exit();
        System.exit(0);
    }

}
