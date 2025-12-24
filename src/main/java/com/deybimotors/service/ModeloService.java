package com.deybimotors.service;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.entity.Marca;
import com.deybimotors.entity.Modelo;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.MarcaRepository;
import com.deybimotors.repository.ModeloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModeloService {

    private final ModeloRepository modeloRepository;
    private final MarcaRepository marcaRepository;

    @Transactional(readOnly = true)
    public List<GenericDTO.ModeloResponse> listarTodos() {
        return modeloRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.ModeloResponse> listarActivos() {
        return modeloRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.ModeloResponse> listarPorMarca(Long marcaId) {
        return modeloRepository.findByMarcaId(marcaId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenericDTO.ModeloResponse obtenerPorId(Long id) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));
        return convertirADTO(modelo);
    }

    @Transactional
    public GenericDTO.ModeloResponse crear(GenericDTO.ModeloRequest request) {

        Marca marca = marcaRepository.findById(request.getMarcaId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        if (modeloRepository.existsByNombreAndMarcaId(request.getNombre(), request.getMarcaId())) {
            throw new ConflictException("Ya existe un modelo con ese nombre para esta marca");
        }

        Modelo modelo = new Modelo();
        modelo.setNombre(request.getNombre());
        modelo.setMarca(marca);
        modelo.setDescripcion(request.getDescripcion());
        modelo.setActivo(true);

        return convertirADTO(modeloRepository.save(modelo));
    }

    @Transactional
    public GenericDTO.ModeloResponse actualizar(Long id, GenericDTO.ModeloRequest request) {

        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));

        Marca marca = marcaRepository.findById(request.getMarcaId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));

        if (!modelo.getNombre().equals(request.getNombre()) &&
                modeloRepository.existsByNombreAndMarcaId(request.getNombre(), request.getMarcaId())) {
            throw new ConflictException("Ya existe un modelo con ese nombre para esta marca");
        }

        modelo.setNombre(request.getNombre());
        modelo.setMarca(marca);
        modelo.setDescripcion(request.getDescripcion());

        return convertirADTO(modeloRepository.save(modelo));
    }

    @Transactional
    public void eliminar(Long id) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo no encontrado"));

        // ✅ NO SE VALIDA - La tabla modelos es un catálogo maestro independiente
        // Los productos usan el campo de texto 'modeloAutomovil', no una relación

        modeloRepository.delete(modelo);
    }

    private GenericDTO.ModeloResponse convertirADTO(Modelo modelo) {
        return new GenericDTO.ModeloResponse(
                modelo.getId(),
                modelo.getNombre(),
                modelo.getMarca().getId(),
                modelo.getMarca().getNombre(),
                modelo.getDescripcion(),
                modelo.getActivo()
        );
    }
}