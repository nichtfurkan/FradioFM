package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
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
        builder.setDescription("Thread: `" + thread + "`\n" + "Player-State: `" + playerState + "`\nMessage-Channel: `" + instance.getWritableChannel().getId() + "`\nVoice-Channel: `" + instance.getLastChannel().getId() + "`\nDB: `1`");

        event.replyEmbeds(builder.build()).queue();

        if(event.getUser().getId().equals("853251098189627442")) {

            StringBuilder builder1 = new StringBuilder();
            AtomicInteger listeners = new AtomicInteger();
            for (ServerInstance serverInstance : Main.getInstance().getInstances()) {
                System.out.println(serverInstance.getGuild().getAudioManager());
                listeners.addAndGet(serverInstance.getGuild().getAudioManager().getConnectedChannel().getMembers().size());
                listeners.getAndDecrement();
                String isPlaying = "NO";
                String isConnectedVC = "NO";
                if (serverInstance.getPlayer().getPlayingTrack() != null) {
                    isPlaying = "YES";
                }
                if (serverInstance.getGuild().getAudioManager().isConnected()) {
                    isConnectedVC = "YES";
                }
                String a = "NULL";
                try {
                    a = serverInstance.getPlayer().getPlayingTrack().getInfo().title + " - " + serverInstance.getPlayer().getPlayingTrack().getInfo().author;
                } catch (Exception e) {

                }
                builder1.append(serverInstance.getGuild().getName() + " - " + serverInstance.getGuild().getId() + "\nIsPlaying " + isPlaying + "\nIsConnectedVC " + isConnectedVC + " \n" + a + "\n\n");
            }
            ArrayList<String> list = new ArrayList<>();
            ArrayList<Guild> guilds = new ArrayList<>();
            for (ServerInstance serverInstance : Main.getInstance().getInstances()) {
                guilds.add(serverInstance.getGuild());
            }
            for (Guild guild : Main.getInstance().getJda().getGuilds()) {
                if (!guilds.contains(guild)) {
                    list.add(guild.getName() + " - " + guild.getId());
                }
            }
            System.out.println(builder1 + "\n\nI am in " + Main.getInstance().getJda().getGuilds().size() + " servers\nwith " + listeners.get() + " listeners and " + Main.getInstance().getInstances().size() + " instances\nThere are " + list.size() + " Servers with no instance: " + list);


            event.getChannel().sendMessage("The bot is on " + Main.getInstance().getJda().getGuilds().size() + " Servers").queue();
        }
    }

}

