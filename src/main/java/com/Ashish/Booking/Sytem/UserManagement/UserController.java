package com.Ashish.Booking.Sytem.UserManagement;

import com.Ashish.Booking.Sytem.Security.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth/")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @PostMapping("register")
    public UserResponse register(@Valid @RequestBody UserDTO user){
        return userService.saveUser(user);
    }

    @PostMapping("login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest){

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));
        return new LoginResponse(jwtService.generateToken(
                loginRequest.getEmail()));
    }

    @GetMapping("me")
    public LoggedUser loggedUser(){
       LoggedUser user = userService.getLoggedUser();
       return user;
    }

    @GetMapping("hello")
    public String hello(){
        return "secured";
    }
}
