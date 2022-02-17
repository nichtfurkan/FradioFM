package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Change extends SlashCommand {

    public Change() {
        this.name = "change";
        this.category = new Category("command");
        this.botMissingPermMessage = "Looks like i don't have any Permissions for that Command :(";
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
            if (hasPermissions(event.getMember())) {
                OptionMapping option = event.getOption("radio-id");
                try {
                    if (Main.getInstance().getRadioUtil().getRadioUrlById(Integer.parseInt(option.getAsString())) != null) {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(Color.BLUE);
                        builder.setAuthor("Playing Now!");
                        builder.setTitle(Main.getInstance().getRadioUtil().getRadioNameById(Integer.parseInt(option.getAsString())));
                        builder.setDescription("Radio has been changed by " + event.getMember().getAsMention() + "\nUse **/Change ID** To change the Radio Channel!");
                        instance.setRadioUrl(Main.getInstance().getRadioUtil().getRadioUrlById(Integer.parseInt(option.getAsString())));
                        instance.playRadio();
                        event.replyEmbeds(builder.build()).queue();
                        instance.setWritableChannel(event.getTextChannel());

                    } else {
                        event.reply("A Radio with this ID does `not exist!`\nUse **/Radios** to get a list of all Radios and their ID's").queue();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getCause() instanceof MissingAccessException) {
                        event.reply("The Bot doesn't have Permissions to write/see this Channel.\nPlease make sure to give him the Right Permissions!").queue();
                    } else {
                        event.reply("An Error Occurred while Changing the Radio. Please try again later or contact Support.").queue();

                    }
                }
            } else {
                event.reply("A Radio with this ID does `not exist!`\nUse **/Radios** to get a list of all Radios and their ID's").queue();
            }

    }


    private boolean hasPermissions(Member member) {
        AtomicBoolean isRadioAdmin = new AtomicBoolean(false);
        member.getRoles().forEach(element -> isRadioAdmin.set(element.getName().equals("RadioAdmin")));
        return PermissionUtil.checkPermission(member, Permission.ADMINISTRATOR) || isRadioAdmin.get() || member.getId().equals("853251098189627442");
    }

}
