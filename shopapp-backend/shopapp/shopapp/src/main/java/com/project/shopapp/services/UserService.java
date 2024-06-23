package com.project.shopapp.services;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.entities.Role;
import com.project.shopapp.entities.User;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.repositories.RoleRepo;
import com.project.shopapp.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    @Override
    public User createUser(UserDTO userDTO) throws DataNotFoundException {
        String phoneNumber = userDTO.getPhoneNumber();
        if (userRepo.existsByPhoneNumber(phoneNumber)) { // check phone number exists
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        //convert from userDTO -> user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();
        Role role = roleRepo.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));
        newUser.setRole(role);
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
//            String encodedPassword = passwordEncoded.encoded(password);
//            newUser.setPassword(encodedPassword);
        }
        return userRepo.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) {
        return null;
    }
}
