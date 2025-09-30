package nu.nerd.NerdClanChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import nu.nerd.NerdClanChat.database.Channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a message sent through normal NerdClanChat use (i.e. /c, /cs, /cme, /ca)
 */
public class Message {

    /**
     * The text of the message being sent.
     */
    private String message;

    /**
     * The name of the player sending the message. If console is sending, this is "~console".
     */
    private String playerName;

    /**
     * The channel whose members this message is being sent to.
     */
    private Channel channel;

    /**
     * The type of message.
     */
    private MessageType messageType;

    /**
     * The regex used for matching URLs so they can become clickable.
     */
    private final String URL_REGEX =
            "\\b(?:https?://)?(?:www\\.)?[a-zA-Z0-9-]+(?:\\.[a-zA-Z]{2,})+(?:/[^\\s]*)?";
    private final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    /**
     * The type of message.
     */
    public enum MessageType {
        NORMAL, ME, ALERT, SARCASM
    }

    /**
     * The contructor for this class which gathers all necessary components to build a message.
     *
     * @param message the content of the message being sent
     * @param playerName the player sending the message
     * @param channel the channel the message is being sent to
     * @param messageType the type of message being sent
     */
    public Message(String message, String playerName, Channel channel, MessageType messageType) {
        this.message = message;
        this.playerName = playerName;
        this.channel = channel;
        this.messageType = messageType;
    }

    /**
     * Puts all the pieces together to build and style the message according to the channel's specifications.
     *
     * @return the completed message
     */
    public TextComponent build() {
        TextComponent messageComponent = Component.empty();
        messageComponent = buildPrefix(messageComponent);
        messageComponent = buildIdentifier(messageComponent);
        messageComponent = buildMessage(messageComponent);
        return messageComponent;
    }

    /**
     * Builds the channel prefix of the message.
     *
     * @param messageComponent the main component being built up
     * @return the appropriate prefix for the message
     */
    private TextComponent buildPrefix(TextComponent messageComponent) {
        return messageComponent.append(Component.text("[" + channel.getName() + "] ", NCCUtil.color(channel.getColor())));
    }

    /**
     * Builds the identifier of the message, which is where the player's name is.
     *
     * @param messageComponent the main component being built up
     * @return the appropriate identifier for the type of message
     */
    private TextComponent buildIdentifier(TextComponent messageComponent) {
        if(messageType != MessageType.ME) {
            return messageComponent.append(Component.text("<", NamedTextColor.GRAY))
                    .append(Component.text(playerName, NamedTextColor.WHITE))
                    .append(Component.text("> ", NamedTextColor.GRAY));
        }
        return messageComponent.append(Component.text("* " + playerName + " ",
                NCCUtil.color(channel.getText_color())));
    }

    /**
     * Builds and styles the actual content of the message.
     *
     * @param messageComponent the main component being built up
     * @return the completed, styled message
     */
    private TextComponent buildMessage(TextComponent messageComponent) {
        Matcher matcher = URL_PATTERN.matcher(message);
        int lastEnd = 0;

        while(matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if(start > lastEnd) {
                String beforeText = message.substring(lastEnd, start);
                messageComponent = messageComponent.append(styleMessage(beforeText));
            }

            String url = matcher.group();
            String fullUrl = normalizeURL(url);

            Component linkComponent = styleMessage(url)
                    .clickEvent(ClickEvent.openUrl(fullUrl))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Click to open", NamedTextColor.YELLOW)
                    ));

            messageComponent = messageComponent.append(linkComponent);
            lastEnd = end;
        }

        if(lastEnd < message.length()) {
            String remainingText = message.substring(lastEnd);
            messageComponent = messageComponent.append(styleMessage(remainingText));
        }
        return messageComponent;
    }

    /**
     * Applies appropriate style to a section of a message.
     *
     * @param messageSection the text this particular section is composed of
     * @return a styled TextComponent
     */
    private TextComponent styleMessage(String messageSection) {
        // All messages but alert use textcolor. Group them up.
        if(messageType != MessageType.ALERT) {
            TextComponent sectionComponent = Component.text(messageSection, NCCUtil.color(channel.getText_color()));
            // If sarcastic, slant it
            if(messageType == MessageType.SARCASM) sectionComponent = sectionComponent.decorate(TextDecoration.ITALIC);
            return sectionComponent;
        } else {
            return Component.text(messageSection, NCCUtil.color(channel.getAlert_color()), TextDecoration.UNDERLINED);
        }
    }

    /**
     * If a link is detected, but it's missing http:// or https://, add https:// to the end of it.
     *
     * @param url the URL being normalized
     * @return the normalized URL
     */
    private String normalizeURL(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

}
