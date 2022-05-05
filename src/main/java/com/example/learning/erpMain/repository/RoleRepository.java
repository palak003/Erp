package com.example.learning.erpMain.repository;

import com.example.learning.erpMain.models.ERole;
import com.example.learning.erpMain.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
