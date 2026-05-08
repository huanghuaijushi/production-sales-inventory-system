package com.hhjs.psi.auth.repository;

import com.hhjs.psi.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
