package com.example.smsmanager.ui.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult {
    @Nullable
    private LoggedInUserView success;
    @Nullable
    private Integer error;

    private String warn;


    LoginResult(@Nullable Integer error) {
        this.error = error;
    }
    LoginResult(@Nullable String w) {
        this.warn = w;
    }
    LoginResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    @Nullable
    LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }

    public String getWarn() {
        return warn;
    }
}