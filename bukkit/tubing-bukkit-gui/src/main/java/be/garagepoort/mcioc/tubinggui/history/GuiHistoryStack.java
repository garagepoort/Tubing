package be.garagepoort.mcioc.tubinggui.history;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.tubinggui.actionquery.GuiActionQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Pattern;

@IocBean
public class GuiHistoryStack {

    private static final String SKIP_HISTORY = "$SKIP_HISTORY$";
    private final Map<UUID, Stack<String>> playerHistoryStack = new HashMap<>();

    public void push(UUID playerUuid, GuiActionQuery action, boolean overrideHistory, boolean skipHistory) {
        this.playerHistoryStack.putIfAbsent(playerUuid, new Stack<>());
        Stack<String> historyStack = this.playerHistoryStack.get(playerUuid);
        if(skipHistory) {
            historyStack.push(SKIP_HISTORY);
            return;
        }

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
        String route = stack.pop();
        while(SKIP_HISTORY.equalsIgnoreCase(route)) {
            route = stack.pop();
        }
        return Optional.ofNullable(route);
    }

    public void clear(UUID playerUuid) {
        this.playerHistoryStack.remove(playerUuid);
    }

    public boolean isLastAction(UUID playerUuid, String action) {
        this.playerHistoryStack.putIfAbsent(playerUuid, new Stack<>());
        Stack<String> stack = this.playerHistoryStack.get(playerUuid);
        if (stack.size() < 1) {
            return false;
        }
        return stack.peek().equalsIgnoreCase(action);
    }
}
