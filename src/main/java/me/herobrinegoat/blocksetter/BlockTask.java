package me.herobrinegoat.blocksetter;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockTask implements Cloneable {

    private final BlockSetter plugin;
    public final World world;
    private final int startX;
    private final int startY;
    private final int startZ;
    private final int endX;
    private final int endY;
    private final int endZ;
    private Material material;
    private final int blocksPerSection;
    private final int tickSpeed;
    private Completable sectionCompletable;
    private Completable finalCompletable;
    private Changeable changeable;

    /**
     *
     * @param world
     * @param startX
     * @param startY
     * @param startZ
     * @param endX
     * @param endY
     * @param endZ
     * @param material - Material to set blocks
     * @param blocksPerSection - How many blocks are set every cycle
     * @param tickSpeed - How often blocks are set, in ticks
     */

    public BlockTask(World world, int startX, int startY, int startZ, int endX, int endY, int endZ, Material material, int blocksPerSection, int tickSpeed) {
        this.plugin = BlockSetter.getPlugin(BlockSetter.class);
        this.world = world;
        this.startX = startX;
        this.startY = Math.max(0, startY);
        this.startZ = startZ;
        this.endX = endX;
        this.endY = Math.min(endY, 255);
        this.endZ = endZ;
        this.tickSpeed = tickSpeed;
        this.blocksPerSection = (int) Math.sqrt(blocksPerSection);
        this.material = null;
    }

    public BlockTask(World world, int startX, int startY, int startZ, int endX, int endY, int endZ, int blocksPerSection, int tickSpeed) {
        this(world, startX, startY, startZ, endX, endY, endZ, null, blocksPerSection, tickSpeed);
    }

    public BlockTask(World world, int startX, int startY, int startZ, int endX, int endY, int endZ, Material material, int blocksPerSection, int tickSpeed, Completable sectionCompletable, Completable finalCompletable) {
        this(world, startX, startY, startZ, endX, endY, endZ, material, blocksPerSection, tickSpeed);
        this.sectionCompletable = sectionCompletable;
        this.finalCompletable = finalCompletable;
    }

    public BlockTask(Location start, Location end, int blocksPerSection, int tickSpeed, Completable sectionCompletable, Completable finalCompletable) {
        this(start, end, null, blocksPerSection, tickSpeed, sectionCompletable, finalCompletable);
    }

    public BlockTask(Location start, Location end, Material material, int blocksPerSection, int tickSpeed, Completable sectionCompletable, Completable finalCompletable) {
        this(start, end, material, blocksPerSection, tickSpeed);
        this.sectionCompletable = sectionCompletable;
        this.finalCompletable = finalCompletable;
    }

    public BlockTask(Location start, Location end, int blocksPerSection, int tickSpeed) {
        this(start, end, null, blocksPerSection, tickSpeed);
    }

    public BlockTask(Location start, Location end, Material material, int blocksPerSection, int tickSpeed) {
        Validate.isTrue(start.getWorld() != null && start.getWorld().equals(end.getWorld()), "The two world locations must be non-null and the same!");
        this.plugin = BlockSetter.getPlugin(BlockSetter.class);
        this.world = start.getWorld();
        this.startX = start.getBlockX();
        this.startY = Math.max(0, start.getBlockY());
        this.startZ = start.getBlockZ();
        this.endX = end.getBlockX();
        this.endY = Math.min(end.getBlockY(), 255);
        this.endZ = end.getBlockZ();
        this.material = material;
        this.blocksPerSection = blocksPerSection;
        this.tickSpeed = tickSpeed;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setSectionCompletable(Completable sectionCompletable) {
        this.sectionCompletable = sectionCompletable;
    }

    public void setFinalCompletable(Completable finalCompletable) {
        this.finalCompletable = finalCompletable;
    }

    public void setChangeable(Changeable changeable) {
        this.changeable = changeable;
    }

    private boolean isRunning;

    public void setBlocks() {
        if (isRunning) return;
        plugin.addBlockTask(this);
        if (!plugin.isFirstInQueue(this)) return;
        isRunning = true;
        setBlocks(startX, startY, startZ, startX + blocksPerSection, startZ + blocksPerSection, 0, 0);
    }

    //Example
    //World world = Bukkit.getWorld("World");
    //BlockTask blockTask = new BlockTask(world, -200, 0, -200, 200, 255, 200, Material.AIR, 100_000, 1);
    //blockTask.setSectionCompletable((result) -> getLogger().info("Section Completed1"));
    //blockTask.setFinalCompletable((result) -> getLogger().info("Final Completed1"));
    //blockTask.setBlocks();


    //Works in a grid coordinate grid pattern
    //Starts in lowest section of grid, and works its way up
    //If a grid is completed, it moves up a y level
    //Example: Coordinate grid from -500, -500 to 500, 500
    //If blocksPerSection is set to 10_000, it will start at
    //-500, -500, and go up to -400, -400 for the first section
    //Then it goes to -300, -400 for the second section, and so on
    //It moves up the x axis first, then it goes up the z axis
    private void setBlocks(int x, int y, int z, int xMax, int zMax, int taskNum, int totalBlocks) {
        if (xMax > endX) xMax = endX;
        if (zMax > endZ) zMax = endZ;
        int savedZ = z;
        for (; x < xMax; x++) {
            for (; z < zMax; z++) {
                if (changeable != null) changeable.change(world.getBlockAt(x, y, z));
                if (material == null) continue;
                plugin.getNms().setBlockFast(world, x, y, z, material, (byte) 0, false);
                totalBlocks++;
            }
            z = savedZ;
        }

        BlockTaskResult result = new BlockTaskResult(x, y, z, taskNum, totalBlocks);

        if (xMax < endX) {
            xMax += blocksPerSection;
        } else if (zMax < endZ) {
            x = startX;
            xMax = startX + blocksPerSection;
            zMax += blocksPerSection;
            z+=blocksPerSection;
        } else if (y < endY) {
            x = startX;
            z = startZ;
            xMax = startX + blocksPerSection;
            zMax = startZ + blocksPerSection;
            y++;
        } else {
            plugin.removeBlockTask();
            if (finalCompletable != null) finalCompletable.onComplete(result);
            return;
        }
        int finalX = x;
        int finalZ = z;
        int finalY = y;
        int finalXMax = xMax;
        int finalZMax = zMax;
        int finalTotalBlocks = totalBlocks;
        if (sectionCompletable != null) sectionCompletable.onComplete(result);
        Bukkit.getScheduler().runTaskLater(plugin, () -> setBlocks(finalX, finalY, finalZ, finalXMax, finalZMax, taskNum +1, finalTotalBlocks), tickSpeed);
    }

    public static class BlockTaskResult {

        public final int endX;
        public final int endY;
        public final int endZ;
        public final int taskNum;
        public final int totalBlocks;

        private BlockTaskResult(int endX, int endY, int endZ, int taskNum, int totalBlocks) {
            this.endX = endX;
            this.endY = endY;
            this.endZ = endZ;
            this.taskNum = taskNum;
            this.totalBlocks = totalBlocks;
        }
    }

    @FunctionalInterface
    public interface Completable {

        void onComplete(BlockTaskResult blockTaskResult);

    }

    @FunctionalInterface
    public interface Changeable {

        void change(Block block);
    }
}
