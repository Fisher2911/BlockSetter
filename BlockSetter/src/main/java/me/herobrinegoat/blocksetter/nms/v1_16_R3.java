package me.herobrinegoat.blocksetter.nms;


import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;

public class v1_16_R3 implements NMS {


    //Does not send block updates to player
    @Override
    public void setBlockFast(org.bukkit.World world, int x, int y, int z, Material material, byte data, boolean applyPhysics) {
        net.minecraft.server.v1_16_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        net.minecraft.server.v1_16_R3.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
        IBlockData ibd = CraftMagicNumbers.getBlock(material).getBlockData();
        nmsChunk.setType(new BlockPosition(x, y, z), ibd, false);
    }
}