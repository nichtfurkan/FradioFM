package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;

public class Help extends SlashCommand {

    public Help() {
        this.name = "help";
        this.help = "Lets you see the the Help Message";
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
            System.out.println("Instance not found for Server. " + event.getGuild().getName() + " help");
            return;
        }
        System.out.println("Help for " + instance.getGuild().getName());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Help");
        builder.setColor(Color.BLUE);
        builder.setDescription("You can use **/Join** To let me Join a Voice Channel\nor **/Change ID** To change the Radio Channel.\nYou can use **/Radios** to get a list of all available Radios and their ID's\nor **/Custom URL** to play a Custom Radio/Youtube-Livestream.\n**/Suggest** To suggest a Radio that will be added to the Official List\n**/Privacy** To see our Privacy Policy\n**/Volume (1 - 1000)** To change the Volume.\n\nYou can move the Bot in any other Voice Channel and it will stay there forever.\n\nIf you want to give other People the permissions to use me\nJust create a Role with the name `RadioAdmin`\nAnd i will listen to them.");

        event.replyEmbeds(builder.build()).queue();
    }

}

