package me.herobrinegoat.blocksetter;

import me.herobrinegoat.blocksetter.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockSetter extends JavaPlugin {

    private NMS nms;

    private final Queue<BlockTask> blockTasks = new ConcurrentLinkedQueue<>();
    private BlockTask current;
    int timerId;

    @Override
    public void onEnable() {
        try {
            nms = (NMS) Class.forName("me.herobrinegoat.blocksetter.nms." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]).newInstance();
        } catch (ClassNotFoundException e) {
            //Unsupported Version
            getLogger().info("Unsupported Version Detected: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
            getLogger().info("Try updating from spigot");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        timerId = Bukkit.getScheduler().runTaskTimer(this, () -> {
            BlockTask blockTask = blockTasks.peek();
            if (blockTask == null || blockTask.equals(current)) return;
            this.current = blockTask;
            blockTask.setBlocks();
        }, 20, 20).getTaskId();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(timerId);
    }

    public NMS getNms() {
        return nms;
    }

    public boolean isFirstInQueue(@Nonnull BlockTask blockTask) {
        return blockTask.equals(blockTasks.peek());
    }

    public void addBlockTask(@Nonnull BlockTask blockTask) {
        this.blockTasks.add(blockTask);
    }

    public void removeBlockTask() {
        this.current = null;
        this.blockTasks.remove();
    }
}
