package com.deybimotors.service;

import com.deybimotors.dto.CompraDTO;
import com.deybimotors.entity.*;
import com.deybimotors.exception.BadRequestException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Compras - ✅ ACTUALIZADO
 * Corregido para trabajar con campos reales de BD
 */
@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final CompraDetalleRepository compraDetalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final SedeRepository sedeRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StockService stockService;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<CompraDTO.CompraResponse> listarTodas() {
        return compraRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompraDTO.CompraResponse> listarUltimas() {
        return compraRepository.findTop10ByOrderByFechaRegistroDesc().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompraDTO.CompraResponse> listarPorEstado(String estado) {
        Compra.EstadoCompra estadoCompra = Compra.EstadoCompra.valueOf(estado.toUpperCase());
        return compraRepository.findByEstado(estadoCompra).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompraDTO.CompraResponse> listarPorProveedor(Long proveedorId) {
        return compraRepository.findByProveedorId(proveedorId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompraDTO.CompraResponse> listarPorSede(Long sedeId) {
        return compraRepository.findBySedeId(sedeId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompraDTO.CompraResponse> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return compraRepository.findByFechaRegistroBetween(fechaInicio, fechaFin).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompraDTO.CompraResponse obtenerPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
        return convertirADTO(compra);
    }

    /**
     * Crear nueva compra - RF-025 a RF-027
     */
    @Transactional
    public CompraDTO.CompraResponse crear(CompraDTO.CrearCompraRequest request, Long usuarioId) {

        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new BadRequestException("La compra debe tener al menos un producto");
        }

        // Crear compra
        Compra compra = new Compra();
        compra.setNumeroCompra(generarNumeroCompra());
        compra.setProveedor(proveedor);
        compra.setSede(sede);
        compra.setEstado(Compra.EstadoCompra.PENDIENTE);
        compra.setUsuarioRegistro(usuario);
        compra.setObservaciones(request.getObservaciones());

        // Calcular monto total y crear detalles
        BigDecimal montoTotal = BigDecimal.ZERO;

        for (CompraDTO.CompraDetalleRequest detalleRequest : request.getDetalles()) {

            Producto producto = productoRepository.findById(detalleRequest.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: ID " + detalleRequest.getProductoId()));

            BigDecimal subtotal = detalleRequest.getPrecioUnitario()
                    .multiply(new BigDecimal(detalleRequest.getCantidad()));

            CompraDetalle detalle = new CompraDetalle();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleRequest.getCantidad());
            detalle.setPrecioUnitario(detalleRequest.getPrecioUnitario());
            detalle.setObservaciones(detalleRequest.getObservaciones());

            compra.getDetalles().add(detalle);
            montoTotal = montoTotal.add(subtotal);
        }

        compra.setMontoTotal(montoTotal);

        Compra guardada = compraRepository.save(compra);

        return convertirADTO(guardada);
    }

    /**
     * Subir factura de compra - RF-025
     */
    @Transactional
    public void subirFactura(Long compraId, MultipartFile archivo) throws IOException {

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        String rutaArchivo = fileStorageService.guardarDocumento(archivo, "facturas");

        compra.setRutaFactura(rutaArchivo);
        compraRepository.save(compra);
    }

    /**
     * Actualizar estado de compra - RF-030
     * CRÍTICO: Solo actualiza stock cuando estado = COMPLETADO
     */
    @Transactional
    public CompraDTO.CompraResponse actualizarEstado(Long id, CompraDTO.ActualizarEstadoCompraRequest request, Long usuarioId) {

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        Compra.EstadoCompra estadoAnterior = compra.getEstado();
        Compra.EstadoCompra estadoNuevo = Compra.EstadoCompra.valueOf(request.getEstado().toUpperCase());

        if (estadoAnterior == Compra.EstadoCompra.COMPLETADO) {
            throw new BadRequestException("No se puede modificar una compra completada");
        }

        if (estadoAnterior == Compra.EstadoCompra.CANCELADO) {
            throw new BadRequestException("No se puede modificar una compra cancelada");
        }

        // Si el nuevo estado es COMPLETADO, actualizar stock
        if (estadoNuevo == Compra.EstadoCompra.COMPLETADO && estadoAnterior != Compra.EstadoCompra.COMPLETADO) {

            for (CompraDetalle detalle : compra.getDetalles()) {
                stockService.incrementarStock(
                        detalle.getProducto().getId(),
                        compra.getSede().getId(),
                        detalle.getCantidad(),
                        usuarioId,
                        compra.getNumeroCompra()
                );
            }
        }

        compra.setEstado(estadoNuevo);
        Compra actualizada = compraRepository.save(compra);

        return convertirADTO(actualizada);
    }

    /**
     * Eliminar compra (solo si está en estado PENDIENTE)
     */
    @Transactional
    public void eliminar(Long id) {

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        if (compra.getEstado() != Compra.EstadoCompra.PENDIENTE) {
            throw new BadRequestException("Solo se pueden eliminar compras en estado PENDIENTE");
        }

        if (compra.getRutaFactura() != null) {
            fileStorageService.eliminarArchivo(compra.getRutaFactura());
        }

        compraRepository.delete(compra);
    }

    /**
     * Generar número de compra automático
     * Formato: CMP-2024-0001
     */
    private String generarNumeroCompra() {
        String year = String.valueOf(Year.now().getValue());
        Integer ultimoNumero = compraRepository.findUltimoNumeroCompraDelAnio(year);

        int nuevoNumero = (ultimoNumero != null ? ultimoNumero : 0) + 1;

        return String.format("CMP-%s-%04d", year, nuevoNumero);
    }

    // Convertir entidad a DTO
    private CompraDTO.CompraResponse convertirADTO(Compra compra) {

        List<CompraDTO.CompraDetalleResponse> detalles = compra.getDetalles().stream()
                .map(this::convertirDetalleADTO)
                .collect(Collectors.toList());

        return new CompraDTO.CompraResponse(
                compra.getId(),
                compra.getNumeroCompra(),
                compra.getProveedor().getId(),
                compra.getProveedor().getNombreEmpresa(),
                compra.getSede().getId(),
                compra.getSede().getNombre(),
                compra.getEstado().name(),
                compra.getMontoTotal(),
                compra.getRutaFactura(),
                compra.getFechaRegistro(),
                compra.getUsuarioRegistro().getNombreCompleto(),
                compra.getObservaciones(),
                detalles
        );
    }

    private CompraDTO.CompraDetalleResponse convertirDetalleADTO(CompraDetalle detalle) {
        return new CompraDTO.CompraDetalleResponse(
                detalle.getId(),
                detalle.getProducto().getId(),
                detalle.getProducto().getCodigo(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal(),
                detalle.getObservaciones()
        );
    }
}