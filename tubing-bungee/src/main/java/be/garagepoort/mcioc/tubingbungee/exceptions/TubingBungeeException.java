package be.garagepoort.mcioc.tubingbungee.exceptions;

public class TubingBungeeException extends RuntimeException{
    public TubingBungeeException(String message) {
        super(message);
    }

    public TubingBungeeException(String message, Exception e) {
        super(message, e);
    }

    public TubingBungeeException(Exception e) {
        super(e);
    }
}
