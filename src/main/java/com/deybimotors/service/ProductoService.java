package com.deybimotors.service;

import com.deybimotors.dto.ProductoDTO;
import com.deybimotors.entity.*;
import com.deybimotors.exception.BadRequestException;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Productos - RF-004 a RF-017
 * ACTUALIZADO: Gestión completa con todos los campos de repuestos automotrices
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final MarcaRepository marcaRepository;
    private final StockRepository stockRepository;
    private final ProductoFotoRepository productoFotoRepository;
    private final FileStorageService fileStorageService;
    private final MovimientoKardexRepository kardexRepository;

    /**
     * Listar todos los productos - RF-004
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoResponse> listarTodos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar productos con paginación
     */
    @Transactional(readOnly = true)
    public Page<ProductoDTO.ProductoResponse> listarConPaginacion(Pageable pageable) {
        return productoRepository.findAll(pageable)
                .map(this::convertirADTO);
    }

    /**
     * Listar productos con stock por sede - RF-004
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoConStockResponse> listarConStockPorSede(Long sedeId) {
        List<Producto> productos = productoRepository.findByActivoTrue();

        return productos.stream()
                .map(producto -> {
                    ProductoDTO.ProductoConStockResponse dto = new ProductoDTO.ProductoConStockResponse();
                    // Copiar datos básicos
                    copiarDatosBasicos(producto, dto);

                    // Obtener stock de la sede específica
                    Stock stock = stockRepository.findByProductoIdAndSedeId(producto.getId(), sedeId)
                            .orElse(null);
                    dto.setStockSede(stock != null ? stock.getCantidad() : 0);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Buscar productos con filtros avanzados - RF-012 a RF-014
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoResponse> buscarConFiltros(
            String nombre,
            Long categoriaId,
            Long subcategoriaId,
            Long marcaId,
            String codigo
    ) {
        Specification<Producto> spec = Specification.where(null);

        if (nombre != null && !nombre.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
        }

        if (categoriaId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoria").get("id"), categoriaId));
        }

        if (subcategoriaId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("subcategoria").get("id"), subcategoriaId));
        }

        if (marcaId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("marca").get("id"), marcaId));
        }

        if (codigo != null && !codigo.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("codigo")), "%" + codigo.toLowerCase() + "%"));
        }

        spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), true));

        return productoRepository.findAll(spec).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener producto por ID - RF-006
     */
    @Transactional(readOnly = true)
    public ProductoDTO.ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return convertirADTO(producto);
    }

    /**
     * Obtener producto por código
     */
    @Transactional(readOnly = true)
    public ProductoDTO.ProductoResponse obtenerPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + codigo));
        return convertirADTO(producto);
    }

    /**
     * Crear producto - RF-029
     */
    @Transactional
    public ProductoDTO.ProductoResponse crear(ProductoDTO.ProductoRequest request, Long usuarioId) {

        // Validar que no exista el código
        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new ConflictException("Ya existe un producto con el código: " + request.getCodigo());
        }

        // Validar entidades relacionadas
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Marca marca = marcaRepository.findById(request.getMarcaId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        Subcategoria subcategoria = null;
        if (request.getSubcategoriaId() != null) {
            subcategoria = subcategoriaRepository.findById(request.getSubcategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada"));
        }

        // Crear producto
        Producto producto = new Producto();
        producto.setCodigo(request.getCodigo());
        producto.setCodigoMarca(request.getCodigoMarca());
        producto.setCodigoReferencia(request.getCodigoReferencia());
        producto.setCodigoOem(request.getCodigoOem());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);
        producto.setSubcategoria(subcategoria);
        producto.setMarca(marca);

        // Datos del vehículo
        producto.setMarcaAutomovil(request.getMarcaAutomovil());
        producto.setModeloAutomovil(request.getModeloAutomovil());
        producto.setAnio(request.getAnio());
        producto.setMotor(request.getMotor());

        // Especificaciones técnicas
        producto.setOrigen(request.getOrigen());
        producto.setMedida(request.getMedida());
        producto.setDiametro(request.getDiametro());
        producto.setTipo(request.getTipo());
        producto.setMedida2(request.getMedida2());

        // Precios
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setPrecioCosto(request.getPrecioCosto());
        producto.setCodigoPrecio(request.getCodigoPrecio());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setPublicoCatalogo(request.getPublicoCatalogo());
        producto.setActivo(true);
        producto.setObservaciones(request.getObservaciones());

        Producto guardado = productoRepository.save(producto);
        return convertirADTO(guardado);
    }

    /**
     * Actualizar producto - RF-009
     */
    @Transactional
    public ProductoDTO.ProductoResponse actualizar(Long id, ProductoDTO.ProductoRequest request) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Validar código si cambió
        if (!producto.getCodigo().equals(request.getCodigo())) {
            if (productoRepository.existsByCodigo(request.getCodigo())) {
                throw new ConflictException("Ya existe un producto con el código: " + request.getCodigo());
            }
            producto.setCodigo(request.getCodigo());
        }

        // Validar y actualizar entidades relacionadas
        if (!producto.getCategoria().getId().equals(request.getCategoriaId())) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        if (!producto.getMarca().getId().equals(request.getMarcaId())) {
            Marca marca = marcaRepository.findById(request.getMarcaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
            producto.setMarca(marca);
        }

        if (request.getSubcategoriaId() != null) {
            Subcategoria subcategoria = subcategoriaRepository.findById(request.getSubcategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada"));
            producto.setSubcategoria(subcategoria);
        } else {
            producto.setSubcategoria(null);
        }

        // Actualizar todos los campos
        producto.setCodigoMarca(request.getCodigoMarca());
        producto.setCodigoReferencia(request.getCodigoReferencia());
        producto.setCodigoOem(request.getCodigoOem());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());

        // Datos del vehículo
        producto.setMarcaAutomovil(request.getMarcaAutomovil());
        producto.setModeloAutomovil(request.getModeloAutomovil());
        producto.setAnio(request.getAnio());
        producto.setMotor(request.getMotor());

        // Especificaciones
        producto.setOrigen(request.getOrigen());
        producto.setMedida(request.getMedida());
        producto.setDiametro(request.getDiametro());
        producto.setTipo(request.getTipo());
        producto.setMedida2(request.getMedida2());

        // Precios
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setPrecioCosto(request.getPrecioCosto());
        producto.setCodigoPrecio(request.getCodigoPrecio());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setPublicoCatalogo(request.getPublicoCatalogo());
        producto.setObservaciones(request.getObservaciones());

        Producto actualizado = productoRepository.save(producto);
        return convertirADTO(actualizado);
    }

    /**
     * Edición inline de campo específico - RF-009
     */
    @Transactional
    public ProductoDTO.ProductoResponse actualizarCampo(Long id, String campo, Object valor) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        switch (campo.toLowerCase()) {
            case "nombre":
                producto.setNombre((String) valor);
                break;
            case "precioventa":
                producto.setPrecioVenta(new BigDecimal(valor.toString()));
                break;
            case "preciocosto":
                producto.setPrecioCosto(new BigDecimal(valor.toString()));
                break;
            case "stockminimo":
                producto.setStockMinimo(Integer.parseInt(valor.toString()));
                break;
            case "descripcion":
                producto.setDescripcion((String) valor);
                break;
            case "marcaautomovil":
                producto.setMarcaAutomovil((String) valor);
                break;
            case "modeloautomovil":
                producto.setModeloAutomovil((String) valor);
                break;
            case "motor":
                producto.setMotor((String) valor);
                break;
            case "medida":
                producto.setMedida((String) valor);
                break;
            case "diametro":
                producto.setDiametro((String) valor);
                break;
            default:
                throw new BadRequestException("Campo no válido para edición inline: " + campo);
        }

        Producto actualizado = productoRepository.save(producto);
        return convertirADTO(actualizado);
    }

    /**
     * Eliminar producto - RF-008
     */
    @Transactional
    public void eliminar(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Verificar si tiene movimientos en Kardex - RF-008
        List<MovimientoKardex> movimientos = kardexRepository.findByProductoIdOrderByFechaMovimientoDesc(id);
        if (!movimientos.isEmpty()) {
            throw new BadRequestException(
                    "No se puede eliminar el producto porque tiene movimientos registrados en el historial (Kardex)."
            );
        }

        // Verificar si tiene stock en alguna sede
        List<Stock> stocks = stockRepository.findByProductoId(id);
        boolean tieneStock = stocks.stream().anyMatch(s -> s.getCantidad() > 0);
        if (tieneStock) {
            throw new BadRequestException(
                    "No se puede eliminar el producto porque tiene stock disponible en una o más sedes."
            );
        }

        // Eliminación lógica (recomendada) - RF-008
        producto.setActivo(false);
        productoRepository.save(producto);

        // Opción 2: Eliminación física (descomentar si se requiere)
        // productoRepository.delete(producto);
    }

    /**
     * Subir foto de producto - RF-010
     */
    @Transactional
    public void subirFoto(Long productoId, MultipartFile archivo, boolean esPrincipal) throws IOException {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Guardar archivo
        String rutaArchivo = fileStorageService.guardarImagen(archivo, "productos");

        // Si es principal, desmarcar las demás fotos
        if (esPrincipal) {
            List<ProductoFoto> fotosActuales = productoFotoRepository.findByProductoIdOrderByOrdenAsc(productoId);
            fotosActuales.forEach(foto -> foto.setPrincipal(false));
            productoFotoRepository.saveAll(fotosActuales);
        }

        // Crear registro de foto
        ProductoFoto foto = new ProductoFoto();
        foto.setProducto(producto);
        foto.setRutaArchivo(rutaArchivo);
        foto.setNombreArchivo(archivo.getOriginalFilename());
        foto.setPrincipal(esPrincipal);
        foto.setOrden(producto.getFotos().size());

        productoFotoRepository.save(foto);
    }

    /**
     * Eliminar foto de producto - RF-010
     */
    @Transactional
    public void eliminarFoto(Long fotoId) {

        ProductoFoto foto = productoFotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        // Eliminar archivo físico
        fileStorageService.eliminarArchivo(foto.getRutaArchivo());

        // Eliminar registro
        productoFotoRepository.delete(foto);
    }

    /**
     * Establecer foto principal - RF-010
     */
    @Transactional
    public void establecerFotoPrincipal(Long productoId, Long fotoId) {

        // Desmarcar todas las fotos del producto
        List<ProductoFoto> fotos = productoFotoRepository.findByProductoIdOrderByOrdenAsc(productoId);
        fotos.forEach(foto -> foto.setPrincipal(false));
        productoFotoRepository.saveAll(fotos);

        // Marcar la foto seleccionada como principal
        ProductoFoto fotoPrincipal = productoFotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        fotoPrincipal.setPrincipal(true);
        productoFotoRepository.save(fotoPrincipal);
    }

    /**
     * Listar productos para catálogo público - RF-058 a RF-062
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoCatalogoPublicoResponse> listarCatalogoPublico(
            Long categoriaId,
            Long marcaId
    ) {
        List<Producto> productos = productoRepository.findByPublicoCatalogoTrueAndActivoTrue();

        // Aplicar filtros
        if (categoriaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria().getId().equals(categoriaId))
                    .collect(Collectors.toList());
        }

        if (marcaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getMarca().getId().equals(marcaId))
                    .collect(Collectors.toList());
        }

        return productos.stream()
                .map(this::convertirACatalogoPublicoDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // MÉTODOS AUXILIARES DE CONVERSIÓN DTO
    // ========================================

    private ProductoDTO.ProductoResponse convertirADTO(Producto producto) {
        ProductoDTO.ProductoResponse dto = new ProductoDTO.ProductoResponse();
        copiarDatosBasicos(producto, dto);

        // Calcular stock total en todas las sedes
        List<Stock> stocks = stockRepository.findByProductoId(producto.getId());
        int stockTotal = stocks.stream().mapToInt(Stock::getCantidad).sum();
        dto.setStockTotal(stockTotal);

        return dto;
    }

    private void copiarDatosBasicos(Producto producto, ProductoDTO.ProductoResponse dto) {
        // Identificadores y códigos
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setCodigoMarca(producto.getCodigoMarca());
        dto.setCodigoReferencia(producto.getCodigoReferencia());
        dto.setCodigoOem(producto.getCodigoOem());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());

        // Categorización
        dto.setCategoriaId(producto.getCategoria().getId());
        dto.setCategoriaNombre(producto.getCategoria().getNombre());

        if (producto.getSubcategoria() != null) {
            dto.setSubcategoriaId(producto.getSubcategoria().getId());
            dto.setSubcategoriaNombre(producto.getSubcategoria().getNombre());
        }

        dto.setMarcaId(producto.getMarca().getId());
        dto.setMarcaNombre(producto.getMarca().getNombre());

        // Datos del vehículo
        dto.setMarcaAutomovil(producto.getMarcaAutomovil());
        dto.setModeloAutomovil(producto.getModeloAutomovil());
        dto.setAnio(producto.getAnio());
        dto.setMotor(producto.getMotor());

        // Especificaciones técnicas
        dto.setOrigen(producto.getOrigen());
        dto.setMedida(producto.getMedida());
        dto.setDiametro(producto.getDiametro());
        dto.setTipo(producto.getTipo());
        dto.setMedida2(producto.getMedida2());

        // Precios
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setPrecioCosto(producto.getPrecioCosto());
        dto.setCodigoPrecio(producto.getCodigoPrecio());

        // Control
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setActivo(producto.getActivo());
        dto.setPublicoCatalogo(producto.getPublicoCatalogo());
        dto.setFechaCreacion(producto.getFechaCreacion());

        // Fotos
        List<ProductoFoto> fotos = productoFotoRepository.findByProductoIdOrderByOrdenAsc(producto.getId());
        dto.setFotos(fotos.stream().map(ProductoFoto::getRutaArchivo).collect(Collectors.toList()));

        fotos.stream()
                .filter(ProductoFoto::getPrincipal)
                .findFirst()
                .ifPresent(foto -> dto.setFotoPrincipal(foto.getRutaArchivo()));
    }

    private ProductoDTO.ProductoCatalogoPublicoResponse convertirACatalogoPublicoDTO(Producto producto) {
        ProductoDTO.ProductoCatalogoPublicoResponse dto = new ProductoDTO.ProductoCatalogoPublicoResponse();

        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());

        // Categorización
        dto.setCategoriaNombre(producto.getCategoria().getNombre());
        dto.setSubcategoriaNombre(producto.getSubcategoria() != null ? producto.getSubcategoria().getNombre() : null);
        dto.setMarcaNombre(producto.getMarca().getNombre());

        // Datos del vehículo
        dto.setMarcaAutomovil(producto.getMarcaAutomovil());
        dto.setModeloAutomovil(producto.getModeloAutomovil());
        dto.setMotor(producto.getMotor());

        // Especificaciones técnicas (las más relevantes para catálogo público)
        dto.setOrigen(producto.getOrigen());
        dto.setMedida(producto.getMedida());
        dto.setDiametro(producto.getDiametro());
        dto.setTipo(producto.getTipo());

        // Precio
        dto.setPrecioVenta(producto.getPrecioVenta());

        // Calcular disponibilidad - RF-060 (Semáforo)
        List<Stock> stocks = stockRepository.findByProductoId(producto.getId());
        int stockTotal = stocks.stream().mapToInt(Stock::getCantidad).sum();

        if (stockTotal == 0) {
            dto.setDisponibilidad("AGOTADO");
        } else if (stockTotal <= producto.getStockMinimo()) {
            dto.setDisponibilidad("ULTIMAS_UNIDADES");
        } else {
            dto.setDisponibilidad("DISPONIBLE");
        }

        // Fotos
        List<ProductoFoto> fotos = productoFotoRepository.findByProductoIdOrderByOrdenAsc(producto.getId());
        dto.setFotos(fotos.stream().map(ProductoFoto::getRutaArchivo).collect(Collectors.toList()));

        fotos.stream()
                .filter(ProductoFoto::getPrincipal)
                .findFirst()
                .ifPresent(foto -> dto.setFotoPrincipal(foto.getRutaArchivo()));

        return dto;
    }
}