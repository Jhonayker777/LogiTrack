package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.DepartamentoDtoRequest;
import LogiTrack.API.dto.response.DepartamentoDtoResponse;
import LogiTrack.API.exception.BussinesRuleException;
import LogiTrack.API.exception.DatabaseRuleException;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.Model.Departamento;
import LogiTrack.API.repository.CiudadRepository;
import LogiTrack.API.repository.DepartamentoRepository;
import LogiTrack.API.service.DepartamentoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoServiceImpl implements DepartamentoService {

    private final DepartamentoMapper departamentoMapper;
    private final DepartamentoRepository departamentoRepository;
    private final CiudadRepository ciudadRepository;

    @Override
    public DepartamentoDtoResponse guardar(DepartamentoDtoRequest dto) {
        if (departamentoRepository.existsByNombre(dto.nombre()))
            throw new BussinesRuleException("Ya existe un departamento con el nombre: " + dto.nombre());
        Departamento d = departamentoMapper.DtoToEntity(dto);
        return departamentoMapper.entityToDto(departamentoRepository.save(d));
    }

    @Override
    public DepartamentoDtoResponse actualizar(DepartamentoDtoRequest dto, Long id) {
        Departamento d = departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departamento no encontrado con ID: " + id));
        departamentoRepository.findByNombre(dto.nombre()).ifPresent(otro -> {
            if (!otro.getId().equals(id))
                throw new BussinesRuleException("Ya existe un departamento con el nombre: " + dto.nombre());
        });
        departamentoMapper.updateEntityFromDto(d, dto);
        return departamentoMapper.entityToDto(departamentoRepository.save(d));
    }

    @Override
    public void eliminar(Long id) {
        Departamento d = departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departamento no encontrado con ID: " + id));
        if (ciudadRepository.existsByDepartamentoId(d.getId()))
            throw new DatabaseRuleException("No se puede eliminar el departamento porque tiene ciudades asociadas.");
        departamentoRepository.delete(d);
    }

    @Override
    public List<DepartamentoDtoResponse> listar() {
        return departamentoRepository.findAll().stream().map(departamentoMapper::entityToDto).toList();
    }

    @Override
    public DepartamentoDtoResponse buscarId(Long id) {
        return departamentoMapper.entityToDto(departamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe departamento con ID: " + id)));
    }
}