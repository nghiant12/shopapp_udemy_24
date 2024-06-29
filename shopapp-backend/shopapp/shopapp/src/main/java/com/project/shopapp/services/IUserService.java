package com.project.shopapp.services;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.entities.User;
import com.project.shopapp.exceptions.DataNotFoundException;

public interface IUserService {
    User register(UserDTO userDTO) throws Exception;

    String login(String phoneNumber, String password) throws Exception;
}
