package com.example.learning.erpMain.payload.request;

public class ForgotPassOtp {
    private String email;
    private int otp;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }
}
