package be.garagepoort.mcioc.tubinggui.model;

import java.util.ArrayList;
import java.util.List;

public class TubingChatGui {

    private final List<String> chatLines;

    public TubingChatGui(List<String> chatLines) {
        this.chatLines = chatLines;
    }

    public List<String> getChatLines() {
        return chatLines;
    }

    public static class Builder {
        private final List<String> chatLines = new ArrayList<>();

        public Builder() {}

        public Builder addLine(String line) {
            this.chatLines.add(line);
            return this;
        }

        public TubingChatGui build() {
            return new TubingChatGui(chatLines);
        }
    }
}
