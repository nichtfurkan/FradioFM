package de.furkan.fradiofm.main;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import de.furkan.fradiofm.commands.*;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.util.RadioUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private static final Volume volumeCommand = new Volume();
    private static final ListRadio radioCommand = new ListRadio();
    private static final Join joinCommand = new Join();
    private static final Change changeCommand = new Change();
    private static final Custom customCommand = new Custom();
    private static final Privacy privacyCommand = new Privacy();
    private static final Debug debugCommand = new Debug();
    private static final Help helpCommand = new Help();
    private static final Search searchCommand = new Search();
    private static final Choose chooseCommand = new Choose();
    public static ArrayList<ServerInstance> instances;


    private static JDA jda;
    private static Main instance;
    private final RadioUtil radioUtil = new RadioUtil();

    public static Main getInstance() {
        return instance;
    }

    static final int threads = 4;

    public JDA getJda() {
        return jda;
    }

    public static void main(String[] args) {


        instances = new ArrayList<>();
        instance = new Main();

        try {
            jda = JDABuilder.createLight("", GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS).disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS).build().awaitReady();
            jda.getPresence().setPresence(OnlineStatus.ONLINE, false);
            jda.addEventListener(new Event());
            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands(new CommandData("join", "Lets the Bot join the first Voice Channel where it has Permission to."));
            commands.addCommands(new CommandData("change", "Lets you change the Radio Channel.").addOption(OptionType.INTEGER, "radio-id", "Specifies the Radio ID that will be played.", true));
            commands.addCommands(new CommandData("custom", "Lets you play your own Radio Channel.").addOption(OptionType.STRING, "radio-mp3-url", "Specifies the Radio URL that the bot will listen to. Supported are: MP3 URL's", true));
            commands.addCommands(new CommandData("radios", "Lists all the Radio Channels currently available."));
            commands.addCommands(new CommandData("privacy", "Lets you see the Privacy Policy."));
            commands.addCommands(new CommandData("volume", "Lets you change the Volume of the Bot.").addOption(OptionType.INTEGER, "volume", "Specifies the Volume. (1- 1000)", true));
            commands.addCommands(new CommandData("suggest", "Lets you suggest a Radio channel that will be added in the Official list.").addOptions(Arrays.asList(new OptionData(OptionType.STRING, "radio-name", "Specifies the Name of the Radio that will be suggested.").setRequired(true), new OptionData(OptionType.STRING, "radio-country", "Specifies the Country of the Radio that will be suggested.").setRequired(true), new OptionData(OptionType.BOOLEAN, "anonymous-suggestion", "Specifies if the Suggestion is anonymous.").setRequired(true), new OptionData(OptionType.STRING, "radio-mp3-url", "Specifies the Url of the Radio that will be suggested.").setRequired(false))));

            commands.addCommands(new CommandData("debug", "Lets you see some Information."));
            commands.addCommands(new CommandData("help", "Lets you see the Help Message."));
            commands.addCommands(new CommandData("search", "Lets you search a Radio Channel.").addOption(OptionType.STRING, "search-name", "Specifies the name of the Radio to search.", true));
            commands.addCommands(new CommandData("choose", "Lets you choose a Searched Radio Channel.").addOption(OptionType.INTEGER, "choose-id", "Specifies id to choose.", true));
            commands.queue();

            CommandClient client = new CommandClientBuilder().setOwnerId("853251098189627442").build();
            client.addSlashCommand(radioCommand);
            client.addSlashCommand(changeCommand);
            client.addSlashCommand(joinCommand);
            client.addSlashCommand(customCommand);
            client.addSlashCommand(volumeCommand);
            client.addSlashCommand(privacyCommand);
            client.addSlashCommand(debugCommand);
            client.addSlashCommand(helpCommand);
            client.addSlashCommand(searchCommand);
            client.addSlashCommand(chooseCommand);
            jda.addEventListener(client);
            jda.addEventListener(new Event());

/*
            int guildSplit = Math.round(jda.getGuilds().size() / threads);
            ArrayList<BotThread> threadArrayList = new ArrayList<>();
            for (int i = 1; i < threads; i++) {
                threadArrayList.add(new BotThread(jda, Partition.ofSize(jda.getGuilds(), guildSplit).get(i), i));
                Thread.sleep(3000);
            }

            ArrayList<Guild> checkGuilds = new ArrayList<>();
            for (ServerInstance serverInstance : instances) {
                if (serverInstance != null && serverInstance.getGuild() != null) {
                    checkGuilds.add(serverInstance.getGuild());
                }
            }*/
            for (Guild guild : jda.getGuilds()) {
                //     if (!checkGuilds.contains(guild)) {
                instances.add(new ServerInstance(jda, guild, 0));
                //   }
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    jda.getPresence().setPresence(Activity.playing("to " + jda.getGuilds().size() + " Servers"), false);
                }
            }, 1000 * 12, 1000 * 12);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    MemoryUtils.freeMemory();
                    for (ServerInstance serverInstance : instances) {
                        if (!serverInstance.getGuild().getAudioManager().isConnected() && serverInstance.getLastChannel() != null) {
                            serverInstance.getGuild().getAudioManager().openAudioConnection(serverInstance.getLastChannel());
                        }
                        if (serverInstance.getPlayer().getPlayingTrack() == null) {
                            serverInstance.playRadio();
                        }
                    }
                }
            }, 1000 * 120, 1000 * 120);
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Search getSearchCommand() {
        return searchCommand;
    }

    public static ServerInstance getInstanceByGuild(Guild guild) {
        for (ServerInstance serverInstance : instances) {
            if (serverInstance.getGuild().getId().equals(guild.getId())) {
                return serverInstance;
            }
        }
        return null;
    }

    public ArrayList<ServerInstance> getInstances() {
        return instances;
    }

    public RadioUtil getRadioUtil() {
        return radioUtil;
    }

}
