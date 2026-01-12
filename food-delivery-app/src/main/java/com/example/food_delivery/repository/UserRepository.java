package com.example.food_delivery.repository;

import com.example.food_delivery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailId(String emailId);
    Boolean existsByEmailId(String emailId);
    Boolean existsByMobileNo(String mobileNo);
}