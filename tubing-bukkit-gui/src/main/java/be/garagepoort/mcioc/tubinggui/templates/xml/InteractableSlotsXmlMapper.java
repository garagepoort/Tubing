package be.garagepoort.mcioc.tubinggui.templates.xml;

import be.garagepoort.mcioc.IocBean;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;

@IocBean
public class InteractableSlotsXmlMapper {

    private static final String INTERACTABLE_SLOTS_ATTR = "interactableSlots";

    public List<Integer> map(Element tubingGuiElement) {
        if (StringUtils.isBlank(tubingGuiElement.attr(INTERACTABLE_SLOTS_ATTR))) {
            return new ArrayList<>();
        }

        List<Integer> slots = new ArrayList<>();

        List<String> ranges = Arrays.stream(tubingGuiElement.attr(INTERACTABLE_SLOTS_ATTR).split(",")).collect(Collectors.toList());
        for (String range : ranges) {
            String[] split = range.split("\\.\\.");
            if (split.length > 2) {
                throw new RuntimeException("Invalid interactable slots configuration");
            }
            if (split.length == 1) {
                slots.add(parseInt(split[0]));
            } else {
                IntStream.rangeClosed(parseInt(split[0]), parseInt(split[1])).forEach(slots::add);
            }
        }
        return slots;
    }
}
