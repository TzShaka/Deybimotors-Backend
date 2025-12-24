package com.deybimotors.service;

import com.deybimotors.dto.KardexDTO;
import com.deybimotors.entity.MovimientoKardex;
import com.deybimotors.repository.MovimientoKardexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Kardex - RF-033 a RF-039
 * Historial completo de movimientos de inventario
 */
@Service
@RequiredArgsConstructor
public class KardexService {

    private final MovimientoKardexRepository kardexRepository;

    /**
     * Kardex general - RF-033
     */
    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarTodos() {
        return kardexRepository.findTop50ByOrderByFechaMovimientoDesc().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Kardex por producto - RF-034
     */
    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorProducto(Long productoId) {
        return kardexRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Kardex por sede
     */
    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorSede(Long sedeId) {
        return kardexRepository.findBySedeIdOrderByFechaMovimientoDesc(sedeId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Kardex por tipo de movimiento - RF-036
     */
    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorTipo(String tipo) {
        MovimientoKardex.TipoMovimiento tipoMovimiento = MovimientoKardex.TipoMovimiento.valueOf(tipo.toUpperCase());
        return kardexRepository.findByTipoMovimientoOrderByFechaMovimientoDesc(tipoMovimiento).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Kardex por rango de fechas - RF-035
     */
    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return kardexRepository.findByFechaMovimientoBetween(fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Kardex filtrado por producto y fechas
     */
    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorProductoYFechas(
            Long productoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        return kardexRepository.findByProductoAndFechas(productoId, fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Kardex por usuario responsable - RF-037
     */
    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorUsuario(Long usuarioId) {
        return kardexRepository.findByUsuarioResponsableIdOrderByFechaMovimientoDesc(usuarioId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private KardexDTO.MovimientoResponse convertirADTO(MovimientoKardex movimiento) {
        return new KardexDTO.MovimientoResponse(
                movimiento.getId(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getCodigo(),
                movimiento.getProducto().getNombre(),
                movimiento.getSede().getId(),
                movimiento.getSede().getNombre(),
                movimiento.getTipoMovimiento().name(),
                traducirTipoMovimiento(movimiento.getTipoMovimiento()),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getMotivo(),
                movimiento.getUsuarioResponsable().getNombreCompleto(),
                movimiento.getFechaMovimiento(),
                movimiento.getReferencia(),
                movimiento.getObservaciones()
        );
    }

    private String traducirTipoMovimiento(MovimientoKardex.TipoMovimiento tipo) {
        return switch (tipo) {
            case ENTRADA_COMPRA -> "Entrada por Compra";
            case SALIDA_VENTA -> "Salida por Venta";
            case AJUSTE_POSITIVO -> "Ajuste Positivo";
            case AJUSTE_NEGATIVO -> "Ajuste Negativo";
            case TRASLADO_ENTRADA -> "Traslado (Entrada)";
            case TRASLADO_SALIDA -> "Traslado (Salida)";
            case DEVOLUCION -> "Devolución";
            case MERMA -> "Merma";
        };
    }
}