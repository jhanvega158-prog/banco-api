package com.banco.api.service;

import com.banco.api.exception.ApiException;
import com.banco.api.model.Movimiento;
import com.banco.api.model.TipoMovimiento;
import com.banco.api.model.Usuario;
import com.banco.api.repository.MovimientoRepository;
import com.banco.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoService(
            MovimientoRepository movimientoRepository,
            UsuarioRepository usuarioRepository) {

        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // lista todos los usuarios

    public List<Movimiento> listarTodos() {
        return movimientoRepository.findAll();
    }

    // aqui da la lista de los usuarios

    public List<Movimiento> listarPorUsuario(Long usuarioId) {

        if (usuarioId == null) {
            throw new ApiException("El usuario es obligatorio");
        }

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ApiException("Usuario no encontrado");
        }

        return movimientoRepository
                .findByUsuarioIdOrderByFechaDesc(usuarioId);
    }


    // esto busca por el id del usuario

    public Movimiento buscarPorId(Long id) {

        return movimientoRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Movimiento no encontrado")
                );
    }


    // esto es para crear el movimiento

    @Transactional
    public Movimiento crear(
            Long usuarioId,
            TipoMovimiento tipo,
            BigDecimal monto,
            String descripcion) {

        if (usuarioId == null) {
            throw new ApiException(
                    "El usuario es obligatorio"
            );
        }

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new ApiException(
                                "Usuario no encontrado"
                        )
                );

        validarMovimiento(tipo, monto);

        BigDecimal saldoActual =
                usuario.getSaldo() != null
                        ? usuario.getSaldo()
                        : BigDecimal.ZERO;


        // aqui el deposito

        if (tipo == TipoMovimiento.DEPOSITO) {

            usuario.setSaldo(
                    saldoActual.add(monto)
            );

        }


        // aqui se hace el retiro

        else if (tipo == TipoMovimiento.RETIRO) {

            if (saldoActual.compareTo(monto) < 0) {

                throw new ApiException(
                        "Saldo insuficiente"
                );
            }

            usuario.setSaldo(
                    saldoActual.subtract(monto)
            );
        }

        Movimiento movimiento =
                Movimiento.builder()
                        .tipo(tipo)
                        .monto(monto)
                        .fecha(LocalDateTime.now())
                        .descripcion(descripcion)
                        .usuario(usuario)
                        .build();

        // Guardamos saldo actualizado
        usuarioRepository.save(usuario);

        // Guardamos movimiento
        return movimientoRepository.save(movimiento);
    }


    // Aqui se actualiza

    @Transactional
    public Movimiento actualizar(
            Long id,
            TipoMovimiento nuevoTipo,
            BigDecimal nuevoMonto,
            String nuevaDescripcion,
            Long usuarioId) {

        Movimiento movimiento =
                buscarPorId(id);

        Usuario usuario =
                movimiento.getUsuario();

        if (usuarioId == null) {
            throw new ApiException(
                    "El usuario es obligatorio"
            );
        }

        if (!usuario.getId().equals(usuarioId)) {

            throw new ApiException(
                    "El movimiento no pertenece al usuario indicado"
            );
        }

        validarMovimiento(
                nuevoTipo,
                nuevoMonto
        );

        BigDecimal saldoActual =
                usuario.getSaldo() != null
                        ? usuario.getSaldo()
                        : BigDecimal.ZERO;

        /*
         * PRIMERO REVERTIMOS EL MOVIMIENTO ANTERIOR
         *
         * Si antes era DEPÓSITO:
         * debemos quitar ese dinero.
         *
         * Si antes era RETIRO:
         * se devuelve ese dinero.
         */

        if (movimiento.getTipo()
                == TipoMovimiento.DEPOSITO) {

            saldoActual =
                    saldoActual.subtract(
                            movimiento.getMonto()
                    );

        } else if (movimiento.getTipo()
                == TipoMovimiento.RETIRO) {

            saldoActual =
                    saldoActual.add(
                            movimiento.getMonto()
                    );
        }

        /*
         * AHORA APLICAMOS EL NUEVO MOVIMIENTO
         */

        if (nuevoTipo
                == TipoMovimiento.DEPOSITO) {

            saldoActual =
                    saldoActual.add(
                            nuevoMonto
                    );

        } else if (nuevoTipo
                == TipoMovimiento.RETIRO) {

            if (saldoActual.compareTo(
                    nuevoMonto) < 0) {

                throw new ApiException(
                        "Saldo insuficiente para actualizar el movimiento"
                );
            }

            saldoActual =
                    saldoActual.subtract(
                            nuevoMonto
                    );
        }

        if (saldoActual.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new ApiException(
                    "El saldo no puede quedar negativo"
            );
        }

        // Actualizamos movimiento
        movimiento.setTipo(nuevoTipo);
        movimiento.setMonto(nuevoMonto);
        movimiento.setDescripcion(
                nuevaDescripcion
        );

        // Actualizamos saldo
        usuario.setSaldo(saldoActual);

        usuarioRepository.save(usuario);

        return movimientoRepository.save(
                movimiento
        );
    }


    // aqui elimina el movimiento

    @Transactional
    public void eliminar(Long id) {

        Movimiento movimiento =
                buscarPorId(id);

        Usuario usuario =
                movimiento.getUsuario();

        BigDecimal saldoActual =
                usuario.getSaldo() != null
                        ? usuario.getSaldo()
                        : BigDecimal.ZERO;

        if (movimiento.getTipo()
                == TipoMovimiento.DEPOSITO) {

            // Eliminar un depósito significa
            // quitar ese dinero del saldo.

            BigDecimal nuevoSaldo =
                    saldoActual.subtract(
                            movimiento.getMonto()
                    );

            if (nuevoSaldo.compareTo(
                    BigDecimal.ZERO) < 0) {

                throw new ApiException(
                        "No se puede eliminar el depósito porque el saldo quedaría negativo"
                );
            }

            usuario.setSaldo(nuevoSaldo);

        } else if (movimiento.getTipo()
                == TipoMovimiento.RETIRO) {

            // Eliminar un retiro significa
            // devolver ese dinero.

            usuario.setSaldo(
                    saldoActual.add(
                            movimiento.getMonto()
                    )
            );
        }

        movimientoRepository.delete(
                movimiento
        );

        usuarioRepository.save(
                usuario
        );
    }

    // aqui me valida el movimiento

    private void validarMovimiento(
            TipoMovimiento tipo,
            BigDecimal monto) {

        if (tipo == null) {

            throw new ApiException(
                    "El tipo de movimiento es obligatorio"
            );
        }

        if (monto == null) {

            throw new ApiException(
                    "El monto es obligatorio"
            );
        }

        if (monto.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new ApiException(
                    "El monto debe ser mayor que cero"
            );
        }
    }
}