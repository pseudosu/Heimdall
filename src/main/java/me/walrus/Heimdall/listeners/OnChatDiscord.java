package me.walrus.Heimdall.listeners;

import me.walrus.Heimdall.Heimdall;
import me.walrus.Heimdall.util.Util;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class OnChatDiscord implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isYourself())
            return;
        ServerTextChannel textChannel = event.getServerTextChannel().get();
        if (textChannel.getIdAsString().equals(Heimdall.getConfigManager().BOT_STAFF_CHAT)) {
            String message = event.getMessage().getContent();
            String username = event.getMessageAuthor().getName();
            String nickname = event.getMessageAuthor().getDisplayName();
            Util.sendStaff(Util.formatDiscordStaffChat(username, nickname, message), false);
        }
    }
}
