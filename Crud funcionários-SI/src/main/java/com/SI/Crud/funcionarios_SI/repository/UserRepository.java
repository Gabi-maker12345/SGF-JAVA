package com.SI.Crud.funcionarios_SI.repository;

import com.SI.Crud.funcionarios_SI.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
