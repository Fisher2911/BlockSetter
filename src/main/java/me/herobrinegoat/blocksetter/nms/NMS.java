package me.herobrinegoat.blocksetter.nms;

import org.bukkit.Material;

public interface NMS {

    void setBlockFast(org.bukkit.World world, int x, int y, int z, Material material, byte data, boolean applyPhysics);

}
