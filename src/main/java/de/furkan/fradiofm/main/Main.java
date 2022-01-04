package de.furkan.fradiofm.main;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import de.furkan.fradiofm.commands.*;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.util.RadioUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main implements EventListener {

    private static final Volume volumeCommand = new Volume();
    private static final ListRadio radioCommand = new ListRadio();
    private static final Join joinCommand = new Join();
    private static final Change changeCommand = new Change();
    private static final Custom customCommand = new Custom();
    private static final Privacy privacyCommand = new Privacy();
    private static final Suggest suggestCommand = new Suggest();
    private static final Debug debugCommand = new Debug();
    private static final Help helpCommand = new Help();
    public static ArrayList<ServerInstance> instances;


    private static JDA jda;
    private static Main instance;
    private static boolean testingMode = false;
    private final RadioUtil radioUtil = new RadioUtil();

    public static Main getInstance() {
        return instance;
    }

    public JDA getJda() {
        return jda;
    }

    static int threads = 3;
    public ArrayList<String> setupUser = new ArrayList<>();


    public static void main(String[] args) {
        if (args.length != 0 && args[0].equals("testing")) {
            testingMode = true;
        }
        instances = new ArrayList<>();
        instance = new Main();

        try {
            jda = JDABuilder.createLight("OTExNzk2MTY4MTYwMzE3NTEx.YZmmWg.CfFuzhCf_pb5wZclgvB7ZbaddTw", GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS).disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS).build().awaitReady();
            jda.getPresence().setPresence(OnlineStatus.ONLINE, false);
            jda.addEventListener(new Event());
            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands(new CommandData("join", "Lets the Bot join the first Voice Channel where it has Permission to."));
            commands.addCommands(new CommandData("change", "Lets you change the Radio Channel.").addOption(OptionType.INTEGER, "radio-id", "Specifies the Radio ID that will be played.", true));
            commands.addCommands(new CommandData("custom", "Lets you play your own Radio Channel.").addOption(OptionType.STRING, "radio-mp3-or-youtube-twitch-url", "Specifies the Radio URL that the bot will listen to. Supported are: Youtube/Twitch and MP3 URL's", true));
            commands.addCommands(new CommandData("radios", "Lists all the Radio Channels currently available."));
            commands.addCommands(new CommandData("privacy", "Lets you see the Privacy Policy."));
            commands.addCommands(new CommandData("volume", "Lets you change the Volume of the Bot.").addOption(OptionType.INTEGER, "volume", "Specifies the Volume. (1- 1000)", true));
            commands.addCommands(new CommandData("suggest", "Lets you suggest a Radio channel that will be added in the Official list.").addOptions(Arrays.asList(new OptionData(OptionType.STRING, "radio-name", "Specifies the Name of the Radio that will be suggested.").setRequired(true), new OptionData(OptionType.STRING, "radio-country", "Specifies the Country of the Radio that will be suggested.").setRequired(true), new OptionData(OptionType.BOOLEAN, "anonymous-suggestion", "Specifies if the Suggestion is anonymous.").setRequired(true), new OptionData(OptionType.STRING, "radio-mp3-url", "Specifies the Url of the Radio that will be suggested.").setRequired(false))));

            commands.addCommands(new CommandData("debug", "Lets you see some Information."));
            commands.addCommands(new CommandData("help", "Lets you see the Help Message."));


            commands.queue();

            CommandClient client = new CommandClientBuilder().setOwnerId("853251098189627442").build();
            client.addSlashCommand(radioCommand);
            client.addSlashCommand(changeCommand);
            client.addSlashCommand(joinCommand);
            client.addSlashCommand(customCommand);
            client.addSlashCommand(volumeCommand);
            client.addSlashCommand(privacyCommand);
            client.addSlashCommand(suggestCommand);
            client.addSlashCommand(debugCommand);
            client.addSlashCommand(helpCommand);

            jda.addEventListener(client);
            jda.addEventListener(new Event());
            jda.addEventListener(getInstance());


            int guildSplit = Math.round(jda.getGuilds().size() / threads);
            LinkedList<Guild> guildss = new LinkedList<>(jda.getGuilds());

            System.out.println("T " + threads + " gsplit " + guildSplit + " gss " + guildss.size());
            System.out.println(Partition.ofSize(jda.getGuilds(), guildSplit).get(0));
            System.out.println(Partition.ofSize(jda.getGuilds(), guildSplit).get(1));
            System.out.println();
            for (int i = 0; i < threads; i++) {
                System.out.println(i);
                new BotThread(jda, Partition.ofSize(jda.getGuilds(), guildSplit).get(i), i);
            }
            //    for (int i = 0; i < threads; i++) {
          /*      List<Guild> guilds = new LinkedList<>();
                for (int a = 0; a < guildSplit; a++) {
                    guilds.add(guildss.get(a));
                }*/
          /*      guilds.forEach(element -> {
                    guildss.remove(element);
                });*/
            //     }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ArrayList<Guild> guilds1 = new ArrayList<>();
                    for (ServerInstance serverInstance : instances) {
                        guilds1.add(serverInstance.getGuild());
                    }
                    for (Guild guild : jda.getGuilds()) {
                        if (!guilds1.contains(guild)) {
                            instances.add(new ServerInstance(jda, guild, 0));
                        }
                    }
                }
            }, 1000 * 10);
            Scanner scanner = new Scanner(System.in);
/*            System.out.println("\nStarting Crashhandler");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    for (ServerInstance serverInstance : instances) {
                        if (serverInstance.getPlayer().getPlayingTrack() == null) {
                            System.out.println("CrashHandler: Restarting for " + serverInstance.getGuild().getName() + " - " + serverInstance.getGuild().getId());
                            serverInstance.playRadio();
                        }
                    }

                }
            }, 1000 * 5, 1000 * 10);*/
            try {
                handleCommand(scanner);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void handleCommand(Scanner scanner) {
        String command = scanner.nextLine();


        if (command.startsWith("broadcast")) {
  /*          if (command.contains(" ") && command.split(" ").length > 1) {
                String arg = command.replaceAll("broadcast ","");
                arg = arg.replaceAll("##","\n");
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GREEN);
                builder.setTitle("Official Discord Server");
                builder.setDescription("You can now Join the Official FradioFM Discord Server here\nhttps://discord.gg/euFcj2a9BD\n\n**For Support, Bugreports or some good music!**");
                builder.setFooter("Made in Germany. By Furkan.#4554");
                broadcastMessage(builder);
            }*/
        } else if (command.startsWith("list")) {

        /*   for(ServerInstance instance : instances) {

                   try {

                       System.out.println(instance.getGuild().getName());
                       EmbedBuilder builder = new EmbedBuilder();
                       builder.setTitle("**FradioFM Youtube Update**");
                     builder.setColor(Color.BLUE);
                       instance.getWritableChannel().sendMessage(builder.build()).queue();
                       System.out.println("Sent to " + instance.getGuild().getName());

                   } catch (Exception e) {

                       System.out.println("Failed to send message alert!");
                   }

           }*/
//       builder.setDescription("**We now Support Youtube Livestreams!**\nYes you read right you can now use **/Custom YOUTUBE_URL** to play a Livestream.\nThis only works with Youtube Livestreams and not with Videos!\n\n**Since this feature is still in Beta beware that sometimes something can crash!**\n Habt weiterhin Spaﬂ mit FradioFM / Have fun with FradioFM");

            StringBuilder builder = new StringBuilder();
            AtomicInteger listeners = new AtomicInteger();
            for (ServerInstance serverInstance : instances) {
                System.out.println(serverInstance.getGuild().getAudioManager());
                listeners.addAndGet(serverInstance.getGuild().getAudioManager().getConnectedChannel().getMembers().size());
                listeners.getAndDecrement();
                String isPlaying = "NO";
                String isConnectedVC = "NO";
                if (serverInstance.getPlayer().getPlayingTrack() != null) {
                    isPlaying = "YES";
                }
                if (serverInstance.getGuild().getAudioManager().isConnected()) {
                    isConnectedVC = "YES";
                }
                String a = "NULL";
                try {
                    a = serverInstance.getPlayer().getPlayingTrack().getInfo().title + " - " + serverInstance.getPlayer().getPlayingTrack().getInfo().author;
                } catch (Exception e) {

                }
                builder.append(serverInstance.getGuild().getName() + " - " + serverInstance.getGuild().getId() + "\nIsPlaying " + isPlaying + "\nIsConnectedVC " + isConnectedVC + " \n" + a + "\n\n");
            }
            ArrayList<String> list = new ArrayList<>();
            ArrayList<Guild> guilds = new ArrayList<>();
            for (ServerInstance serverInstance : instances) {
                guilds.add(serverInstance.getGuild());
            }
            for (Guild guild : jda.getGuilds()) {
                if (!guilds.contains(guild)) {
                    list.add(guild.getName() + " - " + guild.getId());
                }
            }
            System.out.println(builder + "\n\nI am in " + jda.getGuilds().size() + " servers\nwith " + listeners.get() + " listeners and " + instances.size() + " instances\nThere are " + list.size() + " Servers with no instance: " + list);

        } else {
            boolean b = command.contains(" ") && command.split(" ").length > 1;
            if (command.startsWith("restart")) {
                if (b) {
                    ServerInstance pickInstance = null;
                    for (ServerInstance serverInstance : instances) {
                        if (serverInstance.getGuild().getId().equals(command.split(" ")[1])) {
                            pickInstance = serverInstance;
                            break;
                        }
                    }
                    if (pickInstance != null) {
                        System.out.println("Restarting " + pickInstance.getGuild().getName() + " - " + pickInstance.getGuild().getId());
                        pickInstance.playRadio();
                        System.out.println("Restarted " + pickInstance.getGuild().getName() + " - " + pickInstance.getGuild().getId());
                        ServerInstance finalPickInstance = pickInstance;
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                String isPlaying = "NO";
                                String isConnectedVC = "NO";
                                if (finalPickInstance.getPlayer().getPlayingTrack() != null) {
                                    isPlaying = "YES";
                                }
                                if (finalPickInstance.getGuild().getAudioManager().isConnected()) {
                                    isConnectedVC = "YES";
                                }
                                System.out.println(finalPickInstance.getGuild().getName() + " - " + finalPickInstance.getGuild().getId() + ": IsPlaying " + isPlaying + " - IsConnectedVC " + isConnectedVC + " \n");
                            }
                        }, 1000);
                    } else {
                        System.out.println("\nPick_instance is null");
                    }
                }
            } else if (command.startsWith("delete")) {
                if (b) {
                    ServerInstance pickInstance = null;
                    for (ServerInstance serverInstance : instances) {
                        if (serverInstance.getGuild().getId().equals(command.split(" ")[1])) {
                            pickInstance = serverInstance;
                            break;
                        }
                    }
                    if (pickInstance != null) {
                        System.out.println("Deleted " + pickInstance.getGuild().getName() + " - " + pickInstance.getGuild().getId());
                        pickInstance.delete();
                        pickInstance.getGuild().leave().queue();
                    } else {
                        System.out.println("\nPick_instance is null");
                    }
                }
            }
        }
        handleCommand(scanner);
    }

    private static void broadcastMessage(EmbedBuilder message) {
        for (ServerInstance serverInstance : instances) {
            //    serverInstance.getGuild().getTextChannelById(serverInstance.getValue("wchannel")).sendMessage(message.build()).queue();
            serverInstance.getGuild().getTextChannelById(serverInstance.getValue("wchannel")).sendMessage(message.build()).queue();
        }
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


    public boolean isTestingMode() {
        return testingMode;
    }

    public RadioUtil getRadioUtil() {
        return radioUtil;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {

        if (testingMode && event instanceof GuildJoinEvent) {
            if (((GuildJoinEvent) event).getGuild().getId().equals("904370695075667988") || ((GuildJoinEvent) event).getGuild().getId().equals("918363724010647585")) {
                System.out.println("Creating new TEST Thread for " + ((GuildJoinEvent) event).getGuild().getName());
                ServerInstance instance = new ServerInstance(jda, ((GuildJoinEvent) event).getGuild(), 0);
                instances.add(instance);
            }
        } else if (!testingMode && event instanceof GuildJoinEvent && !((GuildJoinEvent) event).getGuild().getId().equals("904370695075667988") && !((GuildJoinEvent) event).getGuild().getId().equals("918363724010647585")) {
            System.out.println("Creating new Thread for " + ((GuildJoinEvent) event).getGuild().getName());
            ServerInstance instance = new ServerInstance(jda, ((GuildJoinEvent) event).getGuild(), 0);
            instances.add(instance);
        }
    }
}
