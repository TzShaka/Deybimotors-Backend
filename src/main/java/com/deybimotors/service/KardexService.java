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
 * ACTUALIZADO: Compatible con estructura de BD
 */
@Service
@RequiredArgsConstructor
public class KardexService {

    private final MovimientoKardexRepository kardexRepository;

    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarTodos() {
        return kardexRepository.findTop50ByOrderByFechaMovimientoDesc().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorProducto(Long productoId) {
        return kardexRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorSede(Long sedeId) {
        return kardexRepository.findBySedeIdOrderByFechaMovimientoDesc(sedeId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorTipo(String tipo) {
        // Como ahora tipo_movimiento es VARCHAR, buscamos por String directamente
        List<MovimientoKardex> movimientos = kardexRepository.findAll().stream()
                .filter(m -> m.getTipoMovimiento().equalsIgnoreCase(tipo))
                .sorted((m1, m2) -> m2.getFechaMovimiento().compareTo(m1.getFechaMovimiento()))
                .collect(Collectors.toList());

        return movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return kardexRepository.findByFechaMovimientoBetween(fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

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

    @Transactional(readOnly = true)
    public List<KardexDTO.MovimientoResponse> listarPorUsuario(Long usuarioId) {
        List<MovimientoKardex> movimientos = kardexRepository.findAll().stream()
                .filter(m -> m.getUsuarioResponsable() != null && m.getUsuarioResponsable().getId().equals(usuarioId))
                .sorted((m1, m2) -> m2.getFechaMovimiento().compareTo(m1.getFechaMovimiento()))
                .collect(Collectors.toList());

        return movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private KardexDTO.MovimientoResponse convertirADTO(MovimientoKardex movimiento) {
        return new KardexDTO.MovimientoResponse(
                movimiento.getId(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getCodigoInterno(),
                movimiento.getProducto().getDescripcion(),
                movimiento.getSede().getId(),
                movimiento.getSede().getNombre(),
                movimiento.getTipoMovimiento(),
                traducirTipoMovimiento(movimiento.getTipoMovimiento()),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockActual(),
                movimiento.getTipoMovimiento(), // Motivo = tipo de movimiento
                movimiento.getUsuarioResponsable() != null ? movimiento.getUsuarioResponsable().getNombreCompleto() : "Sistema",
                movimiento.getFechaMovimiento(),
                movimiento.getReferenciaTabla(),
                null // observaciones no existe en BD
        );
    }

    private String traducirTipoMovimiento(String tipo) {
        return switch (tipo) {
            case "ENTRADA_COMPRA" -> "Entrada por Compra";
            case "SALIDA_VENTA" -> "Salida por Venta";
            case "AJUSTE_POSITIVO" -> "Ajuste Positivo";
            case "AJUSTE_NEGATIVO" -> "Ajuste Negativo";
            case "TRASLADO_ENTRADA" -> "Traslado (Entrada)";
            case "TRASLADO_SALIDA" -> "Traslado (Salida)";
            case "DEVOLUCION" -> "Devolución";
            case "MERMA" -> "Merma";
            default -> tipo;
        };
    }
}