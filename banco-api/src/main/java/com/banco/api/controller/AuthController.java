package com.banco.api.controller;

import com.banco.api.dto.LoginRequest;
import com.banco.api.dto.LoginResponse;
import com.banco.api.model.Usuario;
import com.banco.api.repository.UsuarioRepository;
import com.banco.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email o contraseña incorrectos");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario);

        LoginResponse response = new LoginResponse(
                token,
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name()
        );

        return ResponseEntity.ok(response);
    }
}