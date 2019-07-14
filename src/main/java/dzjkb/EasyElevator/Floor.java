package dzjkb.EasyElevator;

import java.util.ArrayList;
import java.util.List;

import dzjkb.EasyElevator.Elevator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;



public class Floor
{
    private Elevator elevator;
    private Location l1;
    private Location l2;
    private World world;
    private Sign callSign;
    private int floorNumber;
    private int height;
    private boolean isCalled = false;
    private boolean hasOpenDoors = false;
    // private Material outputDoorMat = null;
    // private byte outputDoorData = 0;
    private Material outputFloorMat = null;
    private BlockState outputFloorData = null;
    private List<Block> doorOpenBlock = new ArrayList();
    // private List<Block> redstoneOutDoorBlock = new ArrayList();
    private List<Block> redstoneOutFloorBlock = new ArrayList();

    public Floor(Elevator elv, Location l1, Location l2, Sign callSign, int floor)
    {
        this.elevator = elv;
        this.l1 = l1;
        this.l2 = l2;
        this.world = l1.getWorld();
        this.callSign = callSign;
        this.floorNumber = floor;
        this.height = l1.getBlockY();

        updateSign("0");
        initializeSign();
        initializeDoor();
    }

    private void initializeSign()
    {
        this.callSign.setLine(0, ChatColor.DARK_GRAY + "[EElevator]");
        this.callSign.setLine(1, ""+ this.floorNumber);
        this.callSign.update();
    }

    private void initializeDoor()
    {
        int x1 = this.l1.getBlockX();
        int z1 = this.l1.getBlockZ();
        int x2 = this.l2.getBlockX();
        int z2 = this.l2.getBlockZ();

        int xStart = Math.min(x1, x2);
        int xEnd = Math.max(x1, x2);
        int zStart = Math.min(z1, z2);
        int zEnd = Math.max(z1, z2);

        for (int x = xStart; x <= xEnd; x++) {
            for (int z = zStart; z <= zEnd; z++)
            {
                Block tempBlock = this.world.getBlockAt(x, this.l1.getBlockY() + 1, z);
                if (x == xStart || x == xEnd || z == zStart || z == zEnd) {
                    if (tempBlock.getType().equals(Material.ACACIA_DOOR) || 
                    	tempBlock.getType().equals(Material.BIRCH_DOOR) || 
                    	tempBlock.getType().equals(Material.DARK_OAK_DOOR) ||
                    	tempBlock.getType().equals(Material.WOOD_DOOR) ||
                        tempBlock.getType().equals(Material.WOODEN_DOOR) ||
                    	tempBlock.getType().equals(Material.JUNGLE_DOOR) ||
                    	tempBlock.getType().equals(Material.SPRUCE_DOOR) ||
                    	tempBlock.getType().equals(Material.IRON_DOOR) ||
                        tempBlock.getType().equals(Material.IRON_DOOR_BLOCK)){
                        this.doorOpenBlock.add(tempBlock);
                    }
                }
            }
        }
    }

    // TODO why is redstonefloor functionality doubled here
    // private void switchRedstoneDoorOn(boolean b)
    // {
    //     int x1 = this.l1.getBlockX();
    //     int z1 = this.l1.getBlockZ();
    //     int x2 = this.l2.getBlockX();
    //     int z2 = this.l2.getBlockZ();

    //     int xStart = Math.min(x1, x2);
    //     int xEnd = Math.max(x1, x2);
    //     int zStart = Math.min(z1, z2);
    //     int zEnd = Math.max(z1, z2);

    //     for (int x = xStart; x <= xEnd; x++) {
    //         for (int z = zStart; z <= zEnd; z++)
    //         {
    //             Block tempBlock = this.world.getBlockAt(x, this.l1.getBlockY(), z);
    //             if ((x == xStart) || (x == xEnd) || (z == zStart) || (z == zEnd)) {
    //                 if (b)
    //                 {
    //                     if (this.elevator.isOutputDoor(tempBlock))
    //                     {
    //                         this.outputDoorMat = tempBlock.getType();
    //                         tempBlock.setType(Material.REDSTONE_TORCH_ON);
    //                         this.redstoneOutDoorBlock.add(tempBlock);
    //                     }
    //                 }
    //                 else if ((this.elevator.isOutputDoor(tempBlock)) || (tempBlock.getType().equals(Material.REDSTONE_TORCH_ON))) {
    //                     if (this.redstoneOutDoorBlock.contains(tempBlock))
    //                     {
    //                         tempBlock.setType(this.outputDoorMat);
    //                         this.redstoneOutDoorBlock.remove(tempBlock);
    //                     }
    //                 }
    //             }
    //         }
    //     }
    //     if (!b) {
    //         this.redstoneOutDoorBlock = new ArrayList();
    //     }
    // }

    public void switchRedstoneFloorOn(boolean b)
    {
        int x1 = this.l1.getBlockX();
        int z1 = this.l1.getBlockZ();
        int x2 = this.l2.getBlockX();
        int z2 = this.l2.getBlockZ();

        int xStart = Math.min(x1, x2);
        int xEnd = Math.max(x1, x2);
        int zStart = Math.min(z1, z2);
        int zEnd = Math.max(z1, z2);

        for (int x = xStart; x <= xEnd; x++) {
            for (int z = zStart; z <= zEnd; z++) {
                if ((x == xStart) || (x == xEnd) || (z == zStart) || (z == zEnd)) {
                    Block block = this.world.getBlockAt(x, this.l1.getBlockY(), z);
                    if (b && this.elevator.isOutputFloor(block)) {
                        this.outputFloorMat = block.getType();
                        block.setType(Material.REDSTONE_TORCH_ON);
                        this.redstoneOutFloorBlock.add(block);
                    }
                    else if ((this.elevator.isOutputFloor(block) || block.getType().equals(Material.REDSTONE_TORCH_ON)) &&
                             this.redstoneOutFloorBlock.contains(block)) {
                        block.setType(this.outputFloorMat);
                        this.redstoneOutFloorBlock.remove(block);
                    }
                }
            }
        }
        if (!b) {
            this.redstoneOutFloorBlock = new ArrayList();
        }
    }

    // public void openDoor() {
    //     try {
    //         // switchRedstoneDoorOn(true);
    //         for (Block block : this.doorOpenBlock) {
    //         	BlockState bs = block.getState();
    //         	Door d = (Door)bs.getData();
    //         	if (!d.isOpen()) {
    //         		d.setOpen(true);
    //         		bs.setData(d);
    //         		bs.update();
    //         	}
    //         }
    //     } catch (Exception e) {
    //         // rip doors, who needs them anyway
    //         return;
    //     }
    //     this.hasOpenDoors = true;
    // }

    // public void closeDoor() {
    //     try {
    //         for (Block block : this.doorOpenBlock) {
    //         	BlockState bs = block.getState();
    //         	Door d = (Door)bs.getData();
    //         	if (d.isOpen()) {
    //         		d.setOpen(false);
    //         		bs.setData(d);
    //         		bs.update();
    //         	}
    //         }
    //     } catch (Exception e) {
    //         // rip doors, who needs them anyway
    //         return;
    //     }
    //     this.hasOpenDoors = false;
    //     // switchRedstoneDoorOn(false);
    // }

    public void openDoor() {
        this.setDoor(true);
    }

    public void closeDoor() {
        this.setDoor(false);
    }

    public void setDoor(boolean open) {
        try {
            for (Block block : this.doorOpenBlock) {
                BlockState bs = block.getState();
                Door d = (Door)bs.getData();
                if (d.isOpen() ^ open) {
                    d.setOpen(open);
                    bs.setData(d);
                    bs.update();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        this.hasOpenDoors = open;
        // switchRedstoneDoorOn(false);
    }

    public void updateSign(String platformFloor)
    {
        this.callSign.setLine(2, platformFloor);
        this.callSign.update();
    }

    public void setCalled(boolean b) {
        if (b) {
            if (!this.hasOpenDoors) {
                this.callSign.setLine(3, "Called");
                this.callSign.update();
                this.isCalled = true;
            }
        } else {
            this.callSign.setLine(3, "");
            this.callSign.update();
            this.isCalled = false;
        }
    }

    public boolean isCalled()
    {
        return this.isCalled;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getFloor()
    {
        return this.floorNumber;
    }

    public int getSignHeight()
    {
        return this.callSign.getY();
    }

    public void writeSign(int line, String message)
    {
        this.callSign.setLine(line, message);
        this.callSign.update();
    }

    public String getIdentifyLocation()
    {
        return this.callSign.getBlock().getLocation().getBlockX() + " " + this.callSign.getBlock().getLocation().getBlockZ();
    }

    public Sign getSign()
    {
        return this.callSign;
    }

    public void playOpenSound()
    {
        for (Block block : this.doorOpenBlock)
        {
            Location loc = block.getLocation();
            loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_PLING, 1.0F, 0.0F);
        }
    }
}
