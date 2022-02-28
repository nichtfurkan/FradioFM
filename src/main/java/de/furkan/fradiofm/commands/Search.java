package de.furkan.fradiofm.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.main.Main;
import de.sfuhrm.radiobrowser4j.FieldName;
import de.sfuhrm.radiobrowser4j.ListParameter;
import de.sfuhrm.radiobrowser4j.RadioBrowser;
import de.sfuhrm.radiobrowser4j.SearchMode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Search extends SlashCommand {

    public HashMap<Guild, HashMap<Integer, String[]>> guildHashMapHashMap = new HashMap<>();

    public Search() {
        this.name = "search";
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
        OptionMapping option = event.getOption("search-name");
        if (guildHashMapHashMap.containsKey(event.getGuild())) {
            event.reply("Please use **/Choose <RADIO-ID>**").queue();
            return;
        }
        if (hasPermissions(event.getMember())) {
            event.reply("Searching for **" + option.getAsString() + "**").queue();

            HashMap<Integer, String[]> radioHashMap = new HashMap<>();
            StringBuilder stringBuilder = new StringBuilder();
            AtomicInteger hashmapNumber = new AtomicInteger(0);
            RadioBrowser browser = new RadioBrowser(15000, "Demo agent/1.0");
            String name = option.getAsString();

            browser.listStationsBy(SearchMode.BYNAME, name, ListParameter.create().order(FieldName.NAME)).limit(99).forEach(element -> {
                String country = element.getCountry().length() > 1 ? "`" + element.getCountry() + "`" : "`Unknown Country`";
                if (element.getUrl().contains("mp3") && !stringBuilder.toString().contains(element.getName())) {
                    stringBuilder.append("`" + hashmapNumber.get() + "` : " + element.getName() + " - " + country + "\n");

                    radioHashMap.put(hashmapNumber.getAndIncrement(), new String[]{element.getUrl(), element.getName()});
                }
            });

            String[] a = name.split(" ");
            for (String s : a) {
                browser.listStationsBy(SearchMode.BYNAME, s, ListParameter.create().order(FieldName.NAME)).limit(99).forEach(element -> {
                    String country = element.getCountry().length() > 1 ? "`" + element.getCountry() + "`" : "`Unknown Country`";
                    if (element.getUrl().contains("mp3") && !stringBuilder.toString().contains(element.getName())) {
                        stringBuilder.append("`" + hashmapNumber.get() + "` : " + element.getName() + " - " + country + "\n");

                        radioHashMap.put(hashmapNumber.getAndIncrement(), new String[]{element.getUrl(), element.getName()});
                    }
                });
            }

            if (stringBuilder.length() == 0) {
                event.getChannel().sendMessage("`Sorry but we couldn't find a Radio Station that matches the given name`").queue();
                return;
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.BLUE);
            builder.setAuthor("Made in Germany by Furkan.#4554");
            builder.setTitle("We've found the Following Radio Stations");
            if (stringBuilder.length() > 4020) {
                builder.setDescription("\nTo Choose please use **/Choose <ID>**\n\n" + stringBuilder.substring(0, 4019));
            } else {
                builder.setDescription("\nTo Choose please use **/Choose <ID>**\n\n" + stringBuilder);
            }

            event.getChannel().sendMessageEmbeds(builder.build()).queue();
            guildHashMapHashMap.put(event.getGuild(), radioHashMap);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    guildHashMapHashMap.remove(event.getGuild());
                }
            }, 1000 * 60);
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
