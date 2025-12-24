package com.deybimotors.service;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.entity.Marca;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.MarcaRepository;
import com.deybimotors.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Marcas - RF-050 a RF-053
 */
@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<GenericDTO.MarcaResponse> listarTodas() {
        return marcaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.MarcaResponse> listarActivas() {
        return marcaRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenericDTO.MarcaResponse obtenerPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
        return convertirADTO(marca);
    }

    @Transactional
    public GenericDTO.MarcaResponse crear(GenericDTO.MarcaRequest request) {
        if (marcaRepository.existsByNombre(request.getNombre())) {
            throw new ConflictException("Ya existe una marca con ese nombre");
        }

        Marca marca = new Marca();
        marca.setNombre(request.getNombre());
        marca.setDescripcion(request.getDescripcion());
        marca.setActivo(true);

        return convertirADTO(marcaRepository.save(marca));
    }

    @Transactional
    public GenericDTO.MarcaResponse actualizar(Long id, GenericDTO.MarcaRequest request) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        if (!marca.getNombre().equals(request.getNombre()) && marcaRepository.existsByNombre(request.getNombre())) {
            throw new ConflictException("Ya existe una marca con ese nombre");
        }

        marca.setNombre(request.getNombre());
        marca.setDescripcion(request.getDescripcion());

        return convertirADTO(marcaRepository.save(marca));
    }

    @Transactional
    public void eliminar(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        // Validar integridad - RF-053
        long productosAsociados = productoRepository.countByMarcaId(id);
        if (productosAsociados > 0) {
            throw new ConflictException("No se puede eliminar la marca porque tiene productos asociados");
        }

        marcaRepository.delete(marca);
    }

    private GenericDTO.MarcaResponse convertirADTO(Marca marca) {
        return new GenericDTO.MarcaResponse(
                marca.getId(),
                marca.getNombre(),
                marca.getDescripcion(),
                marca.getActivo()
        );
    }
}