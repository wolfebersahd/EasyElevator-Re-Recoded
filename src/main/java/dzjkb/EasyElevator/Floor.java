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
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.material.MaterialData;



public class Floor
{
    private Elevator elevator;
    private Location l1;
    private Location l2;
    private World world;
    private Sign callSign;
    private int floor;
    private int height;
    private boolean isCalled = false;
    private boolean hasOpenDoors = false;
    private Material OutputDoorMat = null;
    private byte OutputDoorData = 0;
    private Material OutputFloorMat = null;
    private BlockState OutputFloorData = null;
    private List<Block> doorOpenBlock = new ArrayList();
    private List<Block> redstoneOutDoorBlock = new ArrayList();
    private List<Block> redstoneOutFloorBlock = new ArrayList();

    public Floor(Elevator elv, Location l1, Location l2, Sign callSign, int floor)
    {
        this.elevator = elv;
        this.l1 = l1;
        this.l2 = l2;
        this.world = l1.getWorld();
        this.callSign = callSign;
        this.floor = floor;
        this.height = l1.getBlockY();

        updateSign("0");
        initializeSign();
        initializeDoor();
    }

    private void initializeSign()
    {
        this.callSign.setLine(0, ChatColor.DARK_GRAY + "[EElevator]");
        this.callSign.setLine(1, ""+ this.floor);
        this.callSign.update();
    }

    private void initializeDoor()
    {
        int x1 = this.l1.getBlockX();
        int z1 = this.l1.getBlockZ();

        int x2 = this.l2.getBlockX();
        int z2 = this.l2.getBlockZ();

        int xStart = 0;int xEnd = 0;int zStart = 0;int zEnd = 0;
        if (x1 < x2)
        {
            xStart = x1;
            xEnd = x2;
        }
        if (x1 > x2)
        {
            xStart = x2;
            xEnd = x1;
        }
        if (x1 == x2)
        {
            xStart = x1;
            xEnd = x1;
        }
        if (z1 < z2)
        {
            zStart = z1;
            zEnd = z2;
        }
        if (z1 > z2)
        {
            zStart = z2;
            zEnd = z1;
        }
        if (z1 == z2)
        {
            zStart = z1;
            zEnd = z1;
        }
        for (int x = xStart; x <= xEnd; x++) {
            for (int z = zStart; z <= zEnd; z++)
            {
                Block tempBlock = this.world.getBlockAt(x, this.l1.getBlockY() + 1, z);
                if ((x == xStart) || (x == xEnd) || (z == zStart) || (z == zEnd)) {
                    if ((tempBlock.getType().equals(Material.ACACIA_DOOR)) || 
                    	(tempBlock.getType().equals(Material.BIRCH_DOOR)) || 
                    	(tempBlock.getType().equals(Material.DARK_OAK_DOOR)) ||
                    	(tempBlock.getType().equals(Material.OAK_DOOR)) ||
                    	(tempBlock.getType().equals(Material.JUNGLE_DOOR)) ||
                    	(tempBlock.getType().equals(Material.SPRUCE_DOOR)) ||
                    	(tempBlock.getType().equals(Material.IRON_DOOR))){
                        this.doorOpenBlock.add(tempBlock);
                    }
                }
            }
        }
    }

    private void switchRedstoneDoorOn(boolean b)
    {
        int x1 = this.l1.getBlockX();
        int z1 = this.l1.getBlockZ();

        int x2 = this.l2.getBlockX();
        int z2 = this.l2.getBlockZ();

        int xStart = 0;int xEnd = 0;int zStart = 0;int zEnd = 0;
        if (x1 < x2)
        {
            xStart = x1;
            xEnd = x2;
        }
        if (x1 > x2)
        {
            xStart = x2;
            xEnd = x1;
        }
        if (x1 == x2)
        {
            xStart = x1;
            xEnd = x1;
        }
        if (z1 < z2)
        {
            zStart = z1;
            zEnd = z2;
        }
        if (z1 > z2)
        {
            zStart = z2;
            zEnd = z1;
        }
        if (z1 == z2)
        {
            zStart = z1;
            zEnd = z1;
        }
        for (int x = xStart; x <= xEnd; x++) {
            for (int z = zStart; z <= zEnd; z++)
            {
                Block tempBlock = this.world.getBlockAt(x, this.l1.getBlockY(), z);
                if ((x == xStart) || (x == xEnd) || (z == zStart) || (z == zEnd)) {
                    if (b)
                    {
                        if (this.elevator.isOutputDoor(tempBlock))
                        {
                            this.OutputDoorMat = tempBlock.getType();
                            tempBlock.setType(Material.REDSTONE_TORCH);
                            this.redstoneOutDoorBlock.add(tempBlock);
                        }
                    }
                    else if ((this.elevator.isOutputDoor(tempBlock)) || (tempBlock.getType().equals(Material.REDSTONE_TORCH))) {
                        if (this.redstoneOutDoorBlock.contains(tempBlock))
                        {
                            tempBlock.setType(this.OutputDoorMat);
                            this.redstoneOutDoorBlock.remove(tempBlock);
                        }
                    }
                }
            }
        }
        if (!b) {
            this.redstoneOutDoorBlock = new ArrayList();
        }
    }

    public void switchRedstoneFloorOn(boolean b)
    {
        int x1 = this.l1.getBlockX();
        int z1 = this.l1.getBlockZ();

        int x2 = this.l2.getBlockX();
        int z2 = this.l2.getBlockZ();

        int xStart = 0;int xEnd = 0;int zStart = 0;int zEnd = 0;
        if (x1 < x2)
        {
            xStart = x1;
            xEnd = x2;
        }
        if (x1 > x2)
        {
            xStart = x2;
            xEnd = x1;
        }
        if (x1 == x2)
        {
            xStart = x1;
            xEnd = x1;
        }
        if (z1 < z2)
        {
            zStart = z1;
            zEnd = z2;
        }
        if (z1 > z2)
        {
            zStart = z2;
            zEnd = z1;
        }
        if (z1 == z2)
        {
            zStart = z1;
            zEnd = z1;
        }
        for (int x = xStart; x <= xEnd; x++) {
            for (int z = zStart; z <= zEnd; z++)
            {
                Block tempBlock = this.world.getBlockAt(x, this.l1.getBlockY(), z);
                if ((x == xStart) || (x == xEnd) || (z == zStart) || (z == zEnd)) {
                    if (b)
                    {
                        if (this.elevator.isOutputFloor(tempBlock))
                        {
                            this.OutputFloorMat = tempBlock.getType();
                            tempBlock.setType(Material.REDSTONE_TORCH);
                            this.redstoneOutFloorBlock.add(tempBlock);
                            
                        }
                    }
                    else if ((this.elevator.isOutputFloor(tempBlock)) || (tempBlock.getType().equals(Material.REDSTONE_TORCH))) {
                        if (this.redstoneOutFloorBlock.contains(tempBlock))
                        {
                            tempBlock.setType(this.OutputFloorMat);
                            this.redstoneOutFloorBlock.remove(tempBlock);
                        }
                    }
                }
            }
        }
        if (!b) {
            this.redstoneOutFloorBlock = new ArrayList();
        }
    }

    public void OpenDoor()
    {
        switchRedstoneDoorOn(true);
        for (Block block : this.doorOpenBlock) { 
        	BlockState bs = block.getState();
        	BlockData bd = bs.getBlockData();
        	Openable o = (Openable) bd;
        	Door d = (Door) o;
        	if (!d.isOpen()) {
        		d.setOpen(true);
        		bs.setBlockData(o);
        		bs.update();
        	}
//        	BlockState state = block.getState();
//        	Openable openable = (Openable) state.getData();
//        	openable.setOpen(true);
//        	state.setData((MaterialData) openable);
//        	state.update();
//        	Door d = (Door) block.getState().getData();
//        	d.setOpen(true);
            /*if (block.getData() <= 3) {
                block.setData((byte)(block.getData() + 4));
            }*/
        }
        this.hasOpenDoors = true;
    }

    public void CloseDoor()
    {
        for (Block block : this.doorOpenBlock) {
        	BlockState bs = block.getState();
        	BlockData bd = bs.getBlockData();
        	Openable o = (Openable) bd;
        	Door d = (Door) o;
        	if (d.isOpen()) {
        		d.setOpen(false);
        		bs.setBlockData(o);
        		bs.update();
        	}
//        	Openable openable = (Openable) state.getData();
//        	openable.setOpen(false);
//        	state.setData((MaterialData) openable);
//        	state.update();
//        	Door d = (Door) block.getState().getData();
//            d.setOpen(false);
            	
            
        	/*if (block.getData() >= 4) {
                block.setData((byte)(block.getData() - 4));
            }*/
        }
        this.hasOpenDoors = false;
        switchRedstoneDoorOn(false);
    }

    public void updateSign(String platformFloor)
    {
        this.callSign.setLine(2, platformFloor);
        this.callSign.update();
    }

    public void setCalled(boolean b)
    {
        if (b)
        {
            if (!this.hasOpenDoors)
            {
                this.callSign.setLine(3, "Called");
                this.callSign.update();
                this.isCalled = true;
            }
        }
        else
        {
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
        return this.floor;
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
            loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.0F);
        }
    }
}
