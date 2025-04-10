package edu.virginia.sde.reviews;

public class CourseName {
    private static String name;
    private static String subject;
    private static String title;
    private static int number;

    public static String getName() {
        return name;
    }

    public static String getSubject() {
        return subject;
    }

    public static String getTitle() {
        return title;
    }

    public static int getNumber() {
        return number;
    }

    public static void setName(String subjectName, String titleName, int courseNumber) {
        subject = subjectName; title = titleName; number = courseNumber;
        name = subjectName + " " + courseNumber + ": " + titleName;
    }
}
