package com.EE;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class Elevator
        implements Runnable
{
    public EasyElevator plugin;
    private org.bukkit.block.Sign s;
    private Block attached;
    private World world;
    private int highestPoint;
    private int lowestPoint;
    private int xLow;
    private int xHigh;
    private int zLow;
    private int zHigh;
    private int maxFloors = -1;
    private int maxPerimeter = -1;
    private List<Integer> stops = new ArrayList();
    public Floor currentFloor = null;
    private String Direction = "";
    private boolean isMoving = false;
    private boolean hasOpenDoor = false;
    private boolean isInitialized = false;
    private List<Floor> floors = new ArrayList();
    private Platform platform;

    public Elevator(EasyElevator elev, org.bukkit.block.Sign s)
    {
        this.plugin = elev;
        this.world = s.getWorld();
        this.s = s;
        this.maxFloors = elev.getMaxFloors();
        this.maxPerimeter = elev.getMaxPerimeter();

        org.bukkit.material.Sign signData = (org.bukkit.material.Sign)s.getData();
        this.attached = s.getBlock().getRelative(signData.getAttachedFace());

        initializeLift();
    }

    private void initializeLift()
    {
        int count = 0;

        int low = -1;
        int high = -1;
        for (int i = this.s.getY(); i >= 0; i--)
        {
            Block b = this.world.getBlockAt(this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
            if (isBorder(b))
            {
                low = i;
                i = -1;
            }
        }
        for (int i = this.s.getY(); i < this.world.getMaxHeight(); i++)
        {
            Block b = this.world.getBlockAt(this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
            if (isBorder(b))
            {
                high = i;
                i = this.world.getMaxHeight();
            }
        }
        if ((low == -1) || (high == -1)) {
            return;
        }
        this.highestPoint = high;
        this.lowestPoint = low;

        Block b1 = null;
        Block b2 = null;
        for (int i = low; i < high; i++)
        {
            Location currLoc = new Location(this.world, this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
            Block target = this.world.getBlockAt(currLoc);
            if (isFloor(target))
            {
                int dirChange = 0;

                String dir = "";

                List<Block> blocks = new ArrayList();
                Block Start = target;
                Block t = null;

                b2 = null;
                b1 = null;
                do
                {
                    Block temp = null;
                    if (t == null)
                    {
                        if (temp == null)
                        {
                            temp = checkForIron(Start, Start.getRelative(0, 0, 1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "East")) {
                                    dirChange++;
                                }
                                dir = "East";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(Start, Start.getRelative(0, 0, -1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "West")) {
                                    dirChange++;
                                }
                                dir = "West";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(Start, Start.getRelative(1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "North")) {
                                    dirChange++;
                                }
                                dir = "North";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(Start, Start.getRelative(-1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "South")) {
                                    dirChange++;
                                }
                                dir = "South";
                            }
                        }
                    }
                    else if (t != null)
                    {
                        if (temp == null)
                        {
                            temp = checkForIron(Start, t.getRelative(0, 0, 1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "East")) {
                                    dirChange++;
                                }
                                dir = "East";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(Start, t.getRelative(0, 0, -1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "West")) {
                                    dirChange++;
                                }
                                dir = "West";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(Start, t.getRelative(1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "North")) {
                                    dirChange++;
                                }
                                dir = "North";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(Start, t.getRelative(-1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "South")) {
                                    dirChange++;
                                }
                                dir = "South";
                            }
                        }
                    }
                    if (temp == null) {
                        return;
                    }
                    if (dirChange == 1) {
                        if (b1 == null) {
                            b1 = (Block)blocks.get(blocks.size() - 1);
                        }
                    }
                    if (dirChange == 3) {
                        if (b2 == null) {
                            b2 = (Block)blocks.get(blocks.size() - 1);
                        }
                    }
                    blocks.add(temp);
                } while (!Start.equals(t));
                if (blocks.size() > this.maxPerimeter) {
                    return;
                }
                if (blocks.contains(target))
                {
                    if ((b1 == null) || (b2 == null)) {
                        return;
                    }
                    if ((dirChange != 4) && (dirChange != 3)) {
                        return;
                    }
                    org.bukkit.block.Sign callSign = getCallSign(b1.getLocation(), b2.getLocation());
                    if (callSign != null)
                    {
                        Floor floor = new Floor(this, b1.getLocation(), b2.getLocation(), callSign, count + 1);
                        this.floors.add(floor);
                        count++;
                    }
                }
                else
                {
                    return;
                }
            }
        }
        if (this.floors.size() > this.maxFloors) {
            return;
        }
        this.platform = new Platform(this.plugin, b1.getLocation(), b2.getLocation(), ((Floor)this.floors.get(0)).getHeight(), ((Floor)this.floors.get(this.floors.size() - 1)).getHeight());
        if (!this.platform.isInitialized()) {
            return;
        }
        this.isInitialized = true;
        System.out.println("[EasyElevator] An elevator has been initialized");
    }

    private org.bukkit.block.Sign getCallSign(Location l1, Location l2)
    {
        BlockFace[] faces = new BlockFace[4];
        faces[0] = BlockFace.NORTH;
        faces[1] = BlockFace.EAST;
        faces[2] = BlockFace.SOUTH;
        faces[3] = BlockFace.WEST;

        int x1 = l1.getBlockX();
        int z1 = l1.getBlockZ();

        int x2 = l2.getBlockX();
        int z2 = l2.getBlockZ();

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
        this.xLow = xStart;
        this.xHigh = xEnd;
        this.zLow = zStart;
        this.zHigh = zEnd;

        xStart--;
        xEnd++;

        zStart--;
        zEnd++;
        for (int i = l1.getBlockY() + 2; i <= l1.getBlockY() + 3; i++) {
            for (int x = xStart; x <= xEnd; x++) {
                for (int z = zStart; z <= zEnd; z++)
                {
                    Block tempBlock = this.world.getBlockAt(x, i, z);
                    if ((x == xStart) || (x == xEnd))
                    {
                        if (tempBlock.getType().equals(Material.WALL_SIGN))
                        {
                            org.bukkit.block.Sign sign = (org.bukkit.block.Sign)tempBlock.getState();
                            return sign;
                        }
                    }
                    else if ((z == zStart) || (z == zEnd)) {
                        if (tempBlock.getType().equals(Material.WALL_SIGN))
                        {
                            org.bukkit.block.Sign sign = (org.bukkit.block.Sign)tempBlock.getState();
                            return sign;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean dirChanged(String dir, String newDir)
    {
        if (dir.equals("")) {
            return false;
        }
        if (dir.equals(newDir)) {
            return false;
        }
        return true;
    }

    private Block checkForIron(Block Start, Block t, List<Block> blocks)
    {
        if ((isFloor(t)) || (isOutputDoor(t)) || (isOutputFloor(t)))
        {
            if ((Start.equals(t)) && (blocks.size() <= 4)) {
                return null;
            }
            if (!blocks.contains(t)) {
                return t;
            }
        }
        return null;
    }

    public void addStops(int Floor)
    {
        int height = -1;
        for (int i = 0; i < this.floors.size(); i++) {
            if (((Floor)this.floors.get(i)).getFloor() == Floor) {
                height = ((Floor)this.floors.get(i)).getHeight();
            }
        }
        addStopsFromHeight(height);
    }

    public void addStopsFromHeight(int height)
    {
        if (height != -1) {
            if (!this.stops.contains(Integer.valueOf(height)))
            {
                this.stops.add(Integer.valueOf(height));
                if (!this.isMoving)
                {
                    this.isMoving = true;
                    run();
                }
            }
        }
    }

    public void Call(int height)
    {
        boolean hasHeight = false;
        Floor f = null;
        for (int i = 0; i < this.floors.size(); i++)
        {
            f = (Floor)this.floors.get(i);
            if (f.getSignHeight() == height)
            {
                hasHeight = true;
                f.setCalled(true);
                i = this.floors.size();
            }
        }
        if (hasHeight) {
            addStopsFromHeight(f.getHeight());
        }
    }

    public void StopAt(int floor)
    {
        for (Floor f : this.floors) {
            if (f.getFloor() == floor)
            {
                Call(f.getSignHeight());
                return;
            }
        }
    }

    int lcount = 0;

    public void run()
    {
        if (this.lcount == 6) {
            this.lcount = 0;
        }
        updateDirection();
        updateFloorIndicator();
        if (!this.hasOpenDoor)
        {
            if (!this.platform.isStuck())
            {
                if (this.stops.contains(Integer.valueOf(this.platform.getHeight())))
                {
                    for (Floor f : this.floors) {
                        if (f.getHeight() == this.platform.getHeight()) {
                            this.currentFloor = f;
                        }
                    }
                    if (this.currentFloor != null)
                    {
                        if (this.plugin.getArrivalSound()) {
                            this.currentFloor.playOpenSound();
                        }
                        this.currentFloor.switchRedstoneFloorOn(true);
                        this.currentFloor.OpenDoor();
                        this.hasOpenDoor = true;
                        this.currentFloor.setCalled(false);
                        this.platform.stopTeleport();
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 100L);
                    }
                }
                else
                {
                    if (!this.Direction.equals("")) {
                        if (this.currentFloor != null)
                        {
                            this.currentFloor.switchRedstoneFloorOn(false);
                            this.currentFloor = null;
                        }
                    }
                    if (this.Direction.equals("DOWN"))
                    {
                        this.platform.moveDown(this.lcount);
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                        this.lcount += 1;
                    }
                    else if (!this.Direction.equals("UP"))
                    {
                        this.isMoving = false;
                        return;
                    }
                    if (this.Direction.equals("UP"))
                    {
                        this.platform.moveUp(this.lcount);
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                        this.lcount += 1;
                    }
                    else if (!this.Direction.equals("DOWN"))
                    {
                        this.isMoving = false;
                    }
                }
            }
            else
            {
                if (this.Direction.equals("UP")) {
                    this.Direction = "DOWN";
                } else {
                    this.Direction = "UP";
                }
                this.stops.clear();
                addStops(getFloorNumberFromHeight(getNextFloorHeight_2()));
                this.platform.isStuck(false);
                this.platform.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator is stuck. Resetting...");
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 50L);
            }
        }
        else if (this.currentFloor != null)
        {
            this.currentFloor.CloseDoor();
            this.hasOpenDoor = false;
            removeCurrentFloor();
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 5L);
        }
    }

    public void changeFloor()
    {
        int curr = Integer.parseInt(this.platform.getSign().getLine(1));
        int next = curr + 1;
        if (next > this.floors.size()) {
            next = 1;
        }
        this.platform.writeSign(1, next);
    }

    public int getFloorNumberFromHeight(int hight)
    {
        int floor = -1;
        for (Floor f : this.floors) {
            if (f.getHeight() == hight) {
                return f.getFloor();
            }
        }
        return floor;
    }

    public int getNextFloorHeight_2()
    {
        int next = -1;
        int current = this.platform.getHeight();
        if (this.Direction.equals("UP"))
        {
            for (int i = 0; i < this.floors.size(); i++)
            {
                int t = ((Floor)this.floors.get(i)).getHeight();
                if ((next == -1) &&
                        (t > current)) {
                    next = t;
                }
                if ((t > current) && (t < next)) {
                    next = t;
                }
            }
            return next;
        }
        if (this.Direction.equals("DOWN"))
        {
            for (int i = 0; i < this.floors.size(); i++)
            {
                int t = ((Floor)this.floors.get(i)).getHeight();
                if ((next == -1) &&
                        (t < current)) {
                    next = t;
                }
                if ((t < current) && (t > next)) {
                    next = t;
                }
            }
            return next;
        }
        if (this.Direction.equals("")) {
            return this.platform.getHeight();
        }
        return -1;
    }

    public int getNextFloorHeight()
    {
        if (this.currentFloor != null)
        {
            int next = -1;
            int current = this.currentFloor.getHeight();
            if (this.Direction.equals("UP")) {
                for (int i = 0; i < this.stops.size(); i++)
                {
                    int t = ((Integer)this.stops.get(i)).intValue();
                    if ((next == -1) &&
                            (t > current)) {
                        next = t;
                    }
                    if ((t > current) && (t < next)) {
                        next = t;
                    }
                }
            }
            if (this.Direction.equals("DOWN")) {
                for (int i = 0; i < this.stops.size(); i++)
                {
                    int t = ((Integer)this.stops.get(i)).intValue();
                    if ((next == -1) &&
                            (t < current)) {
                        next = t;
                    }
                    if ((t < current) && (t > next)) {
                        next = t;
                    }
                }
            }
            return next;
        }
        return -1;
    }

    public Platform getPlatform()
    {
        return this.platform;
    }

    public Floor getMainFloor()
    {
        return (Floor)this.floors.get(0);
    }

    public boolean isInitialized()
    {
        return this.isInitialized;
    }

    private void removeCurrentFloor()
    {
        for (int i = 0; i < this.stops.size(); i++) {
            if (((Integer)this.stops.get(i)).intValue() == this.platform.getHeight()) {
                this.stops.remove(i);
            }
        }
    }

    private void updateDirection()
    {
        int height = this.platform.getHeight();
        for (Iterator localIterator = this.stops.iterator(); localIterator.hasNext();)
        {
            int i = ((Integer)localIterator.next()).intValue();
            if (this.Direction.equals("DOWN")) {
                if (i < height) {
                    return;
                }
            }
            if (this.Direction.equals("UP")) {
                if (i > height) {
                    return;
                }
            }
            if (this.Direction.equals(""))
            {
                if (i > height) {
                    this.Direction = "UP";
                } else {
                    this.Direction = "DOWN";
                }
                return;
            }
        }
        if (this.stops.size() > 0)
        {
            if (this.Direction.equals("DOWN"))
            {
                this.Direction = "UP";
                return;
            }
            if (this.Direction.equals("UP")) {
                this.Direction = "DOWN";
            }
        }
        else
        {
            this.Direction = "";
        }
    }

    private void updateFloorIndicator()
    {
        int curr = getCurrentFloor();
        for (int i = 0; i < this.floors.size(); i++) {
            if (curr != -1)
            {
                ((Floor)this.floors.get(i)).writeSign(2, curr);
            }
            else
            {
                if (this.Direction.equals("UP")) {
                    ((Floor)this.floors.get(i)).writeSign(2, "/\\");
                }
                if (this.Direction.equals("DOWN")) {
                    ((Floor)this.floors.get(i)).writeSign(2, "\\/");
                }
            }
        }
        if (curr != -1)
        {
            this.platform.writeSign(2, curr);
        }
        else
        {
            if (this.Direction.equals("UP")) {
                this.platform.writeSign(2, "/\\");
            }
            if (this.Direction.equals("DOWN")) {
                this.platform.writeSign(2, "\\/");
            }
        }
        int next = getFloorNumberFromHeight(getNextFloorHeight());
        if (next != -1) {
            this.platform.writeSign(3, next);
        } else {
            this.platform.writeSign(3, "-");
        }
    }

    public int getCurrentFloor()
    {
        if (isFloor(this.platform.getHeight())) {
            for (int i = 0; i < this.floors.size(); i++) {
                if (this.platform.getHeight() == ((Floor)this.floors.get(i)).getHeight()) {
                    return ((Floor)this.floors.get(i)).getFloor();
                }
            }
        }
        return -1;
    }

    public boolean isPartOfElevator(Location loc)
    {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        if ((y > this.lowestPoint) && (y < this.highestPoint) && (x >= this.xLow) && (x <= this.xHigh) && (z >= this.zLow) && (z <= this.zHigh)) {
            return true;
        }
        return false;
    }

    public boolean isFloorSign(org.bukkit.block.Sign sign)
    {
        for (int i = 0; i < this.floors.size(); i++)
        {
            Floor f = (Floor)this.floors.get(i);
            if (f.getSign().equals(sign)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlatformSign(org.bukkit.block.Sign sign)
    {
        if (this.platform.getSign().equals(sign)) {
            return true;
        }
        return false;
    }

    public boolean isInElevator(Player player)
    {
        return this.platform.hasPlayer(player);
    }

    public boolean isFloor(int floorHeight)
    {
        for (int i = 0; i < this.floors.size(); i++) {
            if (floorHeight == ((Floor)this.floors.get(i)).getHeight()) {
                return true;
            }
        }
        return false;
    }

    public List<Floor> getFloors()
    {
        return this.floors;
    }

    public boolean isBorder(Block b)
    {
        try
        {
            String border = this.plugin.getBlockBorder();
            int id = -1;
            int data = -1;
            if (border.contains(":"))
            {
                id = Integer.parseInt(border.split(":")[0]);
                data = Integer.parseInt(border.split(":")[1]);
            }
            else
            {
                id = Integer.parseInt(border);
            }
            if (data != -1)
            {
                if ((data == b.getData()) && (id == b.getTypeId())) {
                    return true;
                }
            }
            else if (id == b.getTypeId()) {
                return true;
            }
        }
        catch (Exception localException) {}
        return false;
    }

    public boolean isFloor(Block b)
    {
        try
        {
            String border = this.plugin.getBlockFloor();
            int id = -1;
            int data = -1;
            if (border.contains(":"))
            {
                id = Integer.parseInt(border.split(":")[0]);
                data = Integer.parseInt(border.split(":")[1]);
            }
            else
            {
                id = Integer.parseInt(border);
            }
            if (data != -1)
            {
                if ((data == b.getData()) && (id == b.getTypeId())) {
                    return true;
                }
            }
            else if (id == b.getTypeId()) {
                return true;
            }
        }
        catch (Exception localException) {}
        return false;
    }

    public boolean isOutputFloor(Block b)
    {
        try
        {
            String border = this.plugin.getBlockOutputFloor();
            int id = -1;
            int data = -1;
            if (border.contains(":"))
            {
                id = Integer.parseInt(border.split(":")[0]);
                data = Integer.parseInt(border.split(":")[1]);
            }
            else
            {
                id = Integer.parseInt(border);
            }
            if (data != -1)
            {
                if ((data == b.getData()) && (id == b.getTypeId())) {
                    return true;
                }
            }
            else if (id == b.getTypeId()) {
                return true;
            }
        }
        catch (Exception localException) {}
        return false;
    }

    public boolean isOutputDoor(Block b)
    {
        try
        {
            String border = this.plugin.getBlockOutputDoor();
            int id = -1;
            int data = -1;
            if (border.contains(":"))
            {
                id = Integer.parseInt(border.split(":")[0]);
                data = Integer.parseInt(border.split(":")[1]);
            }
            else
            {
                id = Integer.parseInt(border);
            }
            if (data != -1)
            {
                if ((data == b.getData()) && (id == b.getTypeId())) {
                    return true;
                }
            }
            else if (id == b.getTypeId()) {
                return true;
            }
        }
        catch (Exception localException) {}
        return false;
    }
}
