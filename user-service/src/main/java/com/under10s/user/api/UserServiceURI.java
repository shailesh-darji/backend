package com.under10s.user.api;

public class UserServiceURI {

    private UserServiceURI(){};

    public static final String URI_USER_SERVICE = "/user";
    public static final String URI_REGISER_USER = "/register";

    public static final String LOGOUT_URL = "/user/logout";

    public static final String URI_LOGIN = "/login";

    public static final String URI_FORGOT_PASSWORD_OTP = "/forgotPassword";

    public static final String URI_VALIDATE_OTP = "/validateOTP";

    public static final String URI_UPDATE_PASSWORD = "/updatePassword";

    public static final String URI_ERROR = "/error";

    public static final String LOGIN_URL = URI_USER_SERVICE + URI_LOGIN;

    public static final String REGISER_URL = URI_USER_SERVICE + URI_REGISER_USER;

    public static final String FORGOT_PASSWORD_OTP_URL = URI_USER_SERVICE + URI_FORGOT_PASSWORD_OTP;

    public static final String VALIDATE_OTP_URL = URI_USER_SERVICE + URI_VALIDATE_OTP;
}
