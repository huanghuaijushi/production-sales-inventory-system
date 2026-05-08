package com.hhjs.psi.auth.repository;

import com.hhjs.psi.auth.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select sysUser from SysUser sysUser where sysUser.username = :username")
    Optional<SysUser> findByUsernameWithRoles(@Param("username") String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select sysUser from SysUser sysUser where sysUser.id = :id")
    Optional<SysUser> findByIdWithRoles(@Param("id") Long id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select sysUser from SysUser sysUser
            where :query = ''
               or lower(sysUser.username) like lower(concat('%', :query, '%'))
               or lower(sysUser.nickname) like lower(concat('%', :query, '%'))
            """)
    Page<SysUser> searchWithRoles(@Param("query") String query, Pageable pageable);

    boolean existsByUsername(String username);
}
