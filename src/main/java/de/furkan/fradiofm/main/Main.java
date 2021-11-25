package de.furkan.fradiofm.main;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import de.furkan.fradiofm.commands.Change;
import de.furkan.fradiofm.commands.Custom;
import de.furkan.fradiofm.commands.Join;
import de.furkan.fradiofm.commands.ListRadio;
import de.furkan.fradiofm.instance.ServerInstance;
import de.furkan.fradiofm.util.RadioUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Main implements EventListener {

    private static final ArrayList<ServerInstance> instances = new ArrayList<>();
    private static final ListRadio radioCommand = new ListRadio();
    private static final Join joinCommand = new Join();
    private static final Change changeCommand = new Change();
    private static final Custom customCommand = new Custom();
    private static JDA jda;
    private static Main instance;
    private static boolean testingMode = false;
    private final RadioUtil radioUtil = new RadioUtil();

    public static Join getJoinCommand() {
        return joinCommand;
    }

    public static Custom getCustomCommand() {
        return customCommand;
    }

    public static Main getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        if (args.length != 0 && args[0].equals("testing")) {
            testingMode = true;
        }
        instance = new Main();
        try {
            jda = JDABuilder.createDefault("OTExNzk2MTY4MTYwMzE3NTEx.YZmmWg.CfFuzhCf_pb5wZclgvB7ZbaddTw").build().awaitReady();
            AtomicInteger status = new AtomicInteger(0);
            ArrayList<OnlineStatus> statuses = new ArrayList<>();
            statuses.add(OnlineStatus.ONLINE);
            statuses.add(OnlineStatus.DO_NOT_DISTURB);
            statuses.add(OnlineStatus.IDLE);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    jda.getPresence().setPresence(statuses.get(status.get()), Activity.listening(getInstance().getRadioUtil().getRadioCount()+" Radios"), false);
                    if(status.get() == 2) {
                        status.set(0);
                    } else {
                        status.addAndGet(1);
                    }
                }
            },1000*5,1000*10);
            jda.addEventListener(new Event());

            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands(new CommandData("join", "Lets the Bot join the first Voice Channel where it has Permission to."));
            commands.addCommands(new CommandData("change", "Lets you change the Radio Channel.").addOption(OptionType.INTEGER, "radio-id", "Specifies the Radio ID that will be played.", true));
            commands.addCommands(new CommandData("custom", "Lets you play your own Radio Channel.").addOption(OptionType.STRING, "radio-mp3-url", "Specifies the Radio URL that the bot will listen to.", true));
            commands.addCommands(new CommandData("radios", "Lists all the Radio Channels currently available."));
            commands.queue();

            CommandClient client = new CommandClientBuilder().setOwnerId("853251098189627442").build();
            client.addSlashCommand(radioCommand);
            client.addSlashCommand(changeCommand);
            client.addSlashCommand(joinCommand);
            client.addSlashCommand(customCommand);
            jda.addEventListener(client);
            jda.addEventListener(new Event());
            jda.addEventListener(getInstance());


            jda.getGuilds().forEach(guild -> {
                if (testingMode) {
                    if (guild.getId().equals("904370695075667988")) {
                        ServerInstance instance = new ServerInstance(jda, guild);
                    }
                } else if (!guild.getId().equals("904370695075667988")) {
                    {
                        ServerInstance instance = new ServerInstance(jda, guild);
                    }
                }
            });
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nWaiting for Commands");
            handleCommand(scanner);
        } catch (LoginException | InterruptedException e) {
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
            System.out.println("I am in " + instances.size() + " servers");
        }
        handleCommand(scanner);
    }

    private static void broadcastMessage(EmbedBuilder message) {
        for (ServerInstance serverInstance : instances) {
            serverInstance.getGuild().getTextChannelById(serverInstance.getValue("wchannel")).sendMessage(message.build()).queue();
        }
    }

    private static void broadcastMessage(String message) {
        for (ServerInstance serverInstance : instances) {
            serverInstance.getGuild().getTextChannelById(serverInstance.getValue("wchannel")).sendMessage(message).queue();
        }
    }

    public ArrayList<ServerInstance> getInstances() {
        return instances;
    }

    public boolean isTestingMode() {
        return testingMode;
    }

    public Change getChangeCommand() {
        return changeCommand;
    }

    public ServerInstance getInstanceByGuild(Guild guild) {
        for (ServerInstance serverInstance : instances) {
            if (serverInstance.getGuild().getId().equals(guild.getId())) {
                return serverInstance;
            }
        }
        return null;
    }

    public RadioUtil getRadioUtil() {
        return radioUtil;
    }

    public ListRadio getRadioCommand() {
        return radioCommand;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildJoinEvent && !testingMode) {
            System.out.println("Creating new Thread for " + ((GuildJoinEvent) event).getGuild().getName());
            ServerInstance instance = new ServerInstance(jda, ((GuildJoinEvent) event).getGuild());
        }
    }
}
