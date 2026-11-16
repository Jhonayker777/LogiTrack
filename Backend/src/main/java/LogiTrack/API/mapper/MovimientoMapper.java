package LogiTrack.API.mapper;

import LogiTrack.API.dto.request.MovimientoDtoRequest;
import LogiTrack.API.dto.response.BodegaDtoResponse;
import LogiTrack.API.dto.response.MovimientoDtoResponse;
import LogiTrack.API.dto.response.UsuarioResumenDtoResponse;
import LogiTrack.API.Model.Bodega;
import LogiTrack.API.Model.Movimiento;
import LogiTrack.API.Model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public MovimientoDtoResponse entityToDto(Movimiento movimiento, UsuarioResumenDtoResponse usuarioDto, BodegaDtoResponse origenDto, BodegaDtoResponse destinoDto) {

        if(movimiento==null || usuarioDto==null) return null;
        return new MovimientoDtoResponse(movimiento.getId(), movimiento.getTipo().name(), movimiento.getFecha(), usuarioDto, origenDto, destinoDto, movimiento.getObservacion());

    }

    public Movimiento DtoToEntity(MovimientoDtoRequest dto, Usuario usuario, Bodega origen, Bodega destino) {

        if(dto==null || usuario==null) return null;
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(dto.tipo());
        movimiento.setUsuario(usuario);
        movimiento.setBodegaOrigen(origen);
        movimiento.setBodegaDestino(destino);
        movimiento.setObservacion(dto.observacion());
        return movimiento;

    }

    public void updateMovimientoFromDto(Movimiento movimiento, MovimientoDtoRequest dto, Usuario usuario, Bodega origen, Bodega destino) {

        if(movimiento==null || dto==null || usuario==null) return;
        movimiento.setTipo(dto.tipo());
        movimiento.setUsuario(usuario);
        movimiento.setBodegaOrigen(origen);
        movimiento.setBodegaDestino(destino);
        movimiento.setObservacion(dto.observacion());

    }

}