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
 * Servicio de Stock - ✅ ACTUALIZADO
 * ❌ SIN validaciones de stockMinimo
 * ✅ Stock bajo = stock <= 2 (valor fijo)
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
                producto.getFechaCreacion(),
                calcularEstadoStock(producto)
        );

        return List.of(response);
    }

    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerStockSede(Long sedeId) {

        Sede sede = sedeRepository.findById(sedeId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        List<Producto> productos = productoRepository.findBySedeId(sedeId);

        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerProductosSinStock(Long sedeId) {

        List<Producto> productos = productoRepository.findProductosSinStockPorSede(sedeId);

        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ ACTUALIZADO: Stock bajo = stock <= 2
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerProductosStockBajo(Long sedeId) {

        List<Producto> productos = productoRepository.findProductosStockBajoPorSede(sedeId);

        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockDTO.StockResponse ajustarStock(StockDTO.AjusteStockRequest request, Long usuarioId) {

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!producto.getSede().getId().equals(request.getSedeId())) {
            throw new BadRequestException("El producto pertenece a otra sede");
        }

        if (request.getCantidadNueva() < 0) {
            throw new BadRequestException("La cantidad no puede ser negativa");
        }

        int stockAnterior = producto.getStock();
        int diferencia = request.getCantidadNueva() - stockAnterior;

        producto.setStock(request.getCantidadNueva());
        Producto actualizado = productoRepository.save(producto);

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

    @Transactional
    public void registrarSalida(StockDTO.ConfirmarSalidaRequest request, Long usuarioId) {

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<String> errores = new ArrayList<>();

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

        Salida salida = new Salida();
        salida.setSede(sede);
        salida.setMotivo(request.getMotivo());
        salida.setObservacion(request.getObservaciones());
        salida.setUsuario(usuario);
        Salida salidaGuardada = salidaRepository.save(salida);

        for (StockDTO.SalidaStockRequest item : request.getProductos()) {

            Producto producto = productoRepository.findById(item.getProductoId()).get();
            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior - item.getCantidad();

            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            DetalleSalida detalle = new DetalleSalida();
            detalle.setSalida(salidaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalleSalidaRepository.save(detalle);

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

        producto.setStock(stockNuevo);
        productoRepository.save(producto);

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

    /**
     * ✅ ACTUALIZADO: Stock bajo = stock <= 2 (sin usar stockMinimo)
     */
    private String calcularEstadoStock(Producto producto) {
        if (producto.getStock() == 0) {
            return "AGOTADO";
        } else if (producto.getStock() <= 2) {
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
                producto.getFechaCreacion(),
                calcularEstadoStock(producto)
        );
    }
}