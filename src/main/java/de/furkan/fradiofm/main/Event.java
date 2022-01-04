package de.furkan.fradiofm.main;

import de.furkan.fradiofm.instance.ServerInstance;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Event extends ListenerAdapter {

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getMember().getId().equals(event.getJDA().getSelfUser().getId())) {
            ServerInstance instance = Main.getInstance().getInstanceByGuild(event.getGuild());

            if (instance == null) {
                System.out.println("Instance not found for Server. " + event.getGuild().getName());
                return;
            }

            instance.setLastChannel(event.getNewValue());
            System.out.println("Save move for " + event.getGuild().getName() + " - " + event.getGuild().getId());
        }
    }


    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        try {
            ServerInstance instance = Main.getInstance().getInstanceByGuild(event.getGuild());
            if (instance.getGuild() != null) {
                instance.delete();
            }
        } catch (Exception e) {

        }
    }
}
