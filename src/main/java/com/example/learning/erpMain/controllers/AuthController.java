package com.example.learning.erpMain.controllers;

import com.example.learning.erpMain.payload.request.*;
import com.example.learning.erpMain.security.services.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    RegisterService registerService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return (ResponseEntity<?>) this.registerService.signin(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest)
    {
        return (ResponseEntity<?>) this.registerService.registerUser(signUpRequest);
    }

    @PostMapping("/verifySignup") //special
    public ResponseEntity<?> validateRegisterService(@RequestBody SignUpOtpDetails signUpOtpDetails)
    {
        try{return ResponseEntity.status(HttpStatus.OK).body(this.registerService.validateRegisterUser(signUpOtpDetails));}
        catch(IllegalArgumentException e1){return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e1.getLocalizedMessage());}
    }

    @PostMapping("/forgotPass")
    public ResponseEntity<?> forgotPass(@RequestBody ForgotPass signupRequest)
    {
       return (ResponseEntity<?>) this.registerService.forgotPass(signupRequest);
    }

    @PostMapping("/forgotPassOtp")
    public ResponseEntity<?> forgotPassOtp(@RequestBody ForgotPassOtp signUpOtpDetails)
    {
        return (ResponseEntity<?>) this.registerService.forgotPassOtp(signUpOtpDetails);
    }

    @PostMapping("validateForgotPass")
    public ResponseEntity<?> validateForgotPass(@RequestBody ForgotPass signupRequest){
        return (ResponseEntity<?>) this.registerService.validateForgotPass(signupRequest);
    }
}