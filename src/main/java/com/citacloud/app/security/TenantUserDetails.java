package com.citacloud.app.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class TenantUserDetails implements UserDetails {

    private final UUID usuarioId;
    private final UUID empresaId;
    private final String empresaCodigo;
    private final String empresaNombre;
    private final String username;
    private final String password;
    private final String nombreCompleto;
    private final Collection<? extends GrantedAuthority> authorities;

    public TenantUserDetails(UUID usuarioId, UUID empresaId, String empresaCodigo, String empresaNombre,
                             String username, String password, String nombreCompleto,
                             Collection<? extends GrantedAuthority> authorities) {
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.empresaCodigo = empresaCodigo;
        this.empresaNombre = empresaNombre;
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.authorities = authorities;
    }

    public UUID getUsuarioId() { return usuarioId; }
    public UUID getEmpresaId() { return empresaId; }
    public String getEmpresaCodigo() { return empresaCodigo; }
    public String getEmpresaNombre() { return empresaNombre; }
    public String getNombreCompleto() { return nombreCompleto; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
