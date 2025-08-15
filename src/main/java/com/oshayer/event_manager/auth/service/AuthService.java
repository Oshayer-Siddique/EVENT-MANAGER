package com.oshayer.event_manager.auth.service;

import com.oshayer.event_manager.auth.dto.*;

public interface AuthService {
    void signup(SignupRequest request);
    JwtResponse login(LoginRequest request);
    void verifyEmail(String token);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);


}

