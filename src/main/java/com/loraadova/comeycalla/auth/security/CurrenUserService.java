package com.loraadova.comeycalla.auth.security;

import com.loraadova.comeycalla.user.entity.User;
import com.loraadova.comeycalla.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrenUserService {

    @Autowired
    UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }


}
