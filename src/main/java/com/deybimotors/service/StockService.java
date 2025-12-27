package com.deybimotors.service;

import com.deybimotors.dto.StockDTO;
import com.deybimotors.entity.*;
import com.deybimotors.exception.BadRequestException;
import com.deybimotors.exception.InsufficientStockException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Stock - ✅ REESCRITO COMPLETO
 * Trabaja directamente con productos.stock (NO hay tabla stock separada)
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductoRepository productoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoKardexRepository kardexRepository;
    private final SalidaRepository salidaRepository;
    private final DetalleSalidaRepository detalleSalidaRepository;

    /**
     * Obtener stock de un producto (en su sede asignada)
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerStockProducto(Long productoId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        StockDTO.StockResponse response = new StockDTO.StockResponse(
                producto.getId(),
                producto.getId(),
                producto.getCodigoInterno(),
                producto.getDescripcion(),
                producto.getSede().getId(),
                producto.getSede().getNombre(),
                producto.getStock(),
                producto.getStockMinimo(),
                producto.getFechaCreacion(),
                calcularEstadoStock(producto)
        );

        return List.of(response);
    }

    /**
     * Obtener stock de una sede específica
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerStockSede(Long sedeId) {

        Sede sede = sedeRepository.findById(sedeId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        List<Producto> productos = productoRepository.findBySedeId(sedeId);

        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos sin stock en una sede
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerProductosSinStock(Long sedeId) {

        List<Producto> productos = productoRepository.findProductosSinStockPorSede(sedeId);

        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos con stock bajo en una sede
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerProductosStockBajo(Long sedeId) {

        List<Producto> productos = productoRepository.findProductosStockBajoPorSede(sedeId);

        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Ajustar stock manualmente - RF-011
     */
    @Transactional
    public StockDTO.StockResponse ajustarStock(StockDTO.AjusteStockRequest request, Long usuarioId) {

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar que la sede del ajuste coincida con la sede del producto
        if (!producto.getSede().getId().equals(request.getSedeId())) {
            throw new BadRequestException("El producto pertenece a otra sede");
        }

        if (request.getCantidadNueva() < 0) {
            throw new BadRequestException("La cantidad no puede ser negativa");
        }

        int stockAnterior = producto.getStock();
        int diferencia = request.getCantidadNueva() - stockAnterior;

        // Actualizar stock
        producto.setStock(request.getCantidadNueva());
        Producto actualizado = productoRepository.save(producto);

        // Registrar movimiento en Kardex
        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setProducto(producto);
        movimiento.setSede(producto.getSede());
        movimiento.setTipoMovimiento(diferencia > 0 ? "AJUSTE_POSITIVO" : "AJUSTE_NEGATIVO");
        movimiento.setCantidad(Math.abs(diferencia));
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockActual(request.getCantidadNueva());
        movimiento.setUsuarioResponsable(usuario);
        movimiento.setReferenciaTabla(request.getMotivo());

        kardexRepository.save(movimiento);

        return convertirADTO(actualizado);
    }

    /**
     * Registrar salida de productos (carrito) - RF-018, RF-019
     */
    @Transactional
    public void registrarSalida(StockDTO.ConfirmarSalidaRequest request, Long usuarioId) {

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<String> errores = new ArrayList<>();

        // Validar stock disponible para todos los productos
        for (StockDTO.SalidaStockRequest item : request.getProductos()) {

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: ID " + item.getProductoId()));

            if (!producto.getSede().getId().equals(request.getSedeId())) {
                errores.add(String.format("El producto '%s' no pertenece a esta sede", producto.getDescripcion()));
                continue;
            }

            if (producto.getStock() < item.getCantidad()) {
                errores.add(String.format("Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d",
                        producto.getDescripcion(),
                        producto.getStock(),
                        item.getCantidad()));
            }
        }

        if (!errores.isEmpty()) {
            throw new InsufficientStockException("Errores en la salida: " + String.join("; ", errores));
        }

        // Crear cabecera de salida
        Salida salida = new Salida();
        salida.setSede(sede);
        salida.setMotivo(request.getMotivo());
        salida.setObservacion(request.getObservaciones());
        salida.setUsuario(usuario);
        Salida salidaGuardada = salidaRepository.save(salida);

        // Registrar salidas
        for (StockDTO.SalidaStockRequest item : request.getProductos()) {

            Producto producto = productoRepository.findById(item.getProductoId()).get();
            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior - item.getCantidad();

            // Actualizar stock
            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            // Crear detalle de salida
            DetalleSalida detalle = new DetalleSalida();
            detalle.setSalida(salidaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalleSalidaRepository.save(detalle);

            // Registrar en Kardex
            MovimientoKardex movimiento = new MovimientoKardex();
            movimiento.setProducto(producto);
            movimiento.setSede(sede);
            movimiento.setTipoMovimiento(determinarTipoMovimiento(request.getMotivo()));
            movimiento.setCantidad(item.getCantidad());
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockActual(stockNuevo);
            movimiento.setUsuarioResponsable(usuario);
            movimiento.setReferenciaTabla("salidas");
            movimiento.setReferenciaId(salidaGuardada.getId());

            kardexRepository.save(movimiento);
        }
    }

    /**
     * Incrementar stock (usado en compras) - RF-030
     */
    @Transactional
    public void incrementarStock(Long productoId, Long sedeId, Integer cantidad, Long usuarioId, String referencia) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!producto.getSede().getId().equals(sedeId)) {
            throw new BadRequestException("El producto no pertenece a esta sede");
        }

        int stockAnterior = producto.getStock();
        int stockNuevo = stockAnterior + cantidad;

        // Actualizar stock
        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        // Registrar en Kardex
        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setProducto(producto);
        movimiento.setSede(producto.getSede());
        movimiento.setTipoMovimiento("ENTRADA_COMPRA");
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockActual(stockNuevo);
        movimiento.setUsuarioResponsable(usuario);
        movimiento.setReferenciaTabla(referencia);

        kardexRepository.save(movimiento);
    }

    // MÉTODOS AUXILIARES

    private String determinarTipoMovimiento(String motivo) {
        return switch (motivo.toUpperCase()) {
            case "VENTA" -> "SALIDA_VENTA";
            case "TRASLADO" -> "TRASLADO_SALIDA";
            default -> "AJUSTE_NEGATIVO";
        };
    }

    private String calcularEstadoStock(Producto producto) {
        if (producto.getStock() == 0) {
            return "AGOTADO";
        } else if (producto.getStock() <= producto.getStockMinimo()) {
            return "BAJO";
        } else {
            return "NORMAL";
        }
    }

    private StockDTO.StockResponse convertirADTO(Producto producto) {
        return new StockDTO.StockResponse(
                producto.getId(),
                producto.getId(),
                producto.getCodigoInterno(),
                producto.getDescripcion(),
                producto.getSede().getId(),
                producto.getSede().getNombre(),
                producto.getStock(),
                producto.getStockMinimo(),
                producto.getFechaCreacion(),
                calcularEstadoStock(producto)
        );
    }
}