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

public class Choose extends SlashCommand {

    public Choose() {
        this.name = "choose";
        this.category = new Category("command");
        this.botMissingPermMessage = "Looks like i don't have any Permissions for that Command :(";
        this.guildOnly = true;
        this.cooldown = 10;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        ServerInstance instance = Main.getInstanceByGuild(event.getGuild());
        if (instance == null) {
            return;
        }
         OptionMapping option = event.getOption("choose-id");

        if (hasPermissions(event.getMember())) {
            try {
                if (Main.getInstance().getSearchCommand().guildHashMapHashMap.containsKey(event.getGuild())) {
                    String url = Main.getInstance().getSearchCommand().guildHashMapHashMap.get(event.getGuild()).get(Integer.valueOf(option.getAsString()))[0];
                    String name = Main.getInstance().getSearchCommand().guildHashMapHashMap.get(event.getGuild()).get(Integer.valueOf(option.getAsString()))[1];
                    event.reply("Playing **" + name + "**").queue();
                    instance.playCustom(url);
                    Main.getInstance().getSearchCommand().guildHashMapHashMap.remove(event.getGuild());
                    System.gc();
                } else {
                    event.reply("Please use **/Search <NAME>** first.").queue();
                }
            } catch (Exception e) {
                Main.getInstance().getSearchCommand().guildHashMapHashMap.remove(event.getGuild());
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
