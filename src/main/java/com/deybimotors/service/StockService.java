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
 * Servicio de Stock - RF-011, RF-018, RF-019
 * Gestión de inventario y movimientos
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProductoRepository productoRepository;
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoKardexRepository kardexRepository;

    /**
     * Obtener stock de un producto en todas las sedes
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerStockProducto(Long productoId) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        List<Stock> stocks = stockRepository.findByProductoId(productoId);

        return stocks.stream()
                .map(stock -> convertirADTO(stock, producto))
                .collect(Collectors.toList());
    }

    /**
     * Obtener stock de una sede específica
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerStockSede(Long sedeId) {

        Sede sede = sedeRepository.findById(sedeId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        List<Stock> stocks = stockRepository.findBySedeId(sedeId);

        return stocks.stream()
                .map(stock -> convertirADTO(stock, stock.getProducto()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos sin stock en una sede
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerProductosSinStock(Long sedeId) {

        List<Stock> stocks = stockRepository.findProductosSinStockPorSede(sedeId);

        return stocks.stream()
                .map(stock -> convertirADTO(stock, stock.getProducto()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener productos con stock bajo en una sede
     */
    @Transactional(readOnly = true)
    public List<StockDTO.StockResponse> obtenerProductosStockBajo(Long sedeId) {

        List<Stock> stocks = stockRepository.findProductosStockBajoPorSede(sedeId);

        return stocks.stream()
                .map(stock -> convertirADTO(stock, stock.getProducto()))
                .collect(Collectors.toList());
    }

    /**
     * Ajustar stock manualmente - RF-011
     */
    @Transactional
    public StockDTO.StockResponse ajustarStock(StockDTO.AjusteStockRequest request, Long usuarioId) {

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Obtener o crear stock
        Stock stock = stockRepository.findByProductoIdAndSedeId(request.getProductoId(), request.getSedeId())
                .orElseGet(() -> {
                    Stock nuevoStock = new Stock();
                    nuevoStock.setProducto(producto);
                    nuevoStock.setSede(sede);
                    nuevoStock.setCantidad(0);
                    return nuevoStock;
                });

        int stockAnterior = stock.getCantidad();
        int diferencia = request.getCantidadNueva() - stockAnterior;

        // Validar que la cantidad nueva no sea negativa
        if (request.getCantidadNueva() < 0) {
            throw new BadRequestException("La cantidad no puede ser negativa");
        }

        // Actualizar stock
        stock.setCantidad(request.getCantidadNueva());
        Stock actualizado = stockRepository.save(stock);

        // Registrar movimiento en Kardex - RF-039
        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setProducto(producto);
        movimiento.setSede(sede);
        movimiento.setTipoMovimiento(
                diferencia > 0 ?
                        MovimientoKardex.TipoMovimiento.AJUSTE_POSITIVO :
                        MovimientoKardex.TipoMovimiento.AJUSTE_NEGATIVO
        );
        movimiento.setCantidad(Math.abs(diferencia));
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(request.getCantidadNueva());
        movimiento.setMotivo(request.getMotivo());
        movimiento.setUsuarioResponsable(usuario);
        movimiento.setObservaciones(request.getObservaciones());

        kardexRepository.save(movimiento);

        return convertirADTO(actualizado, producto);
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

            Stock stock = stockRepository.findByProductoIdAndSedeId(item.getProductoId(), request.getSedeId())
                    .orElse(null);

            if (stock == null || stock.getCantidad() < item.getCantidad()) {
                errores.add(String.format("Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d",
                        producto.getNombre(),
                        stock != null ? stock.getCantidad() : 0,
                        item.getCantidad()));
            }
        }

        if (!errores.isEmpty()) {
            throw new InsufficientStockException("Errores en la salida: " + String.join("; ", errores));
        }

        // Registrar salidas
        for (StockDTO.SalidaStockRequest item : request.getProductos()) {

            Producto producto = productoRepository.findById(item.getProductoId()).get();
            Stock stock = stockRepository.findByProductoIdAndSedeId(item.getProductoId(), request.getSedeId()).get();

            int stockAnterior = stock.getCantidad();
            int stockNuevo = stockAnterior - item.getCantidad();

            // Actualizar stock
            stock.setCantidad(stockNuevo);
            stockRepository.save(stock);

            // Registrar en Kardex
            MovimientoKardex movimiento = new MovimientoKardex();
            movimiento.setProducto(producto);
            movimiento.setSede(sede);

            // Determinar tipo de movimiento según el motivo
            switch (request.getMotivo().toUpperCase()) {
                case "VENTA":
                    movimiento.setTipoMovimiento(MovimientoKardex.TipoMovimiento.SALIDA_VENTA);
                    break;
                case "TRASLADO":
                    movimiento.setTipoMovimiento(MovimientoKardex.TipoMovimiento.TRASLADO_SALIDA);
                    break;
                default:
                    movimiento.setTipoMovimiento(MovimientoKardex.TipoMovimiento.AJUSTE_NEGATIVO);
            }
            movimiento.setCantidad(item.getCantidad());
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockNuevo(stockNuevo);
            movimiento.setMotivo(request.getMotivo());
            movimiento.setUsuarioResponsable(usuario);
            movimiento.setObservaciones(request.getObservaciones());

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

        Sede sede = sedeRepository.findById(sedeId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Obtener o crear stock
        Stock stock = stockRepository.findByProductoIdAndSedeId(productoId, sedeId)
                .orElseGet(() -> {
                    Stock nuevoStock = new Stock();
                    nuevoStock.setProducto(producto);
                    nuevoStock.setSede(sede);
                    nuevoStock.setCantidad(0);
                    return nuevoStock;
                });

        int stockAnterior = stock.getCantidad();
        int stockNuevo = stockAnterior + cantidad;

        // Actualizar stock
        stock.setCantidad(stockNuevo);
        stockRepository.save(stock);

        // Registrar en Kardex
        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setProducto(producto);
        movimiento.setSede(sede);
        movimiento.setTipoMovimiento(MovimientoKardex.TipoMovimiento.ENTRADA_COMPRA);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo("Entrada por compra");
        movimiento.setUsuarioResponsable(usuario);
        movimiento.setReferencia(referencia);

        kardexRepository.save(movimiento);
    }

    // Convertir entidad a DTO
    private StockDTO.StockResponse convertirADTO(Stock stock, Producto producto) {

        String estado;
        if (stock.getCantidad() == 0) {
            estado = "AGOTADO";
        } else if (stock.getCantidad() <= producto.getStockMinimo()) {
            estado = "BAJO";
        } else {
            estado = "NORMAL";
        }

        return new StockDTO.StockResponse(
                stock.getId(),
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                stock.getSede().getId(),
                stock.getSede().getNombre(),
                stock.getCantidad(),
                producto.getStockMinimo(),
                stock.getFechaActualizacion(),
                estado
        );
    }
}