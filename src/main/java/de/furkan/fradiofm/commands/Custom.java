package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.Mode;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class Custom extends SlashCommand {

    public Custom() {
        this.name = "custom";
        this.help = "Lets you play your own Radio Channel.";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "radio-mp3-url", "Specifies the Radio URL that the bot will listen to.").setRequired(true));

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
        System.out.println("Custom for " + instance.getGuild().getName());
        if (!instance.getMode().equals(Mode.READY_MODE)) {
            return;
        }
        if (hasPermissions(event.getMember())) {
            OptionMapping option = event.getOption("radio-mp3-url");
            if (!this.checkOnline(option.getAsString())) {
                event.reply("This URL is not online.\n**" + option.getAsString() + "**").queue();
                return;
            } else {
                event.reply("Trying to play a Custom URL\n").queue();
                instance.playCustom(option.getAsString());
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

    private boolean checkOnline(String host) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(host).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}
