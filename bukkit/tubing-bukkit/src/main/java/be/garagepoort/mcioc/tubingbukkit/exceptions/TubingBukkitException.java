package be.garagepoort.mcioc.tubingbukkit.exceptions;

public class TubingBukkitException extends RuntimeException{
    public TubingBukkitException(String message) {
        super(message);
    }

    public TubingBukkitException(String message, Exception e) {
        super(message, e);
    }

    public TubingBukkitException(Exception e) {
        super(e);
    }
}
