package com.banco.api.dto;

import com.banco.api.model.TipoMovimiento;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MovimientoRequest {

    private TipoMovimiento tipo;

    private BigDecimal monto;

    private String descripcion;

    private Long usuarioId;
}