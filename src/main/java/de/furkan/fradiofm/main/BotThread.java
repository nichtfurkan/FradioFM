package de.furkan.fradiofm.main;

import de.furkan.fradiofm.instance.ServerInstance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class BotThread implements Runnable {

    JDA jda;
    Thread thread;
    List<Guild> guilds;
    int threadID;


    public BotThread(JDA jda, List<Guild> guilds, int threadID) {
        this.jda = jda;
        this.guilds = guilds;
        this.threadID = threadID;
        this.thread = new Thread(this, "BotThread");
        this.thread.start();
        this.thread.setName("BotThread-" + this.thread.getId());
        System.out.println("\nStarting Bot Thread " + this.thread.getId() + "  with " + guilds.size() + " Servers");
    }

    @Override
    public void run() {
        this.guilds.forEach(element -> {

            Main.instances.add(new ServerInstance(jda, element, threadID));
            //   System.out.println("Would create new instance for " + element.getName() + " with Bid " + this.thread.getId());
        });
    }
}
