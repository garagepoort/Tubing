package be.garagepoort.mcioc.gui.exceptions;

import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public interface GuiExceptionHandler extends BiConsumer<Player, Throwable> {
}
