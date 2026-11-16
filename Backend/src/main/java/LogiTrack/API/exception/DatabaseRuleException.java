package LogiTrack.API.exception;

public class DatabaseRuleException extends RuntimeException {
    //(Usada cuando la base de datos rechaza una operación, ej: intentar borrar un producto que tiene movimientos)
    public DatabaseRuleException(String mensaje) {
        super(mensaje);
    }
}