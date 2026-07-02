package com.Ashish.Booking.Sytem.UserManagement;

import com.Ashish.Booking.Sytem.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder encoder;

    public UserResponse saveUser(UserDTO dto){

        if(userRepo.existsByEmail(dto.getEmail())){
                throw new UserAlreadyExistsException(
                        "user already exists with this email"
                );
        }

        // converting from dto to user object
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.USER);

        User savedUSer = userRepo.save(user);

        // converting to response type from user

        UserResponse resp = new UserResponse();
        resp.setId(savedUSer.getId());
        resp.setEmail(savedUSer.getEmail());
        resp.setName(savedUSer.getName());

        return resp;
    }

    public boolean existsByEmail(String email){
        return userRepo.existsByEmail(email);
    }
    public User findUserByEmail(String email){
        Optional<User> user = userRepo.findByEmail(email);
        return user.get();
    }

    public LoggedUser getLoggedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String role = authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .get()
                .getAuthority();

        LoggedUser user = new LoggedUser();
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
    public User getUserByEmail(String email){
        return userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("user not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("user not found"));
        return new PrincipleUser(user);
    }
}
