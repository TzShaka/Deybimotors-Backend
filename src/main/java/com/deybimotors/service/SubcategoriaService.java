package com.deybimotors.service;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.entity.Categoria;
import com.deybimotors.entity.Subcategoria;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.CategoriaRepository;
import com.deybimotors.repository.ProductoRepository;
import com.deybimotors.repository.SubcategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Subcategorías - RF-046 a RF-049
 * ✅ ACTUALIZADO: Agregado método listarActivasPorCategoria
 */
@Service
@RequiredArgsConstructor
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<GenericDTO.SubcategoriaResponse> listarTodas() {
        return subcategoriaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.SubcategoriaResponse> listarActivas() {
        return subcategoriaRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.SubcategoriaResponse> listarPorCategoria(Long categoriaId) {
        return subcategoriaRepository.findByCategoriaId(categoriaId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ NUEVO MÉTODO
     * Listar subcategorías ACTIVAS por categoría
     */
    @Transactional(readOnly = true)
    public List<GenericDTO.SubcategoriaResponse> listarActivasPorCategoria(Long categoriaId) {
        return subcategoriaRepository.findByCategoriaIdAndActivoTrue(categoriaId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenericDTO.SubcategoriaResponse obtenerPorId(Long id) {
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada"));
        return convertirADTO(subcategoria);
    }

    @Transactional
    public GenericDTO.SubcategoriaResponse crear(GenericDTO.SubcategoriaRequest request) {

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (subcategoriaRepository.existsByNombreAndCategoriaId(request.getNombre(), request.getCategoriaId())) {
            throw new ConflictException("Ya existe una subcategoría con ese nombre en esta categoría");
        }

        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setNombre(request.getNombre());
        subcategoria.setCategoria(categoria);
        subcategoria.setDescripcion(request.getDescripcion());
        subcategoria.setActivo(true);

        return convertirADTO(subcategoriaRepository.save(subcategoria));
    }

    @Transactional
    public GenericDTO.SubcategoriaResponse actualizar(Long id, GenericDTO.SubcategoriaRequest request) {

        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada"));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (!subcategoria.getNombre().equals(request.getNombre()) &&
                subcategoriaRepository.existsByNombreAndCategoriaId(request.getNombre(), request.getCategoriaId())) {
            throw new ConflictException("Ya existe una subcategoría con ese nombre en esta categoría");
        }

        subcategoria.setNombre(request.getNombre());
        subcategoria.setCategoria(categoria);
        subcategoria.setDescripcion(request.getDescripcion());

        return convertirADTO(subcategoriaRepository.save(subcategoria));
    }

    @Transactional
    public void eliminar(Long id) {

        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoría no encontrada"));

        // Validar integridad referencial
        long productosAsociados = productoRepository.countBySubcategoriaId(id);

        if (productosAsociados > 0) {
            throw new ConflictException("No se puede eliminar la subcategoría porque tiene productos asociados");
        }

        subcategoriaRepository.delete(subcategoria);
    }

    private GenericDTO.SubcategoriaResponse convertirADTO(Subcategoria subcategoria) {
        return new GenericDTO.SubcategoriaResponse(
                subcategoria.getId(),
                subcategoria.getNombre(),
                subcategoria.getCategoria().getId(),
                subcategoria.getCategoria().getNombre(),
                subcategoria.getDescripcion(),
                subcategoria.getActivo()
        );
    }
}