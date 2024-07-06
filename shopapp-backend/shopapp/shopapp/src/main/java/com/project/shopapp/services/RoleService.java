package com.project.shopapp.services;

import com.project.shopapp.entities.Role;
import com.project.shopapp.repositories.RoleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final RoleRepo roleRepo;

    @Override
    public List<Role> getRoles() {
        return roleRepo.findAll();
    }
}
