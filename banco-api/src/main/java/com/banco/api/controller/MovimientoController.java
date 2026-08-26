package com.banco.api.controller;

import com.banco.api.dto.MovimientoRequest;
import com.banco.api.model.Movimiento;
import com.banco.api.model.Usuario;
import com.banco.api.repository.UsuarioRepository;
import com.banco.api.service.MovimientoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final UsuarioRepository usuarioRepository;

    public MovimientoController(
            MovimientoService movimientoService,
            UsuarioRepository usuarioRepository) {

        this.movimientoService = movimientoService;
        this.usuarioRepository = usuarioRepository;
    }

    // =====================================================
    // ADMIN - LISTAR TODOS LOS MOVIMIENTOS
    // =====================================================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Movimiento>> listarTodos() {

        return ResponseEntity.ok(
                movimientoService.listarTodos()
        );
    }


    // =====================================================
    // CLIENTE - VER SUS PROPIOS MOVIMIENTOS
    // ADMIN TAMBIEN PUEDE USAR ESTE ENDPOINT
    // =====================================================
    @GetMapping("/mis-movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<List<Movimiento>> misMovimientos(
            Authentication authentication) {

        Usuario usuario = obtenerUsuarioAutenticado(authentication);

        return ResponseEntity.ok(
                movimientoService.listarPorUsuario(
                        usuario.getId()
                )
        );
    }


    // =====================================================
    // ADMIN - LISTAR MOVIMIENTOS DE UN USUARIO
    // =====================================================
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Movimiento>> listarPorUsuario(
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(
                movimientoService.listarPorUsuario(usuarioId)
        );
    }


    // =====================================================
    // BUSCAR MOVIMIENTO POR ID
    // ADMIN PUEDE VER CUALQUIERA
    // CLIENTE SOLO PUEDE VER EL SUYO
    // =====================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<Movimiento> buscarPorId(
            @PathVariable Long id,
            Authentication authentication) {

        Movimiento movimiento =
                movimientoService.buscarPorId(id);

        Usuario usuario =
                obtenerUsuarioAutenticado(authentication);

        boolean esAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        if (!esAdmin &&
                !movimiento.getUsuario()
                        .getId()
                        .equals(usuario.getId())) {

            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(movimiento);
    }


    // =====================================================
    // CREAR MOVIMIENTO
    //
    // CLIENTE:
    // El usuario se obtiene DEL TOKEN.
    // No confiamos en usuarioId enviado desde Angular/Postman.
    //
    // ADMIN:
    // Puede indicar usuarioId.
    // =====================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<Movimiento> crear(
            @RequestBody MovimientoRequest request,
            Authentication authentication) {

        Usuario usuarioAutenticado =
                obtenerUsuarioAutenticado(authentication);

        boolean esAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        Long usuarioId;

        if (esAdmin) {

            if (request.getUsuarioId() == null) {
                usuarioId = usuarioAutenticado.getId();
            } else {
                usuarioId = request.getUsuarioId();
            }

        } else {

            // CLIENTE SIEMPRE USA SU PROPIO ID
            usuarioId = usuarioAutenticado.getId();
        }

        Movimiento movimiento =
                movimientoService.crear(
                        usuarioId,
                        request.getTipo(),
                        request.getMonto(),
                        request.getDescripcion()
                );

        return ResponseEntity.ok(movimiento);
    }


    // =====================================================
    // ACTUALIZAR MOVIMIENTO
    //
    // ADMIN PUEDE ACTUALIZAR CUALQUIERA
    // CLIENTE SOLO SUS MOVIMIENTOS
    // =====================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<Movimiento> actualizar(
            @PathVariable Long id,
            @RequestBody MovimientoRequest request,
            Authentication authentication) {

        Movimiento movimientoExistente =
                movimientoService.buscarPorId(id);

        Usuario usuarioAutenticado =
                obtenerUsuarioAutenticado(authentication);

        boolean esAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        if (!esAdmin &&
                !movimientoExistente
                        .getUsuario()
                        .getId()
                        .equals(usuarioAutenticado.getId())) {

            return ResponseEntity.status(403).build();
        }

        Long usuarioId =
                movimientoExistente
                        .getUsuario()
                        .getId();

        Movimiento movimiento =
                movimientoService.actualizar(
                        id,
                        request.getTipo(),
                        request.getMonto(),
                        request.getDescripcion(),
                        usuarioId
                );

        return ResponseEntity.ok(movimiento);
    }


    // =====================================================
    // ELIMINAR MOVIMIENTO
    //
    // ADMIN PUEDE ELIMINAR CUALQUIERA
    // CLIENTE SOLO LOS SUYOS
    // =====================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Movimiento movimiento =
                movimientoService.buscarPorId(id);

        Usuario usuario =
                obtenerUsuarioAutenticado(authentication);

        boolean esAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(a ->
                                a.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        if (!esAdmin &&
                !movimiento.getUsuario()
                        .getId()
                        .equals(usuario.getId())) {

            return ResponseEntity.status(403).build();
        }

        movimientoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }


    // =====================================================
    // OBTENER USUARIO AUTENTICADO DESDE JWT
    // =====================================================
    private Usuario obtenerUsuarioAutenticado(
            Authentication authentication) {

        String email = authentication.getName();

        return usuarioRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario autenticado no encontrado"
                        )
                );
    }
}