package de.furkan.fradiofm.instance;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.furkan.fradiofm.main.Main;
import de.furkan.fradiofm.util.AudioPlayerSendHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import redis.clients.jedis.Jedis;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class ServerInstance {

    private final JDA jda;
    private final AudioPlayerManager manager;
    private final AudioPlayer player;
    private Guild guild;
    private Jedis jedis;
    private Mode mode;
    private TextChannel writableChannel;
    private VoiceChannel lastChannel;
    private String radioUrl;

    public ServerInstance(JDA jda, Guild guild) {
        this.jda = jda;
        this.guild = guild;

        Main.getInstance().getInstances().add(this);

        this.manager = new DefaultAudioPlayerManager();
        this.manager.registerSourceManager(new HttpAudioSourceManager());

        this.player = this.manager.createPlayer();

        this.player.addListener(new TrackScheduler(this));
        this.guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(this.player));

        this.connectJedis();

        if (this.getValue("mode") == null || this.getValue("wchannel") == null || this.getValue("lastchannel") == null || this.getValue("radiourl") == null) {
            this.setMode(Mode.SETUP_MODE);
            this.sendSetupMessage();
        } else {

            if (this.guild.getName().equals("Scamscape")) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Calendar calendar = Calendar.getInstance();
                        Date date = calendar.getTime();
                        String name = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
                        if (name.equals("Monday") || name.equals("Tuesday") || name.equals("Wednesday") || name.equals("Thursday") || name.equals("Friday")) {
                            if (LocalDateTime.now().getHour() == 0 && LocalDateTime.now().getMinute() == 0 && LocalDateTime.now().getSecond() == 0) {
                                writableChannel.sendMessage("<@853251098189627442> Nightlounge beginnt jetzt!").queue();
                            }
                        }
                    }
                }, 1000, 1000);
            }

            this.mode = Mode.valueOf(this.getValue("mode"));
            this.writableChannel = this.guild.getTextChannelById(this.getValue("wchannel"));
            this.lastChannel = this.guild.getVoiceChannelById(this.getValue("lastchannel"));
            this.radioUrl = this.getValue("radiourl");
            this.setValue("name",this.guild.getName() + " - " + this.guild.getId());
            playRadio();
        }
    }

    private boolean connectJedis() {
        try {
            jedis = new Jedis("localhost", 6372);
            if(Main.getInstance().isTestingMode()) {
                System.out.println("Connecting Redis Test");
                jedis = new Jedis("localhost", 6366);
            }
            this.jedis.connect();
            this.jedis.ping();
            if (getMode() == Mode.WAITING_MODE) {
                this.writableChannel.sendMessage("**Connection to Database reestablished!** Commands are now working again!\nThanks for your patience!").queue();
                setMode(Mode.READY_MODE);
            }
            System.out.println("Connected Redis for " + guild.getName());

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getMode() != Mode.WAITING_MODE) {
                        try {
                            jedis.ping();
                        } catch (Exception e) {
                            setMode(Mode.WAITING_MODE);
                            noConnection();
                        }
                    }
                }
            }, 1000 * 5, 1000 * 5);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Reconnecting...");
                    connectJedis();
                }
            }, 1000 * 10);
            return false;
        }
    }

    private void noConnection() {
        if (this.writableChannel != null && getMode() != Mode.READY_MODE) {
            this.writableChannel.sendMessage("**Connection to Database lost!** Commands are temporarily unavailable!\nPlease wait while we are fixing this issue...\n").queue();
            connectJedis();
        }
    }

    public void setRadioUrl(String radioUrl) {
        this.radioUrl = radioUrl;
        this.setValue("radiourl", radioUrl);
    }

    public void delete() {
        if (getMode() == Mode.READY_MODE) {
            System.out.println("Deleted for " + guild.getName());
            this.deleteValue("mode");
            this.deleteValue("lastchannel");
            this.deleteValue("wchannel");
            this.deleteValue("radiourl");
            Main.getInstance().getInstances().remove(this);
            this.shutdown();
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getMode() == Mode.READY_MODE) {
                        cancel();
                        System.out.println("Deleted for " + guild.getName());
                        deleteValue("mode");
                        deleteValue("lastchannel");
                        deleteValue("wchannel");
                        deleteValue("radiourl");
                        guild = null;
                        shutdown();
                    }
                }
            }, 1000 * 10, 1000 * 10);
        }
    }

    public void setLastChannel(VoiceChannel lastChannel) {
        this.lastChannel = lastChannel;
        this.setValue("lastchannel", lastChannel.getId());
    }

    public void setWritableChannel(TextChannel writableChannel) {
        this.writableChannel = writableChannel;
        this.setValue("wchannel", writableChannel.getId());
    }

    public JDA getJda() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        this.setValue("mode", mode.name());
    }

    public void shutdown() {
        this.player.stopTrack();
    }

    public String getValue(String value) {
        try {
            return this.jedis.get("servers/" + guild.getId() + "/" + value);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean setValue(String key, String value) {
        try {
            this.jedis.set("servers/" + guild.getId() + "/" + key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteValue(String key) {
        try {
            this.jedis.del("servers/" + guild.getId() + "/" + key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendSetupMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE);
        builder.setTitle("Welcome, " + this.guild.getName());
        builder.setDescription("**Hey**\nMy name is **FradioFM** but you can also call me **Fred**\nMy purpose is to play great Radio Channel's at your Discord Server.\n"
                + "\nYou can use **/Join** To let me Join a Voice Channel\nor **/Change ID** To change the Radio Channel.\nYou can use **/Radios** to get a list of all available Radios and their ID's\nor **/Custom URL** to play a Custom Radio.\n\nYou can move the Bot in any other Voice Channel and it will stay there forever.\n\nIf you want to give other People the permissions to use me\nJust create a Role with the name `RadioAdmin`\nAnd i will listen to them.\n\n**For Support please join the Official Discord Server**\nhttps://discord.gg/4pwp72s62c");
        builder.setFooter("Made in Germany. By Furkan.#4554");
        for (TextChannel channel : this.guild.getTextChannels()) {
            if (this.guild.getName().equals("Scamscape")) {
                channel = this.guild.getTextChannelById("887444364924690513");
            }

            try {
                channel.sendMessage(builder.build()).complete();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.BLUE);
                embedBuilder.setAuthor("Playing Now!");
                embedBuilder.setTitle("BigFM - Deutschland");
                embedBuilder.setDescription("\nThis is playing by Default.\nUse **/Change ID** To change the Radio Channel!");
                channel.sendMessage(embedBuilder.build()).complete();
                System.out.println("Send default message for " + guild.getName());
                setMode(Mode.READY_MODE);
                this.radioUrl = Main.getInstance().getRadioUtil().getRadioUrlById(0);
                this.writableChannel = channel;
                this.mode = Mode.valueOf(this.getValue("mode"));
                playRadio();
                this.setValue("wchannel", this.writableChannel.getId());
                this.setValue("radiourl", this.radioUrl);

                String voice = this.joinLeastVoice();
                if (voice == null) {
                    channel.sendMessage("Can't join any Voice Channel. Do i have the right permissions?\nRetry with **/Join**").queue();
                } else {
                    channel.sendMessage("Joining Voice `" + voice + "`").queue();
                }

                return;
            } catch (Exception e) {

            }
        }


    }

    public void playCustom(String radioUrl) {
        if (!this.guild.getAudioManager().isConnected()) {
            joinLeastVoice();
        }
        this.manager.registerSourceManager(new HttpAudioSourceManager());
        this.manager.loadItem(radioUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println("Track loaded for " + guild.getName());
                if (player.getPlayingTrack() != null) {
                    player.stopTrack();
                }
                player.playTrack(track.makeClone());
                player.setVolume(15);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.BLUE);
                embedBuilder.setAuthor("Playing Now!");
                embedBuilder.setTitle("Custom URL");
                embedBuilder.setDescription(radioUrl + "\nUse **/Change ID** To change the Radio Channel!");
                writableChannel.sendMessage(embedBuilder.build()).queue();
                setRadioUrl(radioUrl);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
                System.out.println("No matches for " + guild.getName());
                writableChannel.sendMessage("Cant play Radio with URL.\n**" + radioUrl + "**\nPlease make sure that the URL is pointing to a valid MP3 File").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
                System.out.println("Load failed for " + guild.getName());
                writableChannel.sendMessage("Cant play Radio with URL.\n**" + radioUrl + "**\nPlease make sure that the URL is pointing to a valid MP3 File").queue();
            }
        });
    }

    public void playRadio() {
        if (!this.guild.getAudioManager().isConnected()) {
            joinLeastVoice();
        }
        if (this.player.getPlayingTrack() != null) {
            this.player.stopTrack();
        }
        this.manager.registerSourceManager(new HttpAudioSourceManager());
        // System.out.println("\nLoading URL " + this.radioUrl + " for " + guild.getName());
        this.manager.loadItem(this.radioUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println("Track loaded for " + guild.getName());
                player.playTrack(track.makeClone());
                player.setVolume(15);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
                System.out.println("No matches for " + guild.getName());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("Load failed for " + guild.getName());
                if(exception.getMessage().equals("Connecting to the URL failed.")) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("Restarting Audio URL for " + getGuild().getName() + " " + getGuild().getId());
                            playRadio();
                        }
                    },1000*10);
                } else {
                    exception.printStackTrace();
                }
            }
        });
    }

    public String joinLeastVoice() {
        if (this.lastChannel != null) {
        try {
            this.guild.getAudioManager().openAudioConnection(this.lastChannel);
            return this.lastChannel.getName();
        } catch (Exception e) {

        }
        }
        for (VoiceChannel voiceChannel : this.guild.getVoiceChannels()) {
            try {
                this.guild.getAudioManager().openAudioConnection(voiceChannel);
                this.setValue("lastchannel", voiceChannel.getId());
                return voiceChannel.getName();
            } catch (Exception e) {

            }
        }

        return null;
    }

    private void changeMode(Mode mode) {
        this.mode = mode;
    }


}
