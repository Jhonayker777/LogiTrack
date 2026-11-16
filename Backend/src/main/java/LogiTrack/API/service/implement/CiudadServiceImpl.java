package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.CiudadDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.exception.BussinesRuleException;
import LogiTrack.API.exception.DatabaseRuleException;
import LogiTrack.API.mapper.CiudadMapper;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.Departamento;
import LogiTrack.API.repository.BodegaRepository;
import LogiTrack.API.repository.CiudadRepository;
import LogiTrack.API.repository.DepartamentoRepository;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.service.CiudadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CiudadServiceImpl implements CiudadService {

    private final CiudadMapper ciudadMapper;
    private final DepartamentoMapper departamentoMapper;
    private final CiudadRepository ciudadRepository;
    private final DepartamentoRepository departamentoRepository;
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public CiudadDtoResponse guardar(CiudadDtoRequest dto) {
        Departamento d = departamentoRepository.findById(dto.departamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Departamento no encontrado con ID: " + dto.departamentoId()));

        if (ciudadRepository.existsByNombreAndDepartamentoId(dto.nombre(), dto.departamentoId()))
            throw new BussinesRuleException("Ya existe la ciudad " + dto.nombre() + " en ese departamento.");

        Ciudad c = ciudadMapper.DtoToEntity(dto, d);
        return ciudadMapper.entityToDto(ciudadRepository.save(c), departamentoMapper.entityToDto(d));
    }

    @Override
    public CiudadDtoResponse actualizar(CiudadDtoRequest dto, Long id) {
        Ciudad c = ciudadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ciudad no encontrada con ID: " + id));
        Departamento d = departamentoRepository.findById(dto.departamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Departamento no encontrado con ID: " + dto.departamentoId()));

        ciudadRepository.findByNombreAndDepartamentoId(dto.nombre(), dto.departamentoId()).ifPresent(otra -> {
            if (!otra.getId().equals(id))
                throw new BussinesRuleException("Ya existe la ciudad " + dto.nombre() + " en ese departamento.");
        });

        ciudadMapper.updateEntityFromDto(c, dto, d);
        return ciudadMapper.entityToDto(ciudadRepository.save(c), departamentoMapper.entityToDto(d));
    }

    @Override
    public void eliminar(Long id) {
        Ciudad c = ciudadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ciudad no encontrada con ID: " + id));
        if (bodegaRepository.existsByCiudadId(c.getId()))
            throw new DatabaseRuleException("No se puede eliminar la ciudad porque pertenece a una bodega.");
        if (usuarioRepository.existsByCiudadId(c.getId()))
            throw new DatabaseRuleException("No se puede eliminar la ciudad porque pertenece a un usuario.");
        ciudadRepository.delete(c);
    }

    @Override
    public List<CiudadDtoResponse> listar() {
        return ciudadRepository.findAll().stream().map(
                c -> ciudadMapper.entityToDto(c, departamentoMapper.entityToDto(c.getDepartamento()))).toList();
    }

    @Override
    public CiudadDtoResponse buscarId(Long id) {
        Ciudad c = ciudadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe ciudad con ID: " + id));
        return ciudadMapper.entityToDto(c, departamentoMapper.entityToDto(c.getDepartamento()));
    }

    @Override
    public List<CiudadDtoResponse> buscarDepartamentoId(Long id) {
        return ciudadRepository.findByDepartamentoId(id).stream().map(
                c -> ciudadMapper.entityToDto(c, departamentoMapper.entityToDto(c.getDepartamento()))).toList();
    }
}