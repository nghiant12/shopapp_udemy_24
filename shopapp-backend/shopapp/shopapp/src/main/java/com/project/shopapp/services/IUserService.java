package com.project.shopapp.services;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.entities.User;
import com.project.shopapp.exceptions.DataNotFoundException;
import org.springframework.stereotype.Service;

public interface IUserService {
    User createUser(UserDTO userDTO) throws DataNotFoundException;

    String login(String phoneNumber, String password);
}
