package com.deybimotors.service;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.entity.Categoria;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.CategoriaRepository;
import com.deybimotors.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Categorías - RF-046 a RF-049
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<GenericDTO.CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.CategoriaResponse> listarActivas() {
        return categoriaRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenericDTO.CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        return convertirADTO(categoria);
    }

    @Transactional
    public GenericDTO.CategoriaResponse crear(GenericDTO.CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setActivo(true);

        return convertirADTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public GenericDTO.CategoriaResponse actualizar(Long id, GenericDTO.CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (!categoria.getNombre().equals(request.getNombre()) && categoriaRepository.existsByNombre(request.getNombre())) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        return convertirADTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Validar integridad - RF-049
        long productosAsociados = productoRepository.countByCategoriaId(id);
        if (productosAsociados > 0) {
            throw new ConflictException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        categoriaRepository.delete(categoria);
    }

    private GenericDTO.CategoriaResponse convertirADTO(Categoria categoria) {
        return new GenericDTO.CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActivo()
        );
    }
}