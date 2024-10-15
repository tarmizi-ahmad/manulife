package com.manulife.backend.service;

import com.manulife.backend.model.User;
import net.sf.jasperreports.engine.JRException;

import java.util.List;

public interface UserService {
    User saveUser(User user);
    User updateUser(Long id, User user);
    User getUserById(Long id);
    void deleteUser(Long id);
    List<User> getAllUsers();
    byte[] generateUserReport() throws JRException;
}
