package com.citacloud.app.security;

import com.citacloud.app.models.Empresa;
import com.citacloud.app.models.Usuario;
import com.citacloud.app.repositories.EmpresaRepository;
import com.citacloud.app.repositories.UsuarioRepository;
import com.vaadin.flow.server.VaadinSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean login(String empresaCodigo, String username, String password) {
        if (empresaCodigo == null || empresaCodigo.isBlank() ||
            username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            return false;
        }

        // 1. Buscar empresa por código
        Optional<Empresa> empresaOpt = empresaRepository.findByCodigo(empresaCodigo.trim().toUpperCase());
        if (empresaOpt.isEmpty()) {
            return false;
        }

        Empresa empresa = empresaOpt.get();
        // 2. Validar empresa activa
        if (!Boolean.TRUE.equals(empresa.getActiva())) {
            return false;
        }

        // 3. Buscar usuario en la empresa
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmpresaIdAndUsuario(empresa.getId(), username.trim());
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        // 4. Validar estado del usuario
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return false;
        }

        // 5. Validar contraseña exclusivamente contra su hash BCrypt.
        boolean passwordMatches = false;
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isBlank()) {
            try {
                passwordMatches = passwordEncoder.matches(password, usuario.getPasswordHash());
            } catch (IllegalArgumentException ignored) {
                passwordMatches = false;
            }
        }

        if (!passwordMatches) {
            return false;
        }

        // 6. Roles y authorities
        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .flatMap(rol -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(new SimpleGrantedAuthority("ROLE_" + rol.getNombre())),
                        rol.getPermisos().stream().map(permiso -> new SimpleGrantedAuthority(permiso.getCodigo()))))
                .collect(Collectors.toList());

        TenantUserDetails userDetails = new TenantUserDetails(
                usuario.getId(),
                empresa.getId(),
                empresa.getCodigo(),
                empresa.getNombre(),
                usuario.getUsuario(),
                usuario.getPasswordHash(),
                usuario.getNombreCompleto(),
                authorities
        );

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
        TenantContext.setCurrentTenant(empresa.getId());

        if (VaadinSession.getCurrent() != null) {
            VaadinSession vaadinSession = VaadinSession.getCurrent();
            vaadinSession.setAttribute(TenantUserDetails.class, userDetails);
            vaadinSession.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );
        }

        return true;
    }

    public static TenantUserDetails getAuthenticatedUser() {
        if (VaadinSession.getCurrent() != null) {
            TenantUserDetails sessionUser = VaadinSession.getCurrent().getAttribute(TenantUserDetails.class);
            if (sessionUser != null) return sessionUser;
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof TenantUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    public static void logout() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().getSession().removeAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
            );
            VaadinSession.getCurrent().close();
        }
    }
}
