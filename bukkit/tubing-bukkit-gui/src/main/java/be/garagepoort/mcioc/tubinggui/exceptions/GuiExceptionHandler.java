package be.garagepoort.mcioc.tubinggui.exceptions;

import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public interface GuiExceptionHandler<T extends Throwable> extends BiConsumer<Player, T> {
}
