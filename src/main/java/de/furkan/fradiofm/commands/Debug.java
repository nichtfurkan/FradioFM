package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import de.furkan.fradiofm.main.MemoryUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;

public class Debug extends SlashCommand {

    public Debug() {
        this.name = "debug";
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
            return;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Debug Information");
        embedBuilder.setColor(Color.BLUE);
        int thread = instance.getThread();
        String playerState = "RUNNING";
        if (instance.getPlayer().getPlayingTrack() == null && instance.getPlayer().isPaused()) {
            playerState = "STOPPED";
        }
        embedBuilder.setDescription("BotThread: `" + thread + "`\n" + "Player-State: `" + playerState + "`\nMessage-Channel: `" + instance.getWritableChannel().getId() + "`\nVoice-Channel: `" + instance.getLastChannel().getId() + "`\n" + MemoryUtils.getMemoryInfo() + "\nLogic-Cores: `" + Runtime.getRuntime().availableProcessors() + "`\nOS: `" + System.getProperty("os.name") + "`");
        event.replyEmbeds(embedBuilder.build()).queue();
    }

}

