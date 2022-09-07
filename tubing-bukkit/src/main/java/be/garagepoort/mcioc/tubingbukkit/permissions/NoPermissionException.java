package be.garagepoort.mcioc.tubingbukkit.permissions;

import be.garagepoort.mcioc.tubingbukkit.exceptions.TubingBukkitException;

public class NoPermissionException extends TubingBukkitException {
    public NoPermissionException(String message) {
        super(message);
    }
}
