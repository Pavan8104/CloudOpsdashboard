package com.cloudops.dashboard.repository;

import com.cloudops.dashboard.model.Role;
import com.cloudops.dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User repository - database se user data fetch karne ka gateway.
 *
 * JpaRepository extend kiya hai - basic CRUD operations free mein milte hain.
 * Custom queries sirf wahan likhte hain jahan Spring Data JPA ki
 * derived queries kaam nahi karti ya performance concern ho.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Username se user dhundho - login ke waqt yahi use hota hai
    Optional<User> findByUsername(String username);

    // Email se dhundho - password reset flow ke liye
    Optional<User> findByEmail(String email);

    // Username ya email se dhundho - flexible login ke liye
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Check karo koi username already exist karta hai ya nahi - registration validation
    boolean existsByUsername(String username);

    // Email unique check - same
    boolean existsByEmail(String email);

    // Sabhi active users - admin panel mein user list ke liye
    List<User> findByEnabledTrue();

    // Role ke basis pe users - admin ko ENGINEER list dekhni ho toh
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.enabled = true")
    List<User> findByRole(Role role);
}
