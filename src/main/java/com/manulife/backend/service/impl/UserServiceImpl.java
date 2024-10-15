package com.manulife.backend.service.impl;

import com.manulife.backend.model.User;
import com.manulife.backend.repository.UserRepository;
import com.manulife.backend.service.UserService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setDateOfBirth(user.getDateOfBirth());
        existingUser.setStatus(user.getStatus());
        return userRepository.save(existingUser);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public byte[] generateUserReport() throws JRException {
        List<User> users = userRepository.findAll();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);

        // Compile the report from the .jrxml file
        JasperReport jasperReport = JasperCompileManager.compileReport(
                "src/main/resources/reports/user_report.jrxml");

        // Fill the report with data from the datasource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), dataSource);

        // Export the report to a PDF and return as byte array
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
