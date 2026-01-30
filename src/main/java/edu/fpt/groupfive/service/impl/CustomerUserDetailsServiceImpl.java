package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomerUserDetailsServiceImpl implements UserDetailsService {

    private final UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Users users = userDAO.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại: " + username));

        return User
                .withUsername(users.getUsername())
                .password(users.getPasswordHash())
                .roles(users.getRole())
                .build();
    }
}
