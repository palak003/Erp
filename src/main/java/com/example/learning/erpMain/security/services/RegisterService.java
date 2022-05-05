package com.example.learning.erpMain.security.services;

import com.example.learning.erpMain.models.ERole;
import com.example.learning.erpMain.payload.request.ErrorDto;
import com.example.learning.erpMain.models.Role;
import com.example.learning.erpMain.models.User;
import com.example.learning.erpMain.payload.request.*;
import com.example.learning.erpMain.payload.response.ErrorResponse;
import com.example.learning.erpMain.payload.response.JwtResponse;
import com.example.learning.erpMain.repository.RoleRepository;
import com.example.learning.erpMain.repository.StudentRepository;
import com.example.learning.erpMain.repository.TeacherRepository;
import com.example.learning.erpMain.repository.UserRepository;
import com.example.learning.erpMain.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RegisterService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    OtpService otpService;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    StudentRepository studentRepository;

    public Object validateRegisterUser(SignUpOtpDetails signUpOtpDetails){

        final String FAIL = "Entered Otp is NOT valid. Please Retry!";
        if(signUpOtpDetails.getOtp() >= 0){
            int serverOtp = otpService.getOtp(signUpOtpDetails.getEmail().toLowerCase());
            if(serverOtp > 0){
                if(signUpOtpDetails.getOtp()== serverOtp){
                    User user = new User(signUpOtpDetails.getUsername(),
                            signUpOtpDetails.getEmail().toLowerCase(),
                            encoder.encode(signUpOtpDetails.getPassword()));

                    Set<String> strRoles = signUpOtpDetails.getRole();
                    Set<Role> roles = new HashSet<>();


                    if (strRoles == null) {
                        throw new IllegalArgumentException("No roles assigned");
                    } else {
                        for(String r:strRoles) {
                            if(r.equals("student")) {
                                Role adminRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                                        .orElseThrow(() -> new IllegalArgumentException("Error: Role is not found."));
                                roles.add(adminRole);
                            }
                            else if(r.equals("teacher")) {
                                Role modRole = roleRepository.findByName(ERole.ROLE_TEACHER)
                                        .orElseThrow(() -> new IllegalArgumentException("Error: Role is not found."));
                                roles.add(modRole);
                            }
                            else {
                                Role userRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                        .orElseThrow(() -> new IllegalArgumentException("Error: Role is not found."));
                                roles.add(userRole);
                            }
                        }
                    }
                    user.setRoles(roles);
                    userRepository.save(user);
                    otpService.clearOTP(signUpOtpDetails.getEmail().toLowerCase());
                    return generateToken(signUpOtpDetails.getUsername(), signUpOtpDetails.getPassword());
                }else{
                    throw new IllegalArgumentException(FAIL);
                }
            }else {
                throw new IllegalArgumentException(FAIL);
            }
        }else {
            throw new IllegalArgumentException(FAIL);
        }
    }
    public Object forgotPassOtp(ForgotPassOtp signUpOtpDetails) {
        final String SUCCESS = "Entered Otp is correct";
        final String FAIL = "Entered Otp is NOT valid. Please Retry!";
        if(!userRepository.existsByEmail(signUpOtpDetails.getEmail().toLowerCase()))
            return new ResponseEntity<>("Account does not exists", HttpStatus.BAD_REQUEST);
        if (signUpOtpDetails.getOtp() >= 0) {
            int serverForgotOtp = otpService.getOtp(signUpOtpDetails.getEmail().toLowerCase());
            if (serverForgotOtp > 0) {
                if (signUpOtpDetails.getOtp() == serverForgotOtp) {
                    return new ResponseEntity<>(SUCCESS,HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
        }
    }
    public Object registerUser(SignupRequest signupRequest)  {
        ErrorResponse errorResponse=new ErrorResponse();
        Set<ErrorDto> set=new HashSet<>();
        boolean isError=false;
        if(signupRequest.getRole().contains("teacher") && !teacherRepository.existsByTeacherNumber(signupRequest.getUsername())){
            isError=true;
            set.add(new ErrorDto("username","Username is not correct"));
        }
        else if(signupRequest.getRole().contains("student") && !studentRepository.existsByStudentNumber(signupRequest.getUsername())) {
            isError=true;
            set.add(new ErrorDto("username","Username is not correct"));
        }
        if(!isValidEmail(signupRequest.getEmail())){
            isError=true;
            set.add(new ErrorDto("email","Invalid Email"));
        }
        if(!isValidPassword(signupRequest.getPassword())){
            isError=true;
            set.add(new ErrorDto("password","Invalid password"));
        }
        if(userRepository.existsByUsername(signupRequest.getUsername())) {
            isError = true;
            set.add(new ErrorDto("username","You already have an account please SignIn"));
        }
        if(userRepository.existsByEmail(signupRequest.getEmail().toLowerCase())){
            isError=true;
            set.add(new ErrorDto("email","Email is already in use"));
        }
        if(isError) {
            errorResponse.setStatus("failure");
            errorResponse.setErrorDto(set);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        else{
            int otp = otpService.generateOTP(signupRequest.getEmail().toLowerCase());
            this.sendOtpMail(signupRequest.getEmail().toLowerCase(), otp);
            set.add(new ErrorDto("","Otp Sent"));
            errorResponse.setStatus("success");
            errorResponse.setErrorDto(set);
            return new ResponseEntity<>(errorResponse,HttpStatus.OK);
        }
    }
    public Object forgotPass(ForgotPass forgotPassRequest){
        if(!isValidEmail(forgotPassRequest.getEmail()))
            return new ResponseEntity<>("Invalid Email", HttpStatus.BAD_REQUEST);
        if(!userRepository.existsByEmail(forgotPassRequest.getEmail()))
            return new ResponseEntity<>("Seems like you don't have any account", HttpStatus.BAD_REQUEST);
        else{
            int forgotPassOtp = otpService.generateOTP(forgotPassRequest.getEmail().toLowerCase());
            this.sendOtpMail(forgotPassRequest.getEmail().toLowerCase(), forgotPassOtp);
            return new ResponseEntity<>("Otp Sent",HttpStatus.OK);
        }
    }
    public Object validateForgotPass(ForgotPass validateForgotPassRequest){
        if(!isValidEmail(validateForgotPassRequest.getEmail()))
            return new ResponseEntity<>("Invalid Email", HttpStatus.BAD_REQUEST);
        if(!userRepository.existsByEmail(validateForgotPassRequest.getEmail()))
            return new ResponseEntity<>("Account does not exists", HttpStatus.BAD_REQUEST);
        User user=userRepository.findByEmail(validateForgotPassRequest.getEmail().toLowerCase());
        if(!isValidPassword(validateForgotPassRequest.getPassword()))
            return new ResponseEntity<>("Invalid Password",HttpStatus.BAD_REQUEST);
        user.setPassword(encoder.encode(validateForgotPassRequest.getPassword()));
        userRepository.save(user);
        otpService.clearOTP(validateForgotPassRequest.getEmail().toLowerCase());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    public Object signin(LoginRequest loginRequest){
        User user;
        if(!userRepository.existsByEmail(loginRequest.getEmail()) && !userRepository.existsByUsername(loginRequest.getUsername()))
            return new ResponseEntity<>("Please signUp", HttpStatus.BAD_REQUEST);
        else if(Objects.equals(loginRequest.getUsername(), "") && !Objects.equals(loginRequest.getEmail(), ""))
            user=userRepository.findByEmail(loginRequest.getEmail().toLowerCase());
        else if(!Objects.equals(loginRequest.getUsername(), "") && Objects.equals(loginRequest.getEmail(), ""))
            user=userRepository.findByUsername(loginRequest.getUsername());
        else
        user=userRepository.findByUsername(loginRequest.getUsername());
       if(!encoder.matches(loginRequest.getPassword(),user.getPassword()))
           return new ResponseEntity<>("Wrong credentials",HttpStatus.BAD_REQUEST);
        return generateToken(user.getUsername(),loginRequest.getPassword());
    }
    public Object generateToken(String username,String password){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username,password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));

    }
    public void sendOtpMail(String email, int otp) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setSubject("first otp");
            mimeMessageHelper.setFrom(new InternetAddress("mailersendit@gmail.com", "SEND-itMailer.com"));
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setText("the otp is " + otp);

            javaMailSender.send(mimeMessage);


        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }
    }
    public  boolean isValidPassword(String password) {
        String regex2= "^[a-zA-Z0-9@#!$%^_]{8,12}$";
        Pattern pattern2 =Pattern.compile(regex2);
        Matcher matcher2=pattern2.matcher(password);
        return matcher2.matches();
    }
    public boolean isValidEmail(String email) {
        String regex1 = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";
        Pattern pattern1=Pattern.compile(regex1);
        Matcher matcher1=pattern1.matcher(email);
        return matcher1.matches();
    }

}
