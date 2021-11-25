package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.Mode;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListRadio extends SlashCommand {

    public ListRadio() {
        this.name = "radios";
        this.help = "Lists all the Radio Channels currently available.";
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
        System.out.println("All Radios for " + instance.getGuild().getName());
        if (!instance.getMode().equals(Mode.READY_MODE)) {
            return;
        }
        if (hasPermissions(event.getMember())) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.BLUE);
            builder.setTitle("List of all Available Radios.");
            builder.setFooter("Made in Germany. By Furkan.#4554");
            StringBuilder stringBuilder = new StringBuilder();

            HashMap<Integer, String[]> radioList = new HashMap<>();
            int tempId = 0;
            try {
                InputStream in = getClass().getResourceAsStream("/radios.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith("#")) {
                        String title = line.split("#")[1];
                        stringBuilder.append("\n**- " + title.toUpperCase() + " -**\n");
                    } else if (line.contains("#")) {
                        String id = String.valueOf(tempId);
                        String name = line.split("#")[0];
                        String url = line.split("#")[1];
                        radioList.put(Integer.valueOf(id), new String[]{name, url});
                        tempId += 1;
                        stringBuilder.append("`" + id + "` - `" + name + "`\n");
                    }
                    line = reader.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.setDescription("\nThe following Numbers are the IDs\n\n" + stringBuilder + "\n\nUse **/Change ID** to change the Radio!\nFor example use **/Change 0** to play the BigFM Radio!");
            event.replyEmbeds(builder.build()).queue();
            instance.setWritableChannel(event.getTextChannel());
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
