package de.furkan.fradiofm.instance;

import com.sedmelluq.discord.lavaplayer.player.event.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Timer;
import java.util.TimerTask;


public class TrackScheduler extends AudioEventAdapter {

    private final Guild guild;
    private final JDA jda;
    private final ServerInstance instance;

    public TrackScheduler(ServerInstance instance) {
        this.guild = instance.getGuild();
        this.jda = instance.getJda();
        this.instance = instance;
    }

    @Override
    public void onEvent(AudioEvent event) {
        if (event instanceof TrackExceptionEvent) {
            System.out.println("Restarting Audio for " + this.instance.getGuild().getName() + " " + this.instance.getGuild().getId());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    instance.playRadio();
                }
            }, 1000 * 5);
        } else if (event instanceof TrackStuckEvent) {
            System.out.println("Restarting Audio for " + this.instance.getGuild().getName() + " " + this.instance.getGuild().getId());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    instance.playRadio();
                }
            }, 1000 * 5);
        } else if (event instanceof TrackStartEvent) {
            System.out.println("Starting Track for " + guild.getName() + "\n");
        } else {
            System.out.println("AudioEvent: " + event + " for " + guild.getName());
        }
    }
}
