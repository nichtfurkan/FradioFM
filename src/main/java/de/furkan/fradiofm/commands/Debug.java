package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import de.furkan.fradiofm.main.MemoryUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Debug extends SlashCommand {

    public Debug() {
        this.name = "debug";
        this.help = "Lets you see the some Information";
        this.category = new Category("command");
        this.botMissingPermMessage = "Looks like i dont have any Permissions for that Command :(";

        this.guildOnly = true;
        this.cooldown = 5;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        ServerInstance instance = Main.getInstanceByGuild(event.getGuild());
        if (instance == null) {
            System.out.println("Instance not found for Server. " + event.getGuild().getName() + " debug");
            return;
        }
        System.out.println("Debug for " + instance.getGuild().getName());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Debug Information");
        builder.setColor(Color.BLUE);
        int thread = instance.getThread();
        String playerState = "RUNNING";
        if (instance.getPlayer().getPlayingTrack() == null && instance.getPlayer().isPaused()) {
            playerState = "STOPPED";
        }
        builder.setDescription("BotThread: `" + thread + "`\n" + "Player-State: `" + playerState + "`\nMessage-Channel: `" + instance.getWritableChannel().getId() + "`\nVoice-Channel: `" + instance.getLastChannel().getId() + "`\nDB: `1`\n"+ MemoryUtils.getMemoryInfo());

        event.replyEmbeds(builder.build()).queue();
    }

}

