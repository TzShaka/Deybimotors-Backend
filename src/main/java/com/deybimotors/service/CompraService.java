package com.deybimotors.service;

import com.deybimotors.dto.CompraDTO;
import com.deybimotors.entity.*;
import com.deybimotors.exception.BadRequestException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Servicio de Compras
 * ✅ ACTUALIZADO: Usa Cloudinary para almacenamiento global
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompraService {

    private final CompraRepository compraRepository;
    private final CompraDetalleRepository compraDetalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final SedeRepository sedeRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StockService stockService;

    // ✅ CAMBIO: Ahora usa Cloudinary en lugar de FileStorageService local
    private final CloudinaryFileStorageService cloudinaryService;

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

        log.info("✅ Compra creada: {} - Total: {}", guardada.getNumeroCompra(), guardada.getMontoTotal());
        return convertirADTO(guardada);
    }

    /**
     * Actualizar compra (proveedor y observaciones)
     */
    @Transactional
    public CompraDTO.CompraResponse actualizar(Long id, CompraDTO.ActualizarCompraRequest request) {

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        if (compra.getEstado() != Compra.EstadoCompra.PENDIENTE) {
            throw new BadRequestException("Solo se pueden actualizar compras en estado PENDIENTE");
        }

        if (!compra.getProveedor().getId().equals(request.getProveedorId())) {
            Proveedor nuevoProveedor = proveedorRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
            compra.setProveedor(nuevoProveedor);
        }

        compra.setObservaciones(request.getObservaciones());
        Compra actualizada = compraRepository.save(compra);

        log.info("✅ Compra actualizada: {}", actualizada.getNumeroCompra());
        return convertirADTO(actualizada);
    }

    /**
     * ✅ ACTUALIZADO: Subir factura a Cloudinary
     */
    @Transactional
    public CompraDTO.CompraResponse subirFactura(Long compraId, MultipartFile archivo) throws IOException {

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        // Si ya tiene archivo, eliminar el anterior de Cloudinary
        if (compra.getRutaFactura() != null) {
            cloudinaryService.eliminarArchivo(compra.getRutaFactura());
            log.info("🗑️ Archivo anterior eliminado de Cloudinary");
        }

        // ✅ Subir a Cloudinary y obtener URL pública global
        String urlPublica = cloudinaryService.subirDocumento(archivo, "facturas");

        // Guardar URL en BD
        compra.setRutaFactura(urlPublica);
        Compra actualizada = compraRepository.save(compra);

        log.info("✅ Factura subida a Cloudinary: {} -> {}", compra.getNumeroCompra(), urlPublica);
        return convertirADTO(actualizada);
    }

    /**
     * ✅ ACTUALIZADO: Eliminar factura de Cloudinary
     */
    @Transactional
    public CompraDTO.CompraResponse eliminarFactura(Long compraId) {

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        if (compra.getRutaFactura() == null) {
            throw new BadRequestException("La compra no tiene archivo adjunto");
        }

        // Eliminar de Cloudinary
        cloudinaryService.eliminarArchivo(compra.getRutaFactura());

        // Eliminar referencia en BD
        compra.setRutaFactura(null);
        Compra actualizada = compraRepository.save(compra);

        log.info("🗑️ Factura eliminada de Cloudinary para compra: {}", actualizada.getNumeroCompra());
        return convertirADTO(actualizada);
    }

    /**
     * Actualizar estado de compra
     */
    @Transactional
    public CompraDTO.CompraResponse actualizarEstado(Long id, CompraDTO.ActualizarEstadoCompraRequest request, Long usuarioId) {

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        Compra.EstadoCompra estadoAnterior = compra.getEstado();
        Compra.EstadoCompra estadoNuevo = Compra.EstadoCompra.valueOf(request.getEstado().toUpperCase());

        if (estadoAnterior == Compra.EstadoCompra.PAGADO) {
            throw new BadRequestException("No se puede modificar una compra completada");
        }

        if (estadoNuevo == Compra.EstadoCompra.PAGADO && estadoAnterior != Compra.EstadoCompra.PAGADO) {

            for (CompraDetalle detalle : compra.getDetalles()) {
                stockService.incrementarStock(
                        detalle.getProducto().getId(),
                        compra.getSede().getId(),
                        detalle.getCantidad(),
                        usuarioId,
                        compra.getNumeroCompra()
                );
            }

            log.info("✅ Stock actualizado para compra: {}", compra.getNumeroCompra());
        }

        compra.setEstado(estadoNuevo);
        Compra actualizada = compraRepository.save(compra);

        log.info("✅ Estado actualizado: {} -> {}", estadoAnterior, estadoNuevo);
        return convertirADTO(actualizada);
    }

    /**
     * Eliminar compra
     */
    @Transactional
    public void eliminar(Long id) {

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada"));

        if (compra.getEstado() != Compra.EstadoCompra.PENDIENTE) {
            throw new BadRequestException("Solo se pueden eliminar compras en estado PENDIENTE");
        }

        // Eliminar archivo de Cloudinary si existe
        if (compra.getRutaFactura() != null) {
            cloudinaryService.eliminarArchivo(compra.getRutaFactura());
            log.info("🗑️ Archivo eliminado de Cloudinary");
        }

        compraRepository.delete(compra);
        log.info("🗑️ Compra eliminada: {}", compra.getNumeroCompra());
    }

    /**
     * Generar número de compra automático
     */
    private String generarNumeroCompra() {
        String year = String.valueOf(Year.now().getValue());
        Integer ultimoNumero = compraRepository.findUltimoNumeroCompraDelAnio(year);

        int nuevoNumero = (ultimoNumero != null ? ultimoNumero : 0) + 1;

        return String.format("CMP-%s-%04d", year, nuevoNumero);
    }

    /**
     * ✅ ACTUALIZADO: Convertir entidad a DTO
     * La URL de Cloudinary ya viene completa y es global
     */
    private CompraDTO.CompraResponse convertirADTO(Compra compra) {

        List<CompraDTO.CompraDetalleResponse> detalles = compra.getDetalles().stream()
                .map(this::convertirDetalleADTO)
                .collect(Collectors.toList());

        // ✅ La URL de Cloudinary YA ES la URL completa y global
        // Ejemplo: https://res.cloudinary.com/drdet81ws/image/upload/v123/deybimotors/facturas/archivo.jpg
        String archivoUrl = compra.getRutaFactura();

        String archivoNombre = null;
        if (archivoUrl != null) {
            // Extraer nombre del archivo de la URL
            String[] partes = archivoUrl.split("/");
            archivoNombre = partes[partes.length - 1];
        }

        CompraDTO.CompraResponse response = new CompraDTO.CompraResponse();
        response.setId(compra.getId());
        response.setNumeroCompra(compra.getNumeroCompra());
        response.setProveedorId(compra.getProveedor().getId());
        response.setProveedorNombre(compra.getProveedor().getNombreEmpresa());
        response.setSedeId(compra.getSede().getId());
        response.setSedeNombre(compra.getSede().getNombre());
        response.setEstado(compra.getEstado().name());
        response.setMontoTotal(compra.getMontoTotal());
        response.setRutaFactura(compra.getRutaFactura());

        // URLs globales de Cloudinary
        response.setArchivoUrl(archivoUrl);
        response.setArchivoNombre(archivoNombre);

        response.setFechaRegistro(compra.getFechaRegistro());
        response.setUsuarioRegistro(compra.getUsuarioRegistro().getNombreCompleto());
        response.setObservaciones(compra.getObservaciones());
        response.setDetalles(detalles);

        return response;
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