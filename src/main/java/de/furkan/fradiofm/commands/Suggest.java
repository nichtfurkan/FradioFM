package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Suggest extends SlashCommand {

    public Suggest() {
        this.name = "suggest";
        this.help = "Lets you suggest a Radio channel that will be added in the Official list.";
        this.options = Arrays.asList(new OptionData(OptionType.STRING, "radio-name", "Specifies the Name of the Radio that will be suggested.").setRequired(true), new OptionData(OptionType.STRING, "radio-country", "Specifies the Country of the Radio that will be suggested.").setRequired(true), new OptionData(OptionType.STRING, "radio-mp3-url", "Specifies the Url of the Radio that will be suggested.").setRequired(false), new OptionData(OptionType.BOOLEAN, "anonymous-suggestion", "Specifies if the Suggestion is anonymous.").setRequired(true));
        this.category = new Category("command");
        this.botMissingPermMessage = "Looks like i dont have any Permissions for that Command :(";
        this.guildOnly = true;
        this.cooldown = 300;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        ServerInstance instance = Main.getInstanceByGuild(event.getGuild());
        if (instance == null) {
            System.out.println("Instance not found for Server. " + event.getGuild().getName() + " suggest");
            return;
        }
        System.out.println("Suggestion for " + instance.getGuild().getName() + "-" + instance.getGuild().getId());

        if (hasPermissions(event.getMember())) {
            String name = event.getOption("radio-name").getAsString();
            String country = event.getOption("radio-country").getAsString();
            String url = event.getOption("radio-mp3-url") != null ? event.getOption("radio-mp3-url").getAsString() : "Not Specified";
            boolean anonymous = event.getOption("anonymous-suggestion").getAsBoolean();

            if (name.contains("nigger") || country.contains("nigger") || url.contains("nigger")) {
                event.reply("Sorry but this isn't allowed here.\nFor more Information please join the Official Discord Server\n**https://discord.gg/4pwp72s62c**").queue();
                instance.getGuild().leave().queue();
                instance.delete();
                return;
            }

            for (ServerInstance serverInstance : Main.instances) {
                if (serverInstance.getGuild().getId().equals("912552900507615243")) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.BLUE);
                    builder.setTitle("New Radio Suggestion");
                    if (anonymous) {
                        builder.setAuthor("Suggested by Anonymous");
                    } else {
                        builder.setAuthor("Suggested by " + event.getMember().getUser().getAsTag());
                    }
                    builder.setDescription("\nRadio-Name: `" + name + "`\nRadio-Country: `" + country + "`\nRadio-Url: `" + url + "`");
                 /*   serverInstance.getGuild().getTextChannelById("917854779076673556").sendMessage(builder.build()).queue(message -> {
                        message.addReaction("U+2705").queue();

                        message.addReaction("U+274C").queue();
                    });*/
                    serverInstance.getGuild().getTextChannelById("917854779076673556").sendMessageEmbeds(builder.build()).queue(message -> {
                        message.addReaction("U+2705").queue();

                        message.addReaction("U+274C").queue();
                    });
                    event.reply("Suggestion successfully delivered.\nPlease beware that Spamming this feature or Suggesting anything rude/disturbing\nwill result in a **Lifetime Server-Ban**\n\nThe Suggestion is in the Radio-Suggestions Channel on the Official Discord Server.\nThere you can see if it has been accepted or not.").queue();
                }
            }
        } else {
            event.reply("Looks like you don't have any Permissions for that.").queue();
        }
    }


    private boolean hasPermissions(Member member) {
        AtomicBoolean isRadioAdmin = new AtomicBoolean(false);
        member.getRoles().forEach(element -> isRadioAdmin.set(element.getName().equals("RadioAdmin")));
        return PermissionUtil.checkPermission(member, Permission.ADMINISTRATOR) || isRadioAdmin.get() || member.getId().equals("853251098189627442");
    }


}
