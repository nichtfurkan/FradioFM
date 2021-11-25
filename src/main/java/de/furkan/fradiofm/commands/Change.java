package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.Mode;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class Change extends SlashCommand {

    public Change() {
        this.name = "change";
        this.help = "Lets you change the Radio Channel.";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "radio-id", "Specifies the Radio ID that will be played").setRequired(true));
        this.category = new Category("command");
        this.botMissingPermMessage = "Looks like i dont have any Permissions for that Command :(";
        this.guildOnly = true;
        this.cooldown = 5;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        ServerInstance instance = Main.getInstance().getInstanceByGuild(event.getGuild());
        if (instance == null) {
            System.out.println("Instance not found for Server. " + event.getGuild().getName());
            return;
        }
        System.out.println("Change for " + instance.getGuild().getName() + " to " + event.getOption("radio-id").getAsString());
        if (!instance.getMode().equals(Mode.READY_MODE)) {
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
                event.reply("A Radio with this ID does `not exist!`\nUse **/Radios** to get a list of all Radios and their ID's").queue();
            }
        } else {
            event.reply("Looks like you dont have any Permissions for that.").queue();
        }
    }


    private boolean hasPermissions(Member member) {
        AtomicBoolean isRadioAdmin = new AtomicBoolean(false);
        member.getRoles().forEach(element -> isRadioAdmin.set(element.getName().equals("RadioAdmin")));
        return PermissionUtil.checkPermission(member, Permission.ADMINISTRATOR) || isRadioAdmin.get() || member.getId().equals("853251098189627442");
    }

}
