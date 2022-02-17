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
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    instance.playRadio();
                }
            }, 1000 * 5);
        } else if (event instanceof TrackStuckEvent) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    instance.playRadio();
                }
            }, 1000 * 5);
        }
    }
}
