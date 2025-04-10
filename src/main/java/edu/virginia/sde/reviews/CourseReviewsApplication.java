package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;

public class CourseReviewsApplication extends Application {
    public static void main(String[] args) throws SQLException {

        launch(args);

    }

    @Override
    public void start(Stage stage) throws Exception {

        DatabaseDriver databaseDriver = new DatabaseDriver("reviews.sqlite");
        try {
            databaseDriver.connect();
            databaseDriver.createTables();
//            databaseDriver.populateTables(); // <----
        } catch (SQLException e) {
            throw e;
        }

        try {
            databaseDriver.commit();
            databaseDriver.disconnect();
        } catch (SQLException e) {
            throw e;
        }

        URL view = CourseReviewsApplication.class.getResource("log-in.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(view);
        Scene root = new Scene(fxmlLoader.load());
        stage.setScene(root);

        var controller = (LogInController) fxmlLoader.getController();
        controller.setPrimaryStage(stage);
        stage.setTitle("Reviews App");

        stage.show();
    }
}

