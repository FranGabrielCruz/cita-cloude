package com.citacloud.app.repositories;

import com.citacloud.app.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmpresaIdAndUsuario(UUID empresaId, String usuario);
    boolean existsByEmpresaIdAndUsuario(UUID empresaId, String usuario);
    List<Usuario> findByEmpresaId(UUID empresaId);
}
