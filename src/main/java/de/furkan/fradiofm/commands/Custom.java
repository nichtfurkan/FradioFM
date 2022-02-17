package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class Custom extends SlashCommand {

    public Custom() {
        this.name = "custom";
        this.category = new Category("command");
        this.botMissingPermMessage = "Looks like i don't have any Permissions for that Command :(";
        this.guildOnly = true;
        this.cooldown = 5;
        this.ownerCommand = false;
    }
    @Override
    protected void execute(SlashCommandEvent event) {
        ServerInstance instance = Main.getInstance().getInstanceByGuild(event.getGuild());
        if (instance == null) {
            return;
        }
        if (hasPermissions(event.getMember())) {
            OptionMapping option = event.getOption("radio-mp3-url");
            if (option.getAsString().contains("localhost") || option.getAsString().contains("127.0.0.1")) {
                event.reply("Sorry but that isn't allowed here.\nFor more information please join the Official Discord Server and get Support.\nhttps://discord.gg/4pwp72s62c").queue();
                return;
            } else {
                event.reply("**Trying to play a Custom URL**").queue();
                instance.playCustom(option.getAsString());
                instance.setWritableChannel(event.getTextChannel());
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
