package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseDriver {
    private final String sqliteFilename;
    private Connection connection;

    public DatabaseDriver(String database_name) {
        this.sqliteFilename = database_name;
    }

    public Boolean isConnectionNull() {
        return connection == null;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The connection is already opened");
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilename);
        //the next line enables foreign key enforcement - do not delete/comment out
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        //the next line disables auto-commit - do not delete/comment out
        connection.setAutoCommit(false);
    }

    /**
     * Commit all changes since the connection was opened OR since the last commit/rollback
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Rollback to the last commit, or when the connection was opened
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Ends the connection to the database
     */
    public void disconnect() throws SQLException {
        connection.close();
    }

    /**
     * Creates the three database tables Users, Reviews, and Courses
     * foreign keys, if they do not exist already. If they already exist, this method does nothing.
     * Reviews is a relational database of courses, and users and as such these
     * fields are foriegn keys
     *
     * @throws SQLException
     */
    public void createTables() throws SQLException {

        String createUsersTableString = "CREATE TABLE IF NOT EXISTS Users ("
                + "ID INTEGER PRIMARY KEY, "
                + "UserName TEXT UNIQUE NOT NULL, "
                + "Password TEXT NOT NULL "
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createUsersTableString);
        } catch (SQLException e) {
            throw e;
        }

        String createCoursesTableString = "CREATE TABLE IF NOT EXISTS Courses ("
                + "ID INTEGER PRIMARY KEY, "
                + "CourseTitle TEXT NOT NULL, "
                + "CourseSubject TEXT NOT NULL, "
                + "CourseNumber INTEGER NOT NULL"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createCoursesTableString);
        } catch (SQLException e) {
            throw e;
        }

        String createReviewsTableString = "CREATE TABLE IF NOT EXISTS Reviews ("
                + "ID INTEGER PRIMARY KEY, "
                + "CourseID INTEGER NOT NULL, "
                + "UserID INTEGER NOT NULL, "
                + "Comment TEXT,"
                + "CourseRating INTEGER,"
                + "TimeStamp TEXT NOT NULL,"
                + "FOREIGN KEY (CourseID) REFERENCES Courses(ID),"
                + "FOREIGN KEY (UserID) REFERENCES Users(ID)"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createReviewsTableString);
        } catch (SQLException e) {
            throw e;
        }
    }

    public Boolean isCoursesEmpty() throws SQLException {
        String areTablesEmptyQuery = "Select COUNT(*) from Courses";
        try (PreparedStatement statement = connection.prepareStatement(areTablesEmptyQuery)) {
            var resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 0;
        } catch (SQLException e) {
            throw e;
        }
    }

    public Boolean isReviewsEmpty() throws SQLException {
        String areTablesEmptyQuery = "Select COUNT(*) from Reviews";
        try (PreparedStatement statement = connection.prepareStatement(areTablesEmptyQuery)) {
            var resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 0;
        } catch (SQLException e) {
            throw e;
        }
    }

    public Boolean isUsersEmpty() throws SQLException {
        String areTablesEmptyQuery = "Select COUNT(*) from Users";
        try (PreparedStatement statement = connection.prepareStatement(areTablesEmptyQuery)) {
            var resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 0;
        } catch (SQLException e) {
            throw e;
        }
    }

    public void populateTables() throws SQLException {

        if (isUsersEmpty()) {
            try {
                // Populate Users table
                insertUser(1, "brodie", "password");
                insertUser(2, "jonny", "iluvbrodie");
                insertUser(3, "henry", "iluvjonny");
                insertUser(4, "rashid", "iluvhenry");
            }  catch (SQLException e) {
                throw e;
            }
        }


        if (isCoursesEmpty()) {
            try {
                // Populate Courses table
                insertCourse(1, "sde", "cs", 3140);
                insertCourse(2, "computational mechanics", "mae", 3420);
                insertCourse(3, "systems capstone", "sys", 4055);
                insertCourse(4, "risk analysis", "sys", 4050);
            } catch (SQLException e) {
                throw e;
            }
        }

        if (isReviewsEmpty()) {
            try {
                // Populate Reviews table
                insertReview(1, 1, 1, "i like the teaching style", 5, "2023-12-04 15:10:16.835");
                insertReview(2, 2, 1, "i like streams", 4, "2023-12-04 15:10:16.835");
                insertReview(3, 3, 1, "i like coding", 4, "2023-12-04 15:10:16.835");
                insertReview(4, 4, 1, "too ez", 2, "2023-12-04 15:10:16.835");
                insertReview(5, 1, 2, "too hard", 2, "2023-12-04 15:10:16.835");
                insertReview(6, 1, 3, "too boring", 2, "2023-12-04 15:10:16.835");
                insertReview(7, 1, 4, "lorem ipsum...", 3, "2023-12-04 15:10:16.835");
            }  catch (SQLException e) {
                throw e;
            }
        }

    }

    private void insertUser(int id, String username, String password) throws SQLException {
        String insertUserQuery = "INSERT INTO Users (ID, UserName, Password) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertUserQuery)) {
            statement.setInt(1, id);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.executeUpdate();
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    private void insertCourse(int id, String title, String subject, int number) throws SQLException {
        String insertCourseQuery = "INSERT INTO Courses (ID, CourseTitle, CourseSubject, CourseNumber) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertCourseQuery)) {
            statement.setInt(1, id);
            statement.setString(2, title);
            statement.setString(3, subject);
            statement.setInt(4, number);
            statement.executeUpdate();
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    public int nextPrimaryReviewID() {
        String primaryQuery = "SELECT MAX(ID) AS LargestID FROM Reviews";
        try (PreparedStatement statement = connection.prepareStatement(primaryQuery);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                int largestID = resultSet.getInt("LargestID");
                return largestID + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void insertReview(int id, int courseId, int userId, String comment, int rating, String timeStamp) throws SQLException {
        removeReview(userId, courseId);
        String insertReviewQuery = "INSERT INTO Reviews (ID, CourseID, UserID, Comment, CourseRating, TimeStamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertReviewQuery)) {
            statement.setInt(1, id);
            statement.setInt(2, courseId);
            statement.setInt(3, userId);
            statement.setString(4, comment);
            statement.setInt(5, rating);
            statement.setString(6, timeStamp);
            statement.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            rollback();
            throw e;
        }
    }

    public void removeReview(int userID, int courseID) throws SQLException {
        String removeQuery = "DELETE FROM Reviews WHERE UserID = ? AND CourseID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(removeQuery)) {
            preparedStatement.setInt(1, userID);
            preparedStatement.setInt(2, courseID);
            int rowsDeleted = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            // Handle the exception
            e.printStackTrace();
            throw e;
        }
    }

    public int getCourseId(Course course) throws SQLException {
        String getAllStopsQuery = "SELECT ID FROM Courses WHERE CourseNumber = '" +
                course.getCourseNumber() + "' AND CourseSubject = '" +
                course.getCourseSubject() + "' AND CourseTitle = '" +
                course.getCourseTitle() + "'";
        var preparedStatement = connection.prepareStatement(getAllStopsQuery);
        var resultSet = preparedStatement.executeQuery();
        int id = 0;
        while (resultSet.next()) {
            id = resultSet.getInt("ID");
        }
        return id;
    }

    public boolean doesUsernameExist(String username) throws SQLException {
        String countUsernamesQuery = "select Count(*) from Users where Username = ?";
        try {
            var preparedStatement = connection.prepareStatement(countUsernamesQuery);
            preparedStatement.setString(1, username);
            var resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) >= 1;
        } catch (SQLException e) {
            throw e;
        }
    }

    public int getUserId(String username, String password) throws SQLException {
        String getUserIdQuery = "select id from Users where Username = ? and Password = ?";
        try {
            var preparedStatement = connection.prepareStatement(getUserIdQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            var resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void createNewUser(String username, String password) throws SQLException {
        String insertReviewQuery = "INSERT INTO Users (Username, Password) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertReviewQuery)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    public boolean isLoginValid(String username, String password) throws SQLException {
        String countUsernamesQuery = "select Count(*) from Users where Username = ? and Password = ?";
        try {
            var preparedStatement = connection.prepareStatement(countUsernamesQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            var resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 1;
        } catch (SQLException e) {
            throw e;
        }
    }

    public Optional<Review> getUserReview(int id, int curCourse) {
        String reviewQuery = "select * from Reviews where UserID = " + id + " and CourseID = " + curCourse;
        Optional<Review> review = Optional.empty();
        try {
            var preparedStatement = connection.prepareStatement(reviewQuery);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer courseID = resultSet.getInt("CourseID");
                Integer userID = resultSet.getInt("UserID");
                Integer rating = resultSet.getInt("CourseRating");
                String comment = resultSet.getString("Comment");
                String timeStamp = resultSet.getString("TimeStamp");
                review = Optional.of(new Review(courseID, userID, comment, rating, timeStamp));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return review;
    }

    public double getAverageReviewScore(int id) throws SQLException {
        String reviewsQuery = "select CourseRating from Reviews where CourseID = " + id;
        try {
            var preparedStatement = connection.prepareStatement(reviewsQuery);
            var resultSet = preparedStatement.executeQuery();
            double average = -1;
            int count = 0;
            while (resultSet.next()) {
                if (average < 0) {
                    average = 0;
                }
                average += resultSet.getDouble(1);
                count++;
            }
            return average / count;
        } catch (SQLException e) {
            throw e;
        }
    }

    /*
        public String getCourseName(int id) throws SQLException {
            String nameQuery = "select * from Courses where CourseID = " + id;
            try {
                var preparedStatement = connection.prepareStatement(nameQuery);
                var resultSet = preparedStatement.executeQuery();
                String title = resultSet.getString("CourseTitle");
                String subject = resultSet.getString("CourseSubject");
                String number = String.valueOf(resultSet.getInt("CourseNumber"));
                return subject + ' ' + number + ' ' + title;
            } catch (SQLException e) {
                throw e;
            }
        }
    */
    public List<Course> getCourses() throws SQLException {
        List<Course> data = FXCollections.observableArrayList();

        String SQL = "Select * from Courses";
        ResultSet resultSet;
        PreparedStatement statement = connection.prepareStatement(SQL);
        resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Integer id = resultSet.getInt("ID");
            String subject = resultSet.getString("CourseSubject");
            String title = resultSet.getString("CourseTitle");
            Integer number = resultSet.getInt("CourseNumber");
            Course row = new Course(subject, title, number, id);
            data.add(row);
        }
        return data;
    }

    public List<Review> getReviews(int id) throws SQLException {
        List<Review> data = FXCollections.observableArrayList();

        String SQL = "Select * from Reviews where CourseID = " + id;
        ResultSet resultSet;
        PreparedStatement statement = connection.prepareStatement(SQL);
        resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Integer courseID = resultSet.getInt("CourseID");
            Integer userID = resultSet.getInt("UserID");
            Integer rating = resultSet.getInt("CourseRating");
            String comment = resultSet.getString("Comment");
            String timeStamp = resultSet.getString("TimeStamp");
            Review row = new Review(courseID, userID, comment, rating, timeStamp);
            data.add(row);
        }
        return data;
    }

    public void createNewCourse(Course newCourse) throws SQLException {
        String createCourseQuery = "INSERT INTO Courses (CourseTitle, CourseSubject, CourseNumber) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(createCourseQuery)) {
            statement.setString(1, newCourse.getCourseTitle());
            statement.setString(2, newCourse.getCourseSubject());
            statement.setInt(3, (int) newCourse.getCourseNumber());
            statement.executeUpdate();
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    public List<Course> searchCourses(String subject, String title, String number) throws SQLException {
        List<Course> data = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Courses WHERE "
                + "UPPER(CourseSubject) LIKE UPPER(?) AND "
                + "( ? = '' OR CourseNumber = ? ) AND "
                + "UPPER(CourseTitle) LIKE UPPER(?)";


        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, subject.isEmpty() ? "%" : "%" + subject + "%");
        statement.setString(4, title.isEmpty() ? "%" : "%" + title + "%");

        if (number.isEmpty()) {
            statement.setString(2, "");
            statement.setInt(3, 0);  // The actual value doesn't matter since the condition will be ignored
        } else {
            statement.setString(2, "not empty");  // Any non-empty string
            statement.setInt(3, Integer.parseInt(number));  // Convert the number string to an integer
        }
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Integer id = resultSet.getInt("ID");
            String foundSubject = resultSet.getString("CourseSubject");
            String foundTitle = resultSet.getString("CourseTitle");
            Integer foundNumber = resultSet.getInt("CourseNumber");
            if (subject.isEmpty() || foundSubject.toUpperCase().equals(subject.toUpperCase())) {
                Course row = new Course(foundSubject, foundTitle, foundNumber, id);
                data.add(row);
            }

        }
        return data;
    }

    public List<Review> getReviewsByUser(int userId) throws SQLException {
        List<Review> data = FXCollections.observableArrayList();

        String SQL = "Select * from Reviews where UserID = " + userId;
        ResultSet resultSet;
        PreparedStatement statement = connection.prepareStatement(SQL);
        resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Integer courseID = resultSet.getInt("CourseID");
            Integer userID = resultSet.getInt("UserID");
            Integer rating = resultSet.getInt("CourseRating");
            String comment = resultSet.getString("Comment");
            String timeStamp = resultSet.getString("TimeStamp");
            Review row = new Review(courseID, userID, comment, rating, timeStamp);
            data.add(row);
        }
        return data;
    }

    public String getCourseSubjFromCourseID(int courseID) throws SQLException {
        String reviewsQuery = "select CourseSubject from Courses where ID = " + courseID;

        try {
            var preparedStatement = connection.prepareStatement(reviewsQuery);
            var courseSubj = preparedStatement.executeQuery();
            return courseSubj.getString("CourseSubject");
        } catch (SQLException e) {
            throw e;
        }

    }

    public Integer getCourseNumFromCourseID(int courseID) throws SQLException {
        String reviewsQuery = "select CourseNumber from Courses where ID = " + courseID;

        try {
            var preparedStatement = connection.prepareStatement(reviewsQuery);
            var courseNum = preparedStatement.executeQuery();
            return courseNum.getInt("CourseNumber");
        } catch (SQLException e) {
            throw e;
        }

    }

    public String getCourseTitleFromCourseID(int courseID) throws SQLException {
        String reviewsQuery = "select CourseTitle from Courses where ID = " + courseID;

        try {
            var preparedStatement = connection.prepareStatement(reviewsQuery);
            var courseTitle = preparedStatement.executeQuery();
            return courseTitle.getString("CourseTitle");
        } catch (SQLException e) {
            throw e;
        }
    }
}