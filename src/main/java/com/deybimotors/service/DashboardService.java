package com.deybimotors.service;

import com.deybimotors.dto.DashboardDTO;
import com.deybimotors.entity.Compra;
import com.deybimotors.entity.MovimientoKardex;
import com.deybimotors.entity.Producto;
import com.deybimotors.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Dashboard - ✅ ACTUALIZADO
 * ❌ SIN métrica de productosStockMinimo
 * ✅ CON métrica de productosStockBajo (stock <= 2)
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoKardexRepository kardexRepository;
    private final CompraRepository compraRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transactional(readOnly = true)
    public DashboardDTO.DashboardResponse obtenerDatosDashboard(Long sedeId) {

        // Total de productos registrados
        long totalProductos = productoRepository.countProductosActivos();

        // Productos sin stock en la sede
        long productosSinStock = productoRepository.countProductosSinStock(sedeId);

        // ✅ ACTUALIZADO: Productos con stock bajo (stock <= 2)
        long productosStockBajo = productoRepository.countProductosStockBajo(sedeId);

        // Total de categorías
        long totalCategorias = categoriaRepository.count();

        // Últimas actualizaciones
        List<DashboardDTO.UltimaActualizacion> ultimasActualizaciones = obtenerUltimasActualizaciones();

        // Movimientos recientes
        List<DashboardDTO.Movimiento> movimientosRecientes = obtenerMovimientosRecientes();

        return new DashboardDTO.DashboardResponse(
                totalProductos,
                productosSinStock,
                productosStockBajo,
                totalCategorias,
                ultimasActualizaciones,
                movimientosRecientes
        );
    }

    private List<DashboardDTO.UltimaActualizacion> obtenerUltimasActualizaciones() {

        List<DashboardDTO.UltimaActualizacion> actualizaciones = new ArrayList<>();

        // Últimas compras
        List<Compra> ultimasCompras = compraRepository.findTop10ByOrderByFechaRegistroDesc();
        for (Compra compra : ultimasCompras) {
            actualizaciones.add(new DashboardDTO.UltimaActualizacion(
                    "COMPRA",
                    "Compra " + compra.getNumeroCompra() + " - " + compra.getProveedor().getNombreEmpresa(),
                    compra.getFechaRegistro().format(DATE_FORMATTER),
                    compra.getUsuarioRegistro().getNombreCompleto()
            ));
        }

        // Últimos productos creados
        List<Producto> ultimosProductos = productoRepository.findAll()
                .stream()
                .sorted((p1, p2) -> p2.getFechaCreacion().compareTo(p1.getFechaCreacion()))
                .limit(5)
                .toList();

        for (Producto producto : ultimosProductos) {
            actualizaciones.add(new DashboardDTO.UltimaActualizacion(
                    "PRODUCTO",
                    "Nuevo producto: " + producto.getDescripcion(),
                    producto.getFechaCreacion().format(DATE_FORMATTER),
                    "Sistema"
            ));
        }

        actualizaciones.sort((a1, a2) -> a2.getFecha().compareTo(a1.getFecha()));
        return actualizaciones.stream().limit(10).toList();
    }

    private List<DashboardDTO.Movimiento> obtenerMovimientosRecientes() {

        List<MovimientoKardex> movimientos = kardexRepository.findTop50ByOrderByFechaMovimientoDesc();

        return movimientos.stream()
                .limit(10)
                .map(m -> new DashboardDTO.Movimiento(
                        traducirTipoMovimiento(m.getTipoMovimiento()),
                        m.getProducto().getDescripcion(),
                        m.getCantidad(),
                        m.getSede().getNombre(),
                        m.getFechaMovimiento().format(DATE_FORMATTER)
                ))
                .toList();
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