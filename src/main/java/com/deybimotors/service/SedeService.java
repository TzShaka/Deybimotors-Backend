package com.deybimotors.service;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.entity.Sede;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.SedeRepository;
import com.deybimotors.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Sedes - RF-040 a RF-042
 */
@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<GenericDTO.SedeResponse> listarTodas() {
        return sedeRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.SedeResponse> listarActivas() {
        return sedeRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenericDTO.SedeResponse obtenerPorId(Long id) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
        return convertirADTO(sede);
    }

    @Transactional
    public GenericDTO.SedeResponse crear(GenericDTO.SedeRequest request) {
        if (sedeRepository.existsByNombre(request.getNombre())) {
            throw new ConflictException("Ya existe una sede con ese nombre");
        }

        Sede sede = new Sede();
        sede.setNombre(request.getNombre());
        sede.setDireccion(request.getDireccion());
        sede.setCiudad(request.getCiudad());
        sede.setTelefono(request.getTelefono());
        sede.setActivo(true);
        sede.setObservaciones(request.getObservaciones());

        return convertirADTO(sedeRepository.save(sede));
    }

    @Transactional
    public GenericDTO.SedeResponse actualizar(Long id, GenericDTO.SedeRequest request) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        if (!sede.getNombre().equals(request.getNombre()) && sedeRepository.existsByNombre(request.getNombre())) {
            throw new ConflictException("Ya existe una sede con ese nombre");
        }

        sede.setNombre(request.getNombre());
        sede.setDireccion(request.getDireccion());
        sede.setCiudad(request.getCiudad());
        sede.setTelefono(request.getTelefono());
        sede.setObservaciones(request.getObservaciones());

        return convertirADTO(sedeRepository.save(sede));
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
        sede.setActivo(activo);
        sedeRepository.save(sede);
    }

    @Transactional
    public void eliminar(Long id) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        // Validar integridad referencial
        long usuariosAsociados = usuarioRepository.findBySedeId(id).size();
        if (usuariosAsociados > 0) {
            throw new ConflictException("No se puede eliminar la sede porque tiene usuarios asociados");
        }

        sedeRepository.delete(sede);
    }

    private GenericDTO.SedeResponse convertirADTO(Sede sede) {
        return new GenericDTO.SedeResponse(
                sede.getId(),
                sede.getNombre(),
                sede.getDireccion(),
                sede.getCiudad(),
                sede.getTelefono(),
                sede.getActivo(),
                sede.getObservaciones()
        );
    }
}