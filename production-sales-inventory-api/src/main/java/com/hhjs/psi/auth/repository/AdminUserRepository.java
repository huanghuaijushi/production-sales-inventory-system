package com.hhjs.psi.auth.repository;

import com.hhjs.psi.auth.entity.AdminUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select adminUser from AdminUser adminUser where adminUser.username = :username")
    Optional<AdminUser> findByUsernameWithRoles(@Param("username") String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select adminUser from AdminUser adminUser where adminUser.id = :id")
    Optional<AdminUser> findByIdWithRoles(@Param("id") Long id);

    boolean existsByUsername(String username);
}
