package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Privacy extends SlashCommand {

    public Privacy() {
        this.name = "privacy";
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
        if (hasPermissions(event.getMember())) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Privacy Policy");
            builder.setColor(Color.BLUE);
            builder.setDescription("\n**What Data are we collecting**\nWe are collecting data for Troubleshooting\n\n**We are collectiong following data**\n- Radio Channel that is being played (URL)\n- Message Channel where the Bot got Commands (ID)\n- Last Joined VoiceChannel (ID)\n- Discord Server ID and Name (For better orientation in the Database)\n**No Data is being sold or given to any Third-Party**\n\nThe Data collection can be disabled by messaging **Furkan.#4554** to do so.\nIf you want to delete your Data in the Database also contact **Furkan.#4554**.");
            event.replyEmbeds(builder.build()).queue();
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
