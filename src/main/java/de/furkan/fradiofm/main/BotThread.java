package de.furkan.fradiofm.main;

import de.furkan.fradiofm.instance.ServerInstance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

public class BotThread implements Runnable {

    private final JDA jda;
    private final Thread thread;
    private final List<Guild> guilds;
    private final int threadID;

    ArrayList<ServerInstance> arrayList = new ArrayList<>();

    public BotThread(JDA jda, List<Guild> guilds, int threadID) {
        this.jda = jda;
        this.guilds = guilds;
        this.threadID = threadID;
        this.thread = new Thread(this, "BotThread");
        this.thread.start();
        this.thread.setName("BotThread-" + this.thread.getId());
        System.out.println("Starting Bot Thread " + this.thread.getId() + "  with " + guilds.size() + " Servers");
    }

    @Override
    public void run() {
        this.guilds.forEach(element -> {
            ServerInstance serverInstance = new ServerInstance(jda, element, threadID);
            arrayList.add(serverInstance);
            Main.instances.add(serverInstance);
        });
    }
}
