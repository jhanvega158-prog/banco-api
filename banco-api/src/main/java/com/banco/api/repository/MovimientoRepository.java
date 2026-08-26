package com.banco.api.repository;

import com.banco.api.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByUsuarioId(Long usuarioId);

    List<Movimiento> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}