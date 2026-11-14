package LogiTrack.API.service.implement;

import LogiTrack.API.dto.request.CiudadDtoRequest;
import LogiTrack.API.dto.response.CiudadDtoResponse;
import LogiTrack.API.dto.response.DepartamentoDtoResponse;
import LogiTrack.API.mapper.CiudadMapper;
import LogiTrack.API.mapper.DepartamentoMapper;
import LogiTrack.API.Model.Ciudad;
import LogiTrack.API.Model.Departamento;
import LogiTrack.API.repository.BodegaRepository;
import LogiTrack.API.repository.CiudadRepository;
import LogiTrack.API.repository.DepartamentoRepository;
import LogiTrack.API.repository.UsuarioRepository;
import LogiTrack.API.service.CiudadService;
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
        Departamento d = departamentoRepository.findById(dto.departamentoId()).orElseThrow(() -> new RuntimeException("Codigo de departamento no existe"));
        if (ciudadRepository.existsByNombreAndDepartamentoId(dto.nombre(), dto.departamentoId()))
            throw new RuntimeException("Ya existe la ciudad " + dto.nombre() + " en ese departamento");
        Ciudad c = ciudadMapper.DtoToEntity(dto, d);
        DepartamentoDtoResponse departamentoDto = departamentoMapper.entityToDto(d);
        return ciudadMapper.entityToDto(ciudadRepository.save(c), departamentoDto);
    }

    @Override
    public CiudadDtoResponse actualizar(CiudadDtoRequest dto, Long id) {
        Ciudad c = ciudadRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, ciudad a actualizar no encontrada"));
        Departamento d = departamentoRepository.findById(dto.departamentoId()).orElseThrow(() -> new RuntimeException("Codigo de departamento no existe"));
        ciudadRepository.findByNombreAndDepartamentoId(dto.nombre(), dto.departamentoId()).ifPresent(otra -> {
            if (!otra.getId().equals(id))
                throw new RuntimeException("Ya existe la ciudad " + dto.nombre() + " en ese departamento");
        });
        ciudadMapper.updateEntityFromDto(c, dto, d);
        return ciudadMapper.entityToDto(ciudadRepository.save(c), departamentoMapper.entityToDto(d));
    }

    @Override
    public void eliminar(Long id) {
        Ciudad c = ciudadRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, ciudad a eliminar no existe"));
        if (bodegaRepository.existsByCiudadId(c.getId()))
            throw new RuntimeException("Esta ciudad pertenece a una bodega");
        if (usuarioRepository.existsByCiudadId(c.getId()))
            throw new RuntimeException("Esta ciudad pertenece a un usuario");
        ciudadRepository.delete(c);
    }

    @Override
    public List<CiudadDtoResponse> listar() {
        return ciudadRepository.findAll().stream().map(
                c -> ciudadMapper.entityToDto(c, departamentoMapper.entityToDto(c.getDepartamento()))).toList();
    }

    @Override
    public CiudadDtoResponse buscarId(Long id) {
        Ciudad c = ciudadRepository.findById(id).orElseThrow(() -> new RuntimeException("Error, no existe ciudad con ese id"));
        return ciudadMapper.entityToDto(c, departamentoMapper.entityToDto(c.getDepartamento()));
    }

    @Override
    public List<CiudadDtoResponse> buscarDepartamentoId(Long id) {
        return ciudadRepository.findByDepartamentoId(id).stream().map(
                c -> ciudadMapper.entityToDto(c, departamentoMapper.entityToDto(c.getDepartamento()))).toList();
    }
}
