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
 * Servicio de Productos - ✅ CORREGIDO FINAL
 * Con manejo completo de valores NULL
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
    private final MovimientoKardexRepository kardexRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoResponse> listarTodos() {
        List<Producto> productos = productoRepository.findByEstadoTrue();

        // ✅ DEBUG
        System.out.println("========================================");
        System.out.println("Productos encontrados: " + productos.size());
        for (Producto p : productos) {
            System.out.println("- ID: " + p.getId() + " | Código: " + p.getCodigoInterno() + " | Desc: " + p.getDescripcion());
        }
        System.out.println("========================================");

        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO.ProductoResponse> listarConPaginacion(Pageable pageable) {
        return productoRepository.findAll(pageable)
                .map(this::convertirADTO);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoConStockResponse> listarConStockPorSede(Long sedeId) {
        List<Producto> productos = productoRepository.findBySedeId(sedeId);

        return productos.stream()
                .map(producto -> {
                    ProductoDTO.ProductoConStockResponse dto = new ProductoDTO.ProductoConStockResponse();
                    copiarDatosBasicos(producto, dto);
                    dto.setStockSede(producto.getStock());
                    return dto;
                })
                .collect(Collectors.toList());
    }

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

    @Transactional(readOnly = true)
    public ProductoDTO.ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return convertirADTO(producto);
    }

    @Transactional(readOnly = true)
    public ProductoDTO.ProductoResponse obtenerPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigoInterno(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + codigo));
        return convertirADTO(producto);
    }

    @Transactional
    public ProductoDTO.ProductoResponse crear(ProductoDTO.ProductoRequest request, Long usuarioId) {

        if (productoRepository.existsByCodigoInterno(request.getCodigo())) {
            throw new ConflictException("Ya existe un producto con el código: " + request.getCodigo());
        }

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

        Origen origen = null;
        if (request.getOrigen() != null && !request.getOrigen().isEmpty()) {
            origen = origenRepository.findByPais(request.getOrigen())
                    .orElseGet(() -> {
                        Origen nuevoOrigen = new Origen();
                        nuevoOrigen.setPais(request.getOrigen());
                        return origenRepository.save(nuevoOrigen);
                    });
        }

        CodigoPrecio codigoPrecio = null;
        if (request.getCodigoPrecio() != null && !request.getCodigoPrecio().isEmpty()) {
            codigoPrecio = codigoPrecioRepository.findByCodigo(request.getCodigoPrecio())
                    .orElseGet(() -> {
                        CodigoPrecio nuevoCodigo = new CodigoPrecio();
                        nuevoCodigo.setCodigo(request.getCodigoPrecio());
                        return codigoPrecioRepository.save(nuevoCodigo);
                    });
        }

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
        producto.setStock(0);
        producto.setCodigoPrecio(codigoPrecio);
        producto.setPrecioCosto(request.getPrecioCosto());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setPublicoCatalogo(request.getPublicoCatalogo() != null ? request.getPublicoCatalogo() : false);
        producto.setEstado(true);

        Producto guardado = productoRepository.save(producto);
        return convertirADTO(guardado);
    }

    @Transactional
    public ProductoDTO.ProductoResponse actualizar(Long id, ProductoDTO.ProductoRequest request) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (!producto.getCodigoInterno().equals(request.getCodigo())) {
            if (productoRepository.existsByCodigoInterno(request.getCodigo())) {
                throw new ConflictException("Ya existe un producto con el código: " + request.getCodigo());
            }
            producto.setCodigoInterno(request.getCodigo());
        }

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

        producto.setCodigoMarca(request.getCodigoMarca());
        producto.setCodigoReferencia(request.getCodigoReferencia());
        producto.setDescripcion(request.getNombre());
        producto.setMedida(request.getMedida());
        producto.setDiametro(request.getDiametro());
        producto.setTipo(request.getTipo());
        producto.setMedida2(request.getMedida2());
        producto.setPrecioCosto(request.getPrecioCosto());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setPublicoCatalogo(request.getPublicoCatalogo() != null ? request.getPublicoCatalogo() : false);

        Producto actualizado = productoRepository.save(producto);
        return convertirADTO(actualizado);
    }

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

    @Transactional
    public void eliminar(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        List<MovimientoKardex> movimientos = kardexRepository.findByProductoIdOrderByFechaMovimientoDesc(id);
        if (!movimientos.isEmpty()) {
            throw new BadRequestException(
                    "No se puede eliminar el producto porque tiene movimientos registrados en el historial (Kardex)."
            );
        }

        if (producto.getStock() > 0) {
            throw new BadRequestException(
                    "No se puede eliminar el producto porque tiene stock disponible."
            );
        }

        producto.setEstado(false);
        productoRepository.save(producto);
    }

    @Transactional
    public void subirFoto(Long productoId, MultipartFile archivo) throws IOException {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (producto.getFotoUrl() != null) {
            fileStorageService.eliminarArchivo(producto.getFotoUrl());
        }

        String rutaArchivo = fileStorageService.guardarImagen(archivo, "productos");
        producto.setFotoUrl(rutaArchivo);
        productoRepository.save(producto);
    }

    @Transactional
    public void eliminarFoto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (producto.getFotoUrl() != null) {
            fileStorageService.eliminarArchivo(producto.getFotoUrl());
            producto.setFotoUrl(null);
            productoRepository.save(producto);
        }
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO.ProductoCatalogoPublicoResponse> listarCatalogoPublico(
            Long categoriaId,
            Long marcaId
    ) {
        List<Producto> productos = productoRepository.findByEstadoTrue().stream()
                .filter(p -> p.getPublicoCatalogo() != null && p.getPublicoCatalogo())
                .collect(Collectors.toList());

        if (categoriaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().getId().equals(categoriaId))
                    .collect(Collectors.toList());
        }

        if (marcaId != null) {
            productos = productos.stream()
                    .filter(p -> p.getMarcaProducto() != null && p.getMarcaProducto().getId().equals(marcaId))
                    .collect(Collectors.toList());
        }

        return productos.stream()
                .map(this::convertirACatalogoPublicoDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // MÉTODOS AUXILIARES - ✅ CORREGIDOS
    // ========================================

    private ProductoDTO.ProductoResponse convertirADTO(Producto producto) {
        ProductoDTO.ProductoResponse dto = new ProductoDTO.ProductoResponse();

        try {
            copiarDatosBasicos(producto, dto);
            dto.setStockTotal(producto.getStock());
        } catch (Exception e) {
            System.err.println("❌ Error convirtiendo producto ID " + producto.getId() + ": " + e.getMessage());
            e.printStackTrace();

            // DTO con datos mínimos
            dto.setId(producto.getId());
            dto.setCodigo(producto.getCodigoInterno());
            dto.setDescripcion(producto.getDescripcion());
            dto.setStockTotal(producto.getStock());
            dto.setActivo(producto.getEstado());
        }

        return dto;
    }

    private void copiarDatosBasicos(Producto producto, ProductoDTO.ProductoResponse dto) {
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigoInterno());
        dto.setCodigoMarca(producto.getCodigoMarca());
        dto.setCodigoReferencia(producto.getCodigoReferencia());

        // ✅ Manejo seguro de codigosOem
        if (producto.getCodigosOem() != null && !producto.getCodigosOem().isEmpty()) {
            try {
                dto.setCodigoOem(producto.getCodigosOem().get(0).getCodigoOem().getCodigoOem());
            } catch (Exception e) {
                dto.setCodigoOem(null);
            }
        } else {
            dto.setCodigoOem(null);
        }

        dto.setDescripcion(producto.getDescripcion());

        // ✅ Manejo seguro de categoría
        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }

        // ✅ Manejo seguro de subcategoría
        if (producto.getSubcategoria() != null) {
            dto.setSubcategoriaId(producto.getSubcategoria().getId());
            dto.setSubcategoriaNombre(producto.getSubcategoria().getNombre());
        }

        // ✅ Manejo seguro de marca
        if (producto.getMarcaProducto() != null) {
            dto.setMarcaId(producto.getMarcaProducto().getId());
            dto.setMarcaNombre(producto.getMarcaProducto().getNombre());
        }

        // ✅ Manejo seguro de compatibilidades
        if (producto.getCompatibilidades() != null && !producto.getCompatibilidades().isEmpty()) {
            try {
                Compatibilidad compat = producto.getCompatibilidades().get(0);
                if (compat.getMarcaAutomovil() != null) {
                    dto.setMarcaAutomovil(compat.getMarcaAutomovil().getNombre());
                }
                if (compat.getModeloAutomovil() != null) {
                    dto.setModeloAutomovil(compat.getModeloAutomovil().getNombre());
                }
                dto.setAnio(compat.getAnio());
                dto.setMotor(compat.getMotor());
            } catch (Exception e) {
                dto.setMarcaAutomovil(null);
                dto.setModeloAutomovil(null);
                dto.setAnio(null);
                dto.setMotor(null);
            }
        } else {
            dto.setMarcaAutomovil(null);
            dto.setModeloAutomovil(null);
            dto.setAnio(null);
            dto.setMotor(null);
        }

        dto.setOrigen(producto.getOrigen() != null ? producto.getOrigen().getPais() : null);
        dto.setMedida(producto.getMedida());
        dto.setDiametro(producto.getDiametro());
        dto.setTipo(producto.getTipo());
        dto.setMedida2(producto.getMedida2());

        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setPrecioCosto(producto.getPrecioCosto());
        dto.setCodigoPrecio(producto.getCodigoPrecio() != null ? producto.getCodigoPrecio().getCodigo() : null);

        dto.setActivo(producto.getEstado());
        dto.setPublicoCatalogo(producto.getPublicoCatalogo());
        dto.setFechaCreacion(producto.getFechaCreacion());
        dto.setFotoUrl(producto.getFotoUrl());
    }

    private ProductoDTO.ProductoCatalogoPublicoResponse convertirACatalogoPublicoDTO(Producto producto) {
        ProductoDTO.ProductoCatalogoPublicoResponse dto = new ProductoDTO.ProductoCatalogoPublicoResponse();

        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigoInterno());
        dto.setNombre(producto.getDescripcion());
        dto.setDescripcion(producto.getDescripcion());

        dto.setCategoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null);
        dto.setSubcategoriaNombre(producto.getSubcategoria() != null ? producto.getSubcategoria().getNombre() : null);
        dto.setMarcaNombre(producto.getMarcaProducto() != null ? producto.getMarcaProducto().getNombre() : null);

        if (producto.getCompatibilidades() != null && !producto.getCompatibilidades().isEmpty()) {
            try {
                Compatibilidad compat = producto.getCompatibilidades().get(0);
                dto.setMarcaAutomovil(compat.getMarcaAutomovil() != null ? compat.getMarcaAutomovil().getNombre() : null);
                dto.setModeloAutomovil(compat.getModeloAutomovil() != null ? compat.getModeloAutomovil().getNombre() : null);
                dto.setMotor(compat.getMotor());
            } catch (Exception e) {
                // Silenciosamente manejar error
            }
        }

        dto.setOrigen(producto.getOrigen() != null ? producto.getOrigen().getPais() : null);
        dto.setMedida(producto.getMedida());
        dto.setDiametro(producto.getDiametro());
        dto.setTipo(producto.getTipo());
        dto.setPrecioVenta(producto.getPrecioVenta());

        int stock = producto.getStock();
        if (stock == 0) {
            dto.setDisponibilidad("AGOTADO");
        } else if (stock <= 2) {
            dto.setDisponibilidad("ULTIMAS_UNIDADES");
        } else {
            dto.setDisponibilidad("DISPONIBLE");
        }

        dto.setFotoUrl(producto.getFotoUrl());

        return dto;
    }
}