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
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import redis.clients.jedis.Jedis;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ServerInstance {

    private final JDA jda;
    private final AudioPlayerManager manager;
    private final AudioPlayer player;
    private Guild guild;
    private Jedis jedis;
    private TextChannel writableChannel;
    private VoiceChannel lastChannel;
    private String radioUrl;
    int thread;



    public ServerInstance(JDA jda, Guild guild, int thread) {
        this.jda = jda;
        this.guild = guild;
        this.thread = thread;

        this.manager = new DefaultAudioPlayerManager();
        this.manager.registerSourceManager(new HttpAudioSourceManager());

        this.player = this.manager.createPlayer();

        this.player.addListener(new TrackScheduler(this));
        this.guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(this.player));

        this.startInstance();


    }

    public VoiceChannel getLastChannel() {
        return lastChannel;
    }

    public int getThread() {
        return thread;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TextChannel getWritableChannel() {
        return writableChannel;
    }

    public void setWritableChannel(TextChannel writableChannel) {
        this.writableChannel = writableChannel;
        this.setValue("wchannel", writableChannel.getId());
    }

    private boolean startInstance() {
        try {
            System.out.println("Trying to connect to redis database for " + guild.getName());
            jedis = new Jedis("localhost", 6379);
            this.jedis.connect();
            this.jedis.ping();
            System.out.println("Started Instance for " + guild.getName() + " - " + guild.getId() + " on thread " + thread);
            boolean isValid = this.getValue("wchannel") != null && this.getValue("radiourl") != null;
            if (!isValid) {
                this.sendSetupMessage();
            } else {
                this.writableChannel = this.guild.getTextChannelById(this.getValue("wchannel"));
                this.radioUrl = this.getValue("radiourl");

                if (this.getValue("lastchannel") != null) {
                    this.lastChannel = this.guild.getVoiceChannelById(this.getValue("lastchannel"));
                }

                if (this.radioUrl == null) {
                    this.radioUrl = "http://streams.bigfm.de/bigfm-deutschland-128-mp3";
                }
                this.setValue("name", this.guild.getName() + " - " + this.guild.getId());
                playRadio();
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        jedis.ping();
                    } catch (Exception e) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                startInstance();
                            }
                        }, 1000 * 10);
                    }
                }
            }, 1000 * 30, 1000 * 30);
            return true;
        } catch (Exception e) {
            System.out.println("An error has occurred: " + e.getMessage());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startInstance();
                }
            }, 1000 * 3);
            return false;
        }
    }


    public void setRadioUrl(String radioUrl) {
        this.radioUrl = radioUrl;
        this.setValue("radiourl", radioUrl);
    }

    public void delete() {
        this.deleteValue("mode");
        this.deleteValue("lastchannel");
        this.deleteValue("wchannel");
        this.deleteValue("radiourl");
        this.deleteValue("name");
        Main.getInstance().getInstances().remove(this);
        this.shutdown();
    }

    public void setLastChannel(VoiceChannel lastChannel) {
        this.lastChannel = lastChannel;
        this.setValue("lastchannel", lastChannel.getId());
    }

    public JDA getJda() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }


    public void shutdown() {
        this.player.stopTrack();
        this.jedis.close();
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
                + "\nYou can use **/Join** to let me Join a Voice Channel\nor **/Change ID** to change the Radio Channel.\nYou can use **/Radios** to get a list of all available Radios and their ID's\nor **/Custom URL** to play a Custom Radio.\n**/Suggest** to suggest a Radio that will be added to the Official List\n**/Privacy** to see our Privacy Policy\n**/Volume (1 - 1000)** to change the Volume.\n**/Search** to Search a Radio Station on the Internet.\n**/Help** to view the Help Message.\n\nYou can move the Bot in any other Voice Channel and it will stay there forever.\n\nIf you want to give other People the permissions to use me\nJust create a Role with the name `RadioAdmin`\nAnd i will listen to them.\n\n**For Support please join the Official Discord Server**\nhttps://discord.gg/4pwp72s62c");
        builder.setFooter("Made in Germany. By Furkan.#4554");
        boolean added = false;
        for (TextChannel channel : this.guild.getTextChannels()) {
            try {
                channel.sendMessageEmbeds(builder.build()).queue();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.BLUE);
                embedBuilder.setAuthor("Playing Now!");
                embedBuilder.setTitle("BigFM - Deutschland");
                embedBuilder.setDescription("\nThis is playing by Default.\nUse **/Change ID** To change the Radio Channel!");
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
                channel.sendMessage("Hey, we are also offering a Coding Server where we Code Bots/Programs and also minecraft plugins that correspond to your wishes!\nIf you are interested you can join the Discord Server and open a order.\nhttps://discord.gg/dVPbDkwTDd").queue();
                added = true;
                System.out.println("Sent default message for " + guild.getName());
                setWritableChannel(channel);
                setRadioUrl(Main.getInstance().getRadioUtil().getRadioUrlById(0));

                String voice = this.joinLeastVoice();
                if (voice == null) {
                    channel.sendMessage("Can't join any Voice Channel. Do i have the right permissions?\nRetry with **/Join**").queue();
                } else {
                    channel.sendMessage("Joining Voice `" + voice + "`").queue();
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        playRadio();
                    }
                }, 1000 * 5);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (!added) {
            setRadioUrl(Main.getInstance().getRadioUtil().getRadioUrlById(0));
            this.joinLeastVoice();
            playRadio();
            return;
        }
        if (this.guild.getTextChannels().size() == 0) {
            setRadioUrl(Main.getInstance().getRadioUtil().getRadioUrlById(0));
            this.joinLeastVoice();
            playRadio();
            return;
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
                if (player.getPlayingTrack() != null) {
                    player.stopTrack();
                }
                player.playTrack(track.makeClone());
                player.setVolume(15);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.BLUE);
                embedBuilder.setAuthor("Playing Now!");
                embedBuilder.setTitle(radioUrl);
                String author = track.getInfo().author.length() == 0 ? "Unknown." : track.getInfo().author;
                embedBuilder.setDescription("Author: `" + author + "`\n\nUse **/Change ID** To change the Radio Channel!");
                writableChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                setRadioUrl(radioUrl);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                writableChannel.sendMessage("Cant play Radio with URL.\n**" + radioUrl + "**\nPlease make sure that the URL is pointing to a valid MP3 File").queue();
            }

            @Override
            public void noMatches() {
                writableChannel.sendMessage("Cant play Radio with URL.\n**" + radioUrl + "**\nPlease make sure that the URL is pointing to a valid MP3 File").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
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

        this.manager.loadItem(radioUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (!track.getInfo().isStream) {
                    return;
                }
                System.out.println("Playing radio for " + guild.getName() + " " + radioUrl);
                player.playTrack(track.makeClone());
                player.setVolume(15);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("Playing failed for " + guild.getName() + " " + exception.getMessage());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        playRadio();
                    }
                }, 1000 * 10);
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
        for(StageChannel channel : this.guild.getStageChannels()) {
            try {
                if(channel.getStageInstance().getSpeakers().size() == 0) {
                    this.guild.getAudioManager().openAudioConnection(channel);
                    channel.getStageInstance().requestToSpeak().queue();
                    setLastChannel(channel);
                    return channel.getName();
                }
            } catch (Exception e) {

            }
        }

        for (VoiceChannel voiceChannel : this.guild.getVoiceChannels()) {
            try {
                this.guild.getAudioManager().openAudioConnection(voiceChannel);
                setLastChannel(voiceChannel);
                return voiceChannel.getName();
            } catch (Exception e) {

            }
        }
        return null;
    }


}
