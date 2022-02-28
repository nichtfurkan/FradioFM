package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class Join extends SlashCommand {

    public Join() {
        this.name = "join";
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
            return;
        }
        if (hasPermissions(event.getMember())) {
            if (instance.getGuild().getAudioManager().isConnected()) {
                event.reply("Already connected to Voice Channel `" + instance.getGuild().getAudioManager().getConnectedChannel().getName() + "`").queue();
                return;
            }
            String voice = instance.joinLeastVoice();
            if (voice != null) {
                event.reply("Joining Voice `" + voice + "`").queue();
                instance.setWritableChannel(event.getTextChannel());
            } else {
                event.reply("Can't join any Voice Channel. Do i have the right permissions?").queue();
                instance.setWritableChannel(event.getTextChannel());
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
