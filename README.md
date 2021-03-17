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

```java

World world = Bukkit.getWorld("World");

//This will create a blocktask that goes from -200, 0, -200 to 200, 255, 200, and sets blocks ever 1 tick.
BlockTask blockTask = new BlockTask(world, -200, 0, -200, 200, 255, 200, Material.AIR, 1);

//This will happen when a chunk is completed
blockTask.setSectionCompletable(result -> getLogger().info("Section Completed"));

//This will happen every time a whole blocktask is completed
blockTask.setFinalCompletable(result -> getLogger().info("Final Completed"));

//This will happen to every block
blockTask.setSetChangeable(block -> block.setType(Material.AIR));

// It is better to use blockTask.setMaterial(Material.Air) instead,
// because it uses nms to place the blocks

//This starts the BlockTask
blockTask.setBlocks();

```

It works in a grid coordinate grid pattern and
starts in lowest section of grid, and works its way up
Example: Coordinate grid from -500, -500 to 500, 500
It will find the chunk that -500, -500 occupies, and start from there.
It will cycle the chunk up to the y coordinate specified as the height limit.
Once that is done, it moves to the next chunk in the x direction, so if
for example the chunk started on -500, -500 exactly, after cycling
through to -484, -484, it would go to -468, -484.
Once it gets to 500, 500 it repeats, except it starts at
-500, -484
