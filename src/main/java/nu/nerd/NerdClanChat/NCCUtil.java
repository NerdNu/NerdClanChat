package nu.nerd.NerdClanChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import nu.nerd.NerdClanChat.database.Channel;

import java.util.*;
import java.util.List;

public class NCCUtil {


    public static String joinArray(String separator, String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
            sb.append(separator);
        }
        return sb.toString().trim();
    }


    public static TextComponent formatList(List<String> list, NamedTextColor color1, NamedTextColor color2) {
        TextComponent listOutput = Component.text("");

        for(String item : list) {
            if(list.indexOf(item) % 2 == 0) {
                listOutput = listOutput.append(Component.text(item, color1));
            } else {
                listOutput = listOutput.append(Component.text(item, color2));
            }
            if(list.indexOf(item) != (list.size() - 1)) {
            listOutput = listOutput.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }
        return listOutput;
    }


    public static TextComponent formatChannelList(List<Channel> list) {
        TextComponent channelList = Component.text("");
        for(Channel channel : list) {
            String color = channel.getColor();
            channelList = channelList.append(Component.text(channel.getName(), color(color)));
            if(list.indexOf(channel) != (list.size() - 1)) {
                channelList = channelList.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }
        return channelList;
    }


    public static TextComponent formatColorList(List<String> list) {
        TextComponent stringList = Component.text("");
        for(String item : list) {
            stringList = stringList.append(Component.text(item, NamedTextColor.NAMES.value(item.toLowerCase())));
            if(list.indexOf(item) != (list.size() - 1)) {
                stringList = stringList.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }
        return stringList;
    }


    public static List<String> colorList() {
        List<String> colorList = new ArrayList<>();
        Set<String> blacklist = new HashSet<>();
        blacklist.add("MAGIC");
        blacklist.add("BOLD");
        blacklist.add("STRIKETHROUGH");
        blacklist.add("UNDERLINE");
        blacklist.add("ITALIC");
        blacklist.add("RESET");
        for(String color : NamedTextColor.NAMES.keys()) {
            colorList.add(color.toUpperCase());
        }
        colorList.removeAll(blacklist);
        return colorList;
    }

    public static TextColor color(String color) {
        return color.charAt(0) == '#' ? TextColor.fromHexString(color) : NamedTextColor.NAMES.value(color.toLowerCase());
    }


}
