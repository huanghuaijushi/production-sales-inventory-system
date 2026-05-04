package com.hhjs.psi.auth.repository;

import com.hhjs.psi.auth.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    @EntityGraph(attributePaths = "permissions")
    @Query("select r from Role r where r.code = :code")
    Optional<Role> findByCodeWithPermissions(@Param("code") String code);
}
