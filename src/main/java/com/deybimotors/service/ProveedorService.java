package com.deybimotors.service;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.entity.Proveedor;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.CompraRepository;
import com.deybimotors.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Proveedores - RF-043 a RF-045
 */
@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;

    @Transactional(readOnly = true)
    public List<GenericDTO.ProveedorResponse> listarTodos() {
        return proveedorRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.ProveedorResponse> listarActivos() {
        return proveedorRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenericDTO.ProveedorResponse obtenerPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        return convertirADTO(proveedor);
    }

    @Transactional(readOnly = true)
    public List<GenericDTO.ProveedorResponse> buscarPorNombre(String nombre) {
        return proveedorRepository.findByNombreEmpresaContainingIgnoreCase(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GenericDTO.ProveedorResponse crear(GenericDTO.ProveedorRequest request) {

        if (request.getRuc() != null && proveedorRepository.existsByRuc(request.getRuc())) {
            throw new ConflictException("Ya existe un proveedor con ese RUC");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombreEmpresa(request.getNombreEmpresa());
        proveedor.setRuc(request.getRuc());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setActivo(true);
        proveedor.setObservaciones(request.getObservaciones());

        return convertirADTO(proveedorRepository.save(proveedor));
    }

    @Transactional
    public GenericDTO.ProveedorResponse actualizar(Long id, GenericDTO.ProveedorRequest request) {

        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        if (request.getRuc() != null &&
                !request.getRuc().equals(proveedor.getRuc()) &&
                proveedorRepository.existsByRuc(request.getRuc())) {
            throw new ConflictException("Ya existe un proveedor con ese RUC");
        }

        proveedor.setNombreEmpresa(request.getNombreEmpresa());
        proveedor.setRuc(request.getRuc());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setObservaciones(request.getObservaciones());

        return convertirADTO(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
        proveedor.setActivo(activo);
        proveedorRepository.save(proveedor);
    }

    @Transactional
    public void eliminar(Long id) {

        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        long comprasAsociadas = compraRepository.findByProveedorId(id).size();
        if (comprasAsociadas > 0) {
            throw new ConflictException("No se puede eliminar el proveedor porque tiene compras asociadas");
        }

        proveedorRepository.delete(proveedor);
    }

    private GenericDTO.ProveedorResponse convertirADTO(Proveedor proveedor) {
        return new GenericDTO.ProveedorResponse(
                proveedor.getId(),
                proveedor.getNombreEmpresa(),
                proveedor.getRuc(),
                proveedor.getContacto(),
                proveedor.getTelefono(),
                proveedor.getEmail(),
                proveedor.getDireccion(),
                proveedor.getActivo(),
                proveedor.getObservaciones()
        );
    }
}