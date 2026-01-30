package edu.fpt.groupfive.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomerUserDetailsService {
    UserDetails loadUserByUsername(String username);
}
