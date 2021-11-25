package de.furkan.fradiofm.main;

import de.furkan.fradiofm.instance.ServerInstance;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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
