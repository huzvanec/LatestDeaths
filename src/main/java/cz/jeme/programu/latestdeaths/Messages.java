package cz.jeme.programu.latestdeaths;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Messages {
    private Messages() {
    }
    public static final MiniMessage MESSAGE = MiniMessage.miniMessage();
    public static final String PREFIX = "<dark_gray>[<gold>LatestDeaths</gold>]</dark_gray> ";

    public static Component from(String string) {
        return MESSAGE.deserialize(string);
    }
    public static Component prefix(String string) {
        return from(PREFIX + string);
    }
    public static String strip(String text) {
        return MESSAGE.stripTags(text);
    }
    public static String strip(Component component) {
        return strip(to(component));
    }
    public static String to(Component component) {
        return MESSAGE.serialize(component);
    }
    public static String getEscapeTag(String string) {
        return string.replace("<", "</");
    }
}
