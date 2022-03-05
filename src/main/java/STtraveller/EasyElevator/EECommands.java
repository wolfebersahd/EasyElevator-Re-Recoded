package STtraveller.EasyElevator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;

import STtraveller.EasyElevator.EasyElevator;
import STtraveller.EasyElevator.EEUtils;
import STtraveller.EasyElevator.EEPermissions;

public class EECommands implements CommandExecutor {

    private EasyElevator ee;

    public EECommands(EasyElevator e) {
        this.ee = e;
    }

    private void noPerms(Player p) {
        EEUtils.playerMsg(p, "You don't have permission to do this");
    }

    private void help(Player p) {
        p.sendMessage(ChatColor.BLUE + "----------" + ChatColor.DARK_GRAY + "[EasyElevator]" + ChatColor.BLUE + "----------");
        p.sendMessage(ChatColor.GOLD + "Proudly presented by " + ChatColor.AQUA + "ST" + ChatColor.GREEN + "traveller");
        p.sendMessage(ChatColor.BLUE + "EasyElevator help");
        p.sendMessage("./elv help - shows this dialog");
        p.sendMessage("./elv reload - reload the config");
        // TODO
        p.sendMessage("./elv call");
        p.sendMessage("./elv stop");
        p.sendMessage("./elv stop x");
        p.sendMessage(ChatColor.BLUE + "----------" + ChatColor.DARK_GRAY + "[EasyElevator]" + ChatColor.BLUE + "----------");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel1, String[] args) {
        if (!(commandLabel1.equals("elv") || commandLabel1.equals("eelevator")))
            return false;

        if (!(sender instanceof Player))
            return true;

        Player player = (Player)sender;
        EEPermissionManager pm = new EEPermissionManager(player);

        if (this.ee.getEEConfig().debug) {
            this.ee.getLogger().info("EElv got a command:");
            this.ee.getLogger().info(commandLabel1);
            this.ee.getLogger().info("With arguments:");
            for (String a : args)
                this.ee.getLogger().info(a);
        }

        if (args.length <= 0) {
            help(player);
            return true;
        }

        switch (args[0]) {
            case "help":
                help(player);
                return true;

            case "reload":
                if (args.length != 1)
                    help(player);
                else if (!pm.has(EEPermissions.RELOAD))
                    noPerms(player);
                else
                    cmdReload(player);
                return true;

            case "call":
                if (args.length != 1)
                    help(player);
                else if (!(pm.has(EEPermissions.CALL) || pm.has(EEPermissions.CALL_ALL)))
                    noPerms(player);
                else
                    cmdCall(player);
                return true;

            case "stop":
                if (args.length != 1 && args.length != 2)
                    help(player);
                else if (!(pm.has(EEPermissions.STOP_CMD) || pm.has(EEPermissions.STOP_ALL)))
                    noPerms(player);
                else if (args.length == 1)
                    cmdStop(player);
                else if (args.length == 2)
                    cmdStop2(player, args[1]);
                return true;

            default:
                help(player);
                return true;
        }
    }

    private void cmdReload(Player player) {
        this.ee.dbg("command reload activated");
        this.ee.reloadEEConfig();
        for (Elevator e : this.ee.getElevators().getElevators()) {
            if (e.currentFloor != null) {
                e.currentFloor.switchRedstoneFloorOn(false);
            }
        }
        this.ee.getElevators().getElevators().clear();
        EEUtils.playerMsg(player, "The plugin has been reloaded");
    }

    private void cmdCall(Player player) {
        this.ee.dbg("command call activated");
        boolean success = false;
        Sign sign = this.ee.getElevators().getSurroundingElevatorSign(player);
        if (sign != null)
        {
            Elevator e = this.ee.getElevators().getElevator(sign);
            if (e != null)
            {
                e.call(sign.getY());
                success = true;
            }
        }
        if (success) {
            EEUtils.playerMsg(player, "The Elevator has been called");
        } else {
            EEUtils.playerMsg(player, "Failed to call an elevator - out of range or incomplete");
        }
    }

    private void cmdStop(Player player) {
        this.ee.dbg("command stop activated");
        for (int i = 0; i < this.ee.getElevators().getElevators().size(); i++)
        {
            Elevator e = (Elevator)this.ee.getElevators().getElevators().get(i);
            if (e.isInElevator(player))
            {
                int target = e.getFloorNumberFromHeight(e.getNextFloorHeight_2());
                if (target != -1)
                {
                    e.addStops(target);
                    EEUtils.playerMsg(player, "Stopping at floor " + target);
                    break;
                }
            }
        }
    }

    private void cmdStop2(Player player, String arg) {
        this.ee.dbg("command stop2 activated");
        try
        {
            int target = Integer.parseInt(arg);
            for (int i = 0; i < this.ee.getElevators().getElevators().size(); i++)
            {
                Elevator e = this.ee.getElevators().getElevators().get(i);
                if (e.isInElevator(player))
                {
                    if ((target > e.getFloors().size()) || (target < 1))
                    {
                        EEUtils.playerMsg(player, "Floor '" + target + "' is not in range");
                    }
                    e.addStops(target);
                    EEUtils.playerMsg(player, "Stopping at floor " + target);
                    i = this.ee.getElevators().getElevators().size();
                }
            }
        }
        catch (Exception e)
        {
            EEUtils.playerMsg(player, "Floor '" + arg + "' is not a valid value");
        }
    }
}