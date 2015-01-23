/**
 * Created by kroy on 1/23/2015.
 */
package com.EE;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyElevator
        extends JavaPlugin
{
    private EEPlayerListener pl = new EEPlayerListener(this);
    private EEConfiguration config = new EEConfiguration();
    public List<Elevator> elevators = new ArrayList();
    private int MaxPerimeter = 24;
    private int MaxFloors = 10;
    private boolean ArrivalSound = true;
    private boolean ArrivalMessage = true;
    private String BlockBorder = "41";
    private String BlockFloor = "42";
    private String BlockOutputDoor = "35:14";
    private String BlockOutputFloor = "35:1";

    public EasyElevator() {}

    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.pl, this);
        this.config.createConfig();
        this.MaxPerimeter = this.config.getMaxPerimeter();
        this.MaxFloors = this.config.getMaxFloors();
        this.ArrivalSound = this.config.getArrivalSound();
        this.ArrivalMessage = this.config.getArrivalMessage();
        this.BlockBorder = this.config.getBlock("Border");
        this.BlockFloor = this.config.getBlock("Floor");
        this.BlockOutputFloor = this.config.getBlock("OutputFloor");
        this.BlockOutputDoor = this.config.getBlock("OutputDoor");
    }

    public void onDisable()
    {
        for (Elevator e : this.elevators) {
            if (e.currentFloor != null) {
                e.currentFloor.switchRedstoneFloorOn(false);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel1, String[] args)
    {
        if ((commandLabel1.equals("elv")) || (commandLabel1.equals("eelevator"))) {
            if ((sender instanceof Player))
            {
                Player player = (Player)sender;
                EEPermissionManager pm = new EEPermissionManager(player);
                if (args.length == 1)
                {
                    if (args[0].equals("reload")) {
                        if (pm.has("easyelevator.reload"))
                        {
                            this.MaxPerimeter = this.config.getMaxPerimeter();
                            this.MaxFloors = this.config.getMaxFloors();
                            this.ArrivalSound = this.config.getArrivalSound();
                            this.ArrivalMessage = this.config.getArrivalMessage();
                            this.BlockBorder = this.config.getBlock("Border");
                            this.BlockFloor = this.config.getBlock("Floor");
                            this.BlockOutputFloor = this.config.getBlock("OutputFloor");
                            this.BlockOutputDoor = this.config.getBlock("OutputDoor");
                            for (Elevator e : this.elevators) {
                                if (e.currentFloor != null) {
                                    e.currentFloor.switchRedstoneFloorOn(false);
                                }
                            }
                            this.elevators.clear();
                            player.sendMessage(ChatColor.DARK_GRAY +
                                    "[EElevator] " + ChatColor.GRAY +
                                    "The plugin has been reloaded");
                        }
                        else
                        {
                            player.sendMessage(ChatColor.DARK_GRAY +
                                    "[EElevator] " + ChatColor.GRAY +
                                    "You don't have permission to do this");
                        }
                    }
                    if (args[0].equals("call")) {
                        if ((pm.has("easyelevator.call.cmd")) || (pm.has("easyelevator.call.*")))
                        {
                            boolean success = Call(player);
                            if (success) {
                                player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator has been called");
                            } else {
                                player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "No Elevator in range");
                            }
                        }
                        else
                        {
                            player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
                        }
                    }
                    if (args[0].equals("stop")) {
                        if ((pm.has("easyelevator.stop.cmd")) || (pm.has("easyelevator.stop.*"))) {
                            for (int i = 0; i < this.elevators.size(); i++)
                            {
                                Elevator e = (Elevator)this.elevators.get(i);
                                if (e.isInElevator(player))
                                {
                                    int target = e.getFloorNumberFromHeight(e.getNextFloorHeight_2());
                                    if (target != -1)
                                    {
                                        e.addStops(target);
                                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + target);
                                        return true;
                                    }
                                }
                            }
                        } else {
                            player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
                        }
                    }
                }
                if (args.length == 2) {
                    if (args[0].equals("stop"))
                    {
                        if ((pm.has("easyelevator.stop.cmd")) || (pm.has("easyelevator.stop.*"))) {
                            try
                            {
                                int target = Integer.parseInt(args[1]);
                                for (int i = 0; i < this.elevators.size(); i++)
                                {
                                    Elevator e = (Elevator)this.elevators.get(i);
                                    if (e.isInElevator(player))
                                    {
                                        if ((target > e.getFloors().size()) || (target < 1))
                                        {
                                            player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Floor '" + target + "' is not in range");
                                            return true;
                                        }
                                        e.addStops(target);
                                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + target);
                                        i = this.elevators.size();
                                    }
                                }
                            }
                            catch (Exception ex)
                            {
                                player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Floor '" + args[1] + "' is not a valid value");
                                return true;
                            }
                        }
                    }
                    else {
                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
                    }
                }
            }
        }
        return true;
    }

    private boolean Call(Player player)
    {
        org.bukkit.block.Sign sign = getSurroundingElevatorSign(player);
        if (sign != null)
        {
            Elevator e = getElevator(sign);
            if (e != null)
            {
                e.Call(sign.getY());
                return true;
            }
        }
        return false;
    }

    private org.bukkit.block.Sign getSurroundingElevatorSign(Player player)
    {
        Block tempBlock = null;
        World world = player.getWorld();
        Location loc = player.getLocation();

        Location l1 = null;Location l2 = null;
        l1 = loc;
        l2 = loc;

        int z = 0;

        int x1 = l1.getBlockX();
        int y1 = l1.getBlockY();
        int z1 = l1.getBlockZ();

        int x2 = l2.getBlockX();
        int y2 = l2.getBlockY();
        int z2 = l2.getBlockZ();

        int xStart = 0;int xEnd = 0;int yStart = 0;int yEnd = 0;int zStart = 0;int zEnd = 0;
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
        if (y1 < y2)
        {
            yStart = y1;
            yEnd = y2;
        }
        if (y1 > y2)
        {
            yStart = y2;
            yEnd = y1;
        }
        if (y1 == y2)
        {
            yStart = y1;
            yEnd = y1;
        }
        xStart -= 5;yStart += 0;zStart -= 5;xEnd += 5;yEnd += 2;zEnd += 5;
        for (int i = xStart; i <= xEnd; i++)
        {
            int x = i;
            for (int j = yStart; j <= yEnd; j++)
            {
                int y = j;
                for (int k = zStart; k <= zEnd; k++)
                {
                    z = k;

                    tempBlock = world.getBlockAt(x, y, z);
                    if ((tempBlock.getType().equals(Material.WALL_SIGN)) || (tempBlock.getType().equals(Material.SIGN)) || (tempBlock.getType().equals(Material.SIGN_POST)))
                    {
                        org.bukkit.block.Sign sign = (org.bukkit.block.Sign)tempBlock.getState();
                        if (sign.getLine(0).equals(ChatColor.DARK_GRAY + "[EElevator]"))
                        {
                            boolean isPS = false;
                            for (Elevator e : this.elevators) {
                                if (e.getPlatform().getSign().equals(sign)) {
                                    isPS = true;
                                }
                            }
                            if (!isPS) {
                                return (org.bukkit.block.Sign)tempBlock.getState();
                            }
                        }
                    }
                    tempBlock = null;
                }
            }
        }
        return null;
    }

    public Elevator getElevator(org.bukkit.block.Sign sign)
    {
        if ((sign.getLine(0).equals("[EElevator]")) || (sign.getLine(0).equals(ChatColor.DARK_GRAY + "[EElevator]")))
        {
            Elevator e = null;
            for (int i = 0; i < this.elevators.size(); i++)
            {
                org.bukkit.material.Sign signData = (org.bukkit.material.Sign)sign.getData();
                Block attached = sign.getBlock().getRelative(signData.getAttachedFace());
                if (((Elevator)this.elevators.get(i)).isPartOfElevator(attached.getLocation())) {
                    if ((((Elevator)this.elevators.get(i)).isFloorSign(sign)) || (((Elevator)this.elevators.get(i)).isPlatformSign(sign)))
                    {
                        e = (Elevator)this.elevators.get(i);
                        i = this.elevators.size();
                    }
                }
            }
            if (e == null) {
                e = new Elevator(this, sign);
            }
            if (e != null) {
                if (e.isInitialized())
                {
                    if (!this.elevators.contains(e)) {
                        this.elevators.add(e);
                    }
                    return e;
                }
            }
        }
        return null;
    }

    public int getMaxPerimeter()
    {
        return this.MaxPerimeter;
    }

    public int getMaxFloors()
    {
        return this.MaxFloors;
    }

    public boolean getArrivalSound()
    {
        return this.ArrivalSound;
    }

    public boolean getArrivalMessage()
    {
        return this.ArrivalMessage;
    }

    public String getBlockBorder()
    {
        return this.BlockBorder;
    }

    public String getBlockFloor()
    {
        return this.BlockFloor;
    }

    public String getBlockOutputDoor()
    {
        return this.BlockOutputDoor;
    }

    public String getBlockOutputFloor()
    {
        return this.BlockOutputFloor;
    }
}
