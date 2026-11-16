package LogiTrack.API.exception;

/** Regla de negocio violada (stock insuficiente, duplicados, referencias inválidas...). Responde 400. */
public class BussinesRuleException extends RuntimeException {
    public BussinesRuleException(String mensaje) {
        super(mensaje);
    }
}
