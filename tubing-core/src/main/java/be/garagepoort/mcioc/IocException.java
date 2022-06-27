package be.garagepoort.mcioc;

public class IocException extends RuntimeException {

    public IocException(String message) {
        super("Mc-Ioc bean be.garagepoort.mcioc.tubingvelocity.exceptions: [" + message + "]");
    }

    public IocException(String s, Throwable e) {
        super("Mc-Ioc bean be.garagepoort.mcioc.tubingvelocity.exceptions: [" + s + "]", e);
    }
}
