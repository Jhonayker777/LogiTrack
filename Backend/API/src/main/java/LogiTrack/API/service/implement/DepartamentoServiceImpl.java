package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.DepartamentoDtoRequest;
import LogiTrack.API.dto.response.DepartamentoDtoResponse;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.Model.Departamento;
import LogiTrack.API.repository.CiudadRepository;
import LogiTrack.API.repository.DepartamentoRepository;
import LogiTrack.API.service.DepartamentoService;
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
            throw new RuntimeException("Ya existe un departamento con el nombre: " + dto.nombre());
        Departamento d = departamentoMapper.DtoToEntity(dto);
        Departamento departamentoRespuesta = departamentoRepository.save(d);
        return departamentoMapper.entityToDto(departamentoRespuesta);
    }

    @Override
    public DepartamentoDtoResponse actualizar(DepartamentoDtoRequest dto, Long id) {
        Departamento d = departamentoRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, departamento a actualizar no encontrado"));
        departamentoRepository.findByNombre(dto.nombre()).ifPresent(otro -> {
            if (!otro.getId().equals(id))
                throw new RuntimeException("Ya existe un departamento con el nombre: " + dto.nombre());
        });
        departamentoMapper.updateEntityFromDto(d, dto);
        Departamento departamentoActualizado = departamentoRepository.save(d);
        return departamentoMapper.entityToDto(departamentoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        Departamento d = departamentoRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, departamento a eliminar no existe"));
        if (ciudadRepository.existsByDepartamentoId(d.getId()))
            throw new RuntimeException("Este departamento tiene ciudades asociadas");
        departamentoRepository.delete(d);
    }

    @Override
    public List<DepartamentoDtoResponse> listar() {
        return departamentoRepository.findAll().stream().map(departamentoMapper::entityToDto).toList();
    }

    @Override
    public DepartamentoDtoResponse buscarId(Long id) {
        return departamentoMapper.entityToDto(departamentoRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, no existe departamento con ese id")));
    }
}
