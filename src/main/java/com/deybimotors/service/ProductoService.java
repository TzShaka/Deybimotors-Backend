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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Productos - RF-004 a RF-017
 * COMPLETAMENTE REESCRITO para la estructura real de BD
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final MarcaRepository marcaRepository;
    private final SedeRepository sedeRepository;
    private final OrigenRepository origenRepository;
    private final CodigoPrecioRepository codigoPrecioRepository;
    private final ImagenProductoRepository imagenProductoRepository;
    private final MovimientoKardexRepository kardexRepository;
    private final FileStorageService fileStorageService;

    /**
     * Listar todos los productos - RF-004
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoResponse> listarTodos() {
        return productoRepository.findByEstadoTrue().stream()
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
        List<Producto> productos = productoRepository.findBySedeId(sedeId);

        return productos.stream()
                .map(producto -> {
                    ProductoDTO.ProductoConStockResponse dto = new ProductoDTO.ProductoConStockResponse();
                    copiarDatosBasicos(producto, dto);
                    dto.setStockSede(producto.getStock()); // Stock de la sede
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
                    cb.like(cb.lower(root.get("descripcion")), "%" + nombre.toLowerCase() + "%"));
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
                    cb.equal(root.get("marcaProducto").get("id"), marcaId));
        }

        if (codigo != null && !codigo.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("codigoInterno")), "%" + codigo.toLowerCase() + "%"));
        }

        spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), true));

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
        Producto producto = productoRepository.findByCodigoInterno(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + codigo));
        return convertirADTO(producto);
    }

    /**
     * Crear producto - RF-029
     */
    @Transactional
    public ProductoDTO.ProductoResponse crear(ProductoDTO.ProductoRequest request, Long usuarioId) {

        // Validar que no exista el código
        if (productoRepository.existsByCodigoInterno(request.getCodigo())) {
            throw new ConflictException("Ya existe un producto con el código: " + request.getCodigo());
        }

        // Validar entidades relacionadas
        Sede sede = sedeRepository.findById(request.getSedeId() != null ? request.getSedeId() : 1L)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Marca marca = marcaRepository.findById(request.getMarcaId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        Subcategoria subcategoria = null;
        if (request.getSubcategoriaId() != null) {
            subcategoria = subcategoriaRepository.findById(request.getSubcategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada"));
        }

        // Buscar o crear origen
        Origen origen = null;
        if (request.getOrigen() != null && !request.getOrigen().isEmpty()) {
            origen = origenRepository.findByPais(request.getOrigen())
                    .orElseGet(() -> {
                        Origen nuevoOrigen = new Origen();
                        nuevoOrigen.setPais(request.getOrigen());
                        return origenRepository.save(nuevoOrigen);
                    });
        }

        // Buscar o crear código de precio
        CodigoPrecio codigoPrecio = null;
        if (request.getCodigoPrecio() != null && !request.getCodigoPrecio().isEmpty()) {
            codigoPrecio = codigoPrecioRepository.findByCodigo(request.getCodigoPrecio())
                    .orElseGet(() -> {
                        CodigoPrecio nuevoCodigo = new CodigoPrecio();
                        nuevoCodigo.setCodigo(request.getCodigoPrecio());
                        return codigoPrecioRepository.save(nuevoCodigo);
                    });
        }

        // Crear producto
        Producto producto = new Producto();
        producto.setSede(sede);
        producto.setCodigoInterno(request.getCodigo());
        producto.setCodigoMarca(request.getCodigoMarca());
        producto.setCodigoReferencia(request.getCodigoReferencia());
        producto.setDescripcion(request.getNombre());
        producto.setCategoria(categoria);
        producto.setSubcategoria(subcategoria);
        producto.setMarcaProducto(marca);
        producto.setOrigen(origen);
        producto.setMedida(request.getMedida());
        producto.setDiametro(request.getDiametro());
        producto.setTipo(request.getTipo());
        producto.setMedida2(request.getMedida2());
        producto.setStock(0); // Stock inicial
        producto.setStockMinimo(request.getStockMinimo());
        producto.setCodigoPrecio(codigoPrecio);
        producto.setPrecioCosto(request.getPrecioCosto());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setEstado(true);

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
        if (!producto.getCodigoInterno().equals(request.getCodigo())) {
            if (productoRepository.existsByCodigoInterno(request.getCodigo())) {
                throw new ConflictException("Ya existe un producto con el código: " + request.getCodigo());
            }
            producto.setCodigoInterno(request.getCodigo());
        }

        // Actualizar entidades relacionadas
        if (!producto.getCategoria().getId().equals(request.getCategoriaId())) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        if (!producto.getMarcaProducto().getId().equals(request.getMarcaId())) {
            Marca marca = marcaRepository.findById(request.getMarcaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
            producto.setMarcaProducto(marca);
        }

        if (request.getSubcategoriaId() != null) {
            Subcategoria subcategoria = subcategoriaRepository.findById(request.getSubcategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada"));
            producto.setSubcategoria(subcategoria);
        } else {
            producto.setSubcategoria(null);
        }

        // Actualizar campos
        producto.setCodigoMarca(request.getCodigoMarca());
        producto.setCodigoReferencia(request.getCodigoReferencia());
        producto.setDescripcion(request.getNombre());
        producto.setMedida(request.getMedida());
        producto.setDiametro(request.getDiametro());
        producto.setTipo(request.getTipo());
        producto.setMedida2(request.getMedida2());
        producto.setPrecioCosto(request.getPrecioCosto());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStockMinimo(request.getStockMinimo());

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
            case "nombre", "descripcion":
                producto.setDescripcion((String) valor);
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

        // Verificar si tiene stock
        if (producto.getStock() > 0) {
            throw new BadRequestException(
                    "No se puede eliminar el producto porque tiene stock disponible."
            );
        }

        // Eliminación lógica (recomendada)
        producto.setEstado(false);
        productoRepository.save(producto);
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
            List<ImagenProducto> fotosActuales = imagenProductoRepository.findByProductoIdOrderByOrdenAsc(productoId);
            fotosActuales.forEach(foto -> foto.setEsPrincipal(false));
            imagenProductoRepository.saveAll(fotosActuales);
        }

        // Crear registro de imagen
        ImagenProducto imagen = new ImagenProducto();
        imagen.setProducto(producto);
        imagen.setUrl(rutaArchivo);
        imagen.setEsPrincipal(esPrincipal);
        imagen.setOrden(producto.getImagenes().size() + 1);

        imagenProductoRepository.save(imagen);
    }

    /**
     * Eliminar foto de producto - RF-010
     */
    @Transactional
    public void eliminarFoto(Long fotoId) {

        ImagenProducto foto = imagenProductoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        // Eliminar archivo físico
        fileStorageService.eliminarArchivo(foto.getUrl());

        // Eliminar registro
        imagenProductoRepository.delete(foto);
    }

    /**
     * Establecer foto principal - RF-010
     */
    @Transactional
    public void establecerFotoPrincipal(Long productoId, Long fotoId) {

        // Desmarcar todas las fotos del producto
        List<ImagenProducto> fotos = imagenProductoRepository.findByProductoIdOrderByOrdenAsc(productoId);
        fotos.forEach(foto -> foto.setEsPrincipal(false));
        imagenProductoRepository.saveAll(fotos);

        // Marcar la foto seleccionada como principal
        ImagenProducto fotoPrincipal = imagenProductoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto no encontrada"));

        fotoPrincipal.setEsPrincipal(true);
        imagenProductoRepository.save(fotoPrincipal);
    }

    /**
     * Listar productos para catálogo público - RF-058 a RF-062
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoCatalogoPublicoResponse> listarCatalogoPublico(
            Long categoriaId,
            Long marcaId
    ) {
        List<Producto> productos = productoRepository.findByEstadoTrue();

        // Aplicar filtros
        if (categoriaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria().getId().equals(categoriaId))
                    .collect(Collectors.toList());
        }

        if (marcaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getMarcaProducto().getId().equals(marcaId))
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
        dto.setStockTotal(producto.getStock());
        return dto;
    }

    private void copiarDatosBasicos(Producto producto, ProductoDTO.ProductoResponse dto) {
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigoInterno());
        dto.setCodigoMarca(producto.getCodigoMarca());
        dto.setCodigoReferencia(producto.getCodigoReferencia());

        // ✅ AGREGAR: Código OEM desde la relación
        if (!producto.getCodigosOem().isEmpty()) {
            dto.setCodigoOem(producto.getCodigosOem().get(0).getCodigoOem().getCodigoOem());
        } else {
            dto.setCodigoOem(null);
        }

        dto.setDescripcion(producto.getDescripcion());

        // Categorización
        dto.setCategoriaId(producto.getCategoria().getId());
        dto.setCategoriaNombre(producto.getCategoria().getNombre());

        if (producto.getSubcategoria() != null) {
            dto.setSubcategoriaId(producto.getSubcategoria().getId());
            dto.setSubcategoriaNombre(producto.getSubcategoria().getNombre());
        }

        dto.setMarcaId(producto.getMarcaProducto().getId());
        dto.setMarcaNombre(producto.getMarcaProducto().getNombre());

        // ✅ AGREGAR: Datos del vehículo desde compatibilidades
        if (!producto.getCompatibilidades().isEmpty()) {
            Compatibilidad compat = producto.getCompatibilidades().get(0);
            dto.setMarcaAutomovil(compat.getMarcaAutomovil().getNombre());
            if (compat.getModeloAutomovil() != null) {
                dto.setModeloAutomovil(compat.getModeloAutomovil().getNombre());
            }
            dto.setAnio(compat.getAnio());
            dto.setMotor(compat.getMotor());
        } else {
            dto.setMarcaAutomovil(null);
            dto.setModeloAutomovil(null);
            dto.setAnio(null);
            dto.setMotor(null);
        }

        // Especificaciones técnicas
        dto.setOrigen(producto.getOrigen() != null ? producto.getOrigen().getPais() : null);
        dto.setMedida(producto.getMedida());
        dto.setDiametro(producto.getDiametro());
        dto.setTipo(producto.getTipo());
        dto.setMedida2(producto.getMedida2());

        // Precios
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setPrecioCosto(producto.getPrecioCosto());
        dto.setCodigoPrecio(producto.getCodigoPrecio() != null ? producto.getCodigoPrecio().getCodigo() : null);

        // Control
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setActivo(producto.getEstado());
        dto.setFechaCreacion(producto.getFechaCreacion());

        // Fotos
        List<ImagenProducto> fotos = imagenProductoRepository.findByProductoIdOrderByOrdenAsc(producto.getId());
        dto.setFotos(fotos.stream().map(ImagenProducto::getUrl).collect(Collectors.toList()));

        fotos.stream()
                .filter(ImagenProducto::getEsPrincipal)
                .findFirst()
                .ifPresent(foto -> dto.setFotoPrincipal(foto.getUrl()));
    }

    private ProductoDTO.ProductoCatalogoPublicoResponse convertirACatalogoPublicoDTO(Producto producto) {
        ProductoDTO.ProductoCatalogoPublicoResponse dto = new ProductoDTO.ProductoCatalogoPublicoResponse();

        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigoInterno());
        dto.setNombre(producto.getDescripcion());
        dto.setDescripcion(producto.getDescripcion());

        dto.setCategoriaNombre(producto.getCategoria().getNombre());
        dto.setSubcategoriaNombre(producto.getSubcategoria() != null ? producto.getSubcategoria().getNombre() : null);
        dto.setMarcaNombre(producto.getMarcaProducto().getNombre());

        dto.setOrigen(producto.getOrigen() != null ? producto.getOrigen().getPais() : null);
        dto.setMedida(producto.getMedida());
        dto.setDiametro(producto.getDiametro());
        dto.setTipo(producto.getTipo());

        dto.setPrecioVenta(producto.getPrecioVenta());

        // Calcular disponibilidad - RF-060
        int stock = producto.getStock();
        if (stock == 0) {
            dto.setDisponibilidad("AGOTADO");
        } else if (stock <= producto.getStockMinimo()) {
            dto.setDisponibilidad("ULTIMAS_UNIDADES");
        } else {
            dto.setDisponibilidad("DISPONIBLE");
        }   

        // Fotos
        List<ImagenProducto> fotos = imagenProductoRepository.findByProductoIdOrderByOrdenAsc(producto.getId());
        dto.setFotos(fotos.stream().map(ImagenProducto::getUrl).collect(Collectors.toList()));

        fotos.stream()
                .filter(ImagenProducto::getEsPrincipal)
                .findFirst()
                .ifPresent(foto -> dto.setFotoPrincipal(foto.getUrl()));

        return dto;
    }
}