 private final BlockSetter plugin;
    public final World world;
    private final int startX;
    private final int startY;
    private final int startZ;
    private final int endX;
    private final int endY;
    private final int endZ;
    private Material material;
    private int chunksPer;
    private final int tickSpeed;
    private Completable sectionCompletable;
    private Completable finalCompletable;
    private Changeable changeable;

    private int x;
    private int y;
    private int z;

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
     * @param chunksPer - How many blocks are set every cycle
     * @param tickSpeed - How often blocks are set, in ticks
     */

    public BlockTask(World world, int startX, int startY, int startZ, int endX, int endY, int endZ, Material material, int chunksPer, int tickSpeed) {
        this.plugin = BlockSetter.getPlugin(BlockSetter.class);
        this.world = world;
        this.startX = startX;
        this.startY = Math.max(0, startY);
        this.startZ = startZ;
        this.endX = endX;
        this.endY = Math.min(endY, 255);
        this.endZ = endZ;
        this.tickSpeed = tickSpeed;
        this.chunksPer = chunksPer;
        this.material = material;
    }

    public BlockTask(World world, int startX, int startY, int startZ, int endX, int endY, int endZ, int chunksPer, int tickSpeed) {
        this(world, startX, startY, startZ, endX, endY, endZ, null, chunksPer, tickSpeed);
    }

    public BlockTask(World world, int startX, int startY, int startZ, int endX, int endY, int endZ, Material material, int chunksPer, int tickSpeed, Completable sectionCompletable, Completable finalCompletable) {
        this(world, startX, startY, startZ, endX, endY, endZ, material, chunksPer, tickSpeed);
        this.sectionCompletable = sectionCompletable;
        this.finalCompletable = finalCompletable;
    }

    public BlockTask(Location start, Location end, int chunksPer, int tickSpeed, Completable sectionCompletable, Completable finalCompletable) {
        this(start, end, null, chunksPer, tickSpeed, sectionCompletable, finalCompletable);
    }

    public BlockTask(Location start, Location end, Material material, int chunksPer, int tickSpeed, Completable sectionCompletable, Completable finalCompletable) {
        this(start, end, material, chunksPer, tickSpeed);
        this.sectionCompletable = sectionCompletable;
        this.finalCompletable = finalCompletable;
    }

    public BlockTask(Location start, Location end, int chunksPer, int tickSpeed) {
        this(start, end, null, chunksPer, tickSpeed);
    }

    public BlockTask(Location start, Location end, Material material, int chunksPer, int tickSpeed) {
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
        this.chunksPer = chunksPer;
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

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getStartZ() {
        return startZ;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public int getEndZ() {
        return endZ;
    }

    public Material getMaterial() {
        return material;
    }

    public int getChunksPer() {
        return chunksPer;
    }

    public int getTickSpeed() {
        return tickSpeed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getChunkStartX() {
        return chunkStartX;
    }

    public int getChunkStartZ() {
        return chunkStartZ;
    }

    private Runnable pluginDisableRunnable;

    public void setPluginDisableRunnable(Runnable pluginDisableRunnable) {
        this.pluginDisableRunnable = pluginDisableRunnable;
    }

    public void onPluginDisable() {
        if (pluginDisableRunnable != null) pluginDisableRunnable.run();
    }

    private boolean isRunning;

    int chunkStartX;
    int chunkStartZ;


    private void setBlocks(int x, int y, int z, int xMax, int zMax, int chunksPer, int taskNum, int totalBlocks) {
        if (xMax > endX) xMax = endX;
        if (zMax > endZ) zMax = endZ;
        int savedZ = z;
        int savedX = x;

        int chunkAmount = chunksPer * 16;

        System.out.println(x + " " + y + " " + z);
        System.out.println(xMax + " " + zMax);
        NMS nms = plugin.getNms();
        for (; y <= endY; y++) {
            for (; x <= xMax; x++) {
                if (x < startX) continue;
                for (; z <= zMax; z++) {
                    if (z < startZ) continue;
                    if (changeable != null) changeable.change(world.getBlockAt(x, y, z));
                    if (material == null) continue;
                    nms.setBlockFast(world, x, y, z, material, (byte) 0, false);
                    totalBlocks++;
                }
                z = savedZ;
            }
            x = savedX;
        }
        x = savedX;
        z = savedZ;
        BlockTaskResult result = new BlockTaskResult(x, y, z, taskNum, totalBlocks);

        if (xMax < endX) {
            xMax += chunkAmount;
            x += chunkAmount;
        } else if (zMax < endZ) {
            x = chunkStartX;
            xMax = chunkStartX + chunkAmount;
            zMax += chunkAmount;
            z+= chunkAmount;
        } else {
            plugin.removeBlockTask();
            if (finalCompletable != null) finalCompletable.onComplete(result);
            return;
        }
        int finalX = x;
        int finalY = startY;
        int finalZ = z;
        int finalXMax = xMax;
        int finalZMax = zMax;
        int finalTotalBlocks = totalBlocks;
        if (sectionCompletable != null) sectionCompletable.onComplete(result);
        Bukkit.getScheduler().runTaskLater(plugin, () -> setBlocks(finalX, startY, finalZ, finalXMax, finalZMax, chunksPer, taskNum +1, finalTotalBlocks), tickSpeed);
    }

    public void setBlocks() {
        if (isRunning) return;
        plugin.addBlockTask(this);
        if (!plugin.isFirstInQueue(this)) return;
        isRunning = true;
        int x = startX >> 4 << 4;
        int z = startZ >> 4 << 4;
        this.chunkStartX = x;
        this.chunkStartZ = z;
        setBlocks(x, 0, z, x + 15, z + 15, chunksPer, 0, 0);
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

    @FunctionalInterface
    public interface Runnable {

        void run();

    }
