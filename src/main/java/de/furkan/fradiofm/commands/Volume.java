package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class Volume extends SlashCommand {

    public Volume() {
        this.name = "volume";
        this.help = "Lets you change the Volume of the Bot.";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "volume", "Specifies the Volume. (1 - 1000)").setRequired(true));
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
            System.out.println("Instance not found for Server. " + event.getGuild().getName() + " volume");
            return;
        }
        System.out.println("Volume for " + instance.getGuild().getName() + " to " + event.getOption("volume").getAsString());

        if (hasPermissions(event.getMember())) {
            OptionMapping option = event.getOption("volume");
            int setVolume = Integer.parseInt(option.getAsString());
            if (setVolume > 0 && setVolume <= 1000) {
                event.reply("Volume set to `" + setVolume + "` Before `" + instance.getPlayer().getVolume() + "`").queue();
                instance.getPlayer().setVolume(setVolume);
            } else {
                event.reply("Please choose a value between `1` or `1000`").queue();
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
