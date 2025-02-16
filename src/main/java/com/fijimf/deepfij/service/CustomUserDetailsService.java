package com.fijimf.deepfij.service;

import com.fijimf.deepfij.repo.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
   import org.springframework.security.core.userdetails.UserDetails;
   import org.springframework.security.core.userdetails.User;
   import org.springframework.security.core.userdetails.UserDetailsService;
   import org.springframework.security.core.userdetails.UsernameNotFoundException;
   import org.springframework.stereotype.Service;
   import org.springframework.beans.factory.annotation.Autowired;

   import java.util.Set;
   import java.util.stream.Collectors;

   @Service
   public class CustomUserDetailsService implements UserDetailsService {

       private final UserRepository userRepository;

       public CustomUserDetailsService(UserRepository userRepository) {
           this.userRepository = userRepository;
       }

       @Override
       public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
           // Fetch the user from the database
           com.fijimf.deepfij.model.User user = userRepository.findByUsername(username);

           if (user == null) {
               throw new UsernameNotFoundException("User not found with username: " + username);
           }

           // Map roles to authorities
           Set<SimpleGrantedAuthority> authorities = user.getRoles()
               .stream()
               .map(role -> new SimpleGrantedAuthority(role.getName()))
               .collect(Collectors.toSet());

           // Return a Spring Security User object
           return new User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, authorities);
       }
   }