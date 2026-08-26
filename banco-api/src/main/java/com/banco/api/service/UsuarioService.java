package com.banco.api.service;

import com.banco.api.model.Rol;
import com.banco.api.model.Usuario;
import com.banco.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // LISTAR TODOS
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // BUSCAR POR ID
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // BUSCAR POR EMAIL
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // CREAR USUARIO
    public Usuario crear(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Encriptar contraseña con BCrypt
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        // Si no envían rol, será CLIENTE
        if (usuario.getRol() == null) {
            usuario.setRol(Rol.CLIENTE);
        }

        return usuarioRepository.save(usuario);
    }

    // ACTUALIZAR
    public Usuario actualizar(Long id, Usuario datos) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(datos.getNombre());
        usuario.setEmail(datos.getEmail());
        usuario.setSaldo(datos.getSaldo());
        usuario.setRol(datos.getRol());

        return usuarioRepository.save(usuario);
    }

    // ELIMINAR
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}