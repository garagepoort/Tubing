package be.garagepoort.mcioc.gui.history;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.actionquery.GuiActionQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Pattern;

@IocBean
public class GuiHistoryStack {

    private final Map<UUID, Stack<String>> playerHistoryStack = new HashMap<>();

    public void push(UUID playerUuid, GuiActionQuery action, boolean overrideHistory) {
        this.playerHistoryStack.putIfAbsent(playerUuid, new Stack<>());
        Stack<String> historyStack = this.playerHistoryStack.get(playerUuid);

        if (!historyStack.isEmpty()) {
            String lastRoute = historyStack.peek().split(Pattern.quote("?"), 2)[0];
            if (action.getRoute().equalsIgnoreCase(lastRoute) && overrideHistory) {
                historyStack.pop();
            }
        }
        historyStack.push(action.getFullQuery());
    }

    public Optional<String> pop(UUID playerUuid) {
        this.playerHistoryStack.putIfAbsent(playerUuid, new Stack<>());
        Stack<String> stack = this.playerHistoryStack.get(playerUuid);
        if (stack.size() < 2) {
            return Optional.empty();
        }
        stack.pop();
        return Optional.ofNullable(stack.pop());
    }

    public void clear(UUID playerUuid) {
        this.playerHistoryStack.remove(playerUuid);
    }
}
