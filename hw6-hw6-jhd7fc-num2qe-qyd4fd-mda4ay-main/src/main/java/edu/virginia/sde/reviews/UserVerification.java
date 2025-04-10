package edu.virginia.sde.reviews;

public class UserVerification {

    private UserVerification() {}

    private static UserVerification userVerification;
    private static int userId = 0;

    public static UserVerification getUserVerification() {
        if (userVerification == null) {
            return new UserVerification();
        }
        return userVerification;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userid) {
        userId = userid;
    }

    public Boolean isUserLoggedIn() {
        return userId != 0;
    }

    public void logOutUser() {
        setUserId(0);
    }

}
