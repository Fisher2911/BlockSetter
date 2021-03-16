# BlockSetter

You can use this to edit blocks in your world, as of now it only works in 1.16.4.
It will at most set one y layer at a time
For example, if there was a 100 x 100 * 256 layer you wanted cleared,
And you specified to clear 100,000 blocks at a time, it would only clear
10,000 blocks at a time because 100 * 100 = 10,000

You can create multiple BlockTasks,
They will be queued so they will not
run at the same time

Example
World world = Bukkit.getWorld("World");
BlockTask blockTask = new BlockTask(world, -200, 0, -200, 200, 255, 200, Material.AIR, 100_000, 1);
//This will happen when a section of a blocktask is completed
blockTask.setSectionCompletable(result -> getLogger().info("Section Completed1"));
//This will happen every time a whole blocktask is completed
blockTask.setFinalCompletable(result -> getLogger().info("Final Completed1"));
//This will happen to every block
blockTask.setSetChangeable(block -> block.setType(Material.AIR));
//This starts the BlockTask
blockTask.setBlocks();


Works in a grid coordinate grid pattern
Starts in lowest section of grid, and works its way up
If a grid is completed, it moves up a y level
Example: Coordinate grid from -500, -500 to 500, 500
If blocksPerSection is set to 10_000, it will start at
-500, -500, and go up to -400, -400 for the first section
Then it goes to -300, -400 for the second section, and so on
It moves up the x axis first, then it goes up the z axis
