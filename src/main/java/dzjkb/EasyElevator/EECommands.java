package dzjkb.EasyElevator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;

import dzjkb.EasyElevator.EasyElevator;

public class EECommands implements CommandExecutor {

    private EasyElevator ee;

    public EECommands(EasyElevator e) {
        this.ee = e;
    }

    private void noPerms(Player p) {
        p.sendMessage(
            ChatColor.DARK_GRAY + "[EasyElevator] " + ChatColor.GRAY +
            "You don't have permission to do this"
        );
    }

    private void help(Player p) {
        p.sendMessage(ChatColor.BLUE + "----------" + ChatColor.DARK_GRAY + "[EasyElevator]" + ChatColor.BLUE + "----------");
        p.sendMessage(ChatColor.RED + "EasyElevator help");
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

        if (args.length <= 0)
            help(player);

        switch (args[0]) {
            case "help":
                help(player);
                return true;

            case "reload":
                if (args.length != 1)
                    help(player);
                else if (!pm.has("easyelevator.reload"))
                    noPerms(player);
                else
                    cmdReload(player);
                return true;

            case "call":
                if (args.length != 1)
                    help(player);
                else if (!(pm.has("easyelevator.call") || pm.has("easyelevator.call.*")))
                    noPerms(player);
                else
                    cmdCall(player);
                return true;

            case "stop":
                if (args.length != 1 && args.length != 2)
                    help(player);
                else if (!(pm.has("easyelevator.stop.cmd") || pm.has("easyelevator.stop.*")))
                    noPerms(player);
                else
                    cmdStop(player);
                return true;

            default:
                help(player);
                return true;
        }
    }

    private void cmdReload(Player player) {
        this.ee.reloadConfig();
        for (Elevator e : this.ee.getElevators()) {
            if (e.currentFloor != null) {
                e.currentFloor.switchRedstoneFloorOn(false);
            }
        }
        this.ee.getElevators().clear();
        player.sendMessage(ChatColor.DARK_GRAY +
                "[EasyElevator] " + ChatColor.GRAY +
                "The plugin has been reloaded");
    }

    private void cmdCall(Player player) {
        boolean success = false;
        Sign sign = getSurroundingElevatorSign(player);
        if (sign != null)
        {
            Elevator e = getElevator(sign);
            if (e != null)
            {
                e.Call(sign.getY());
                success = true;
            }
        }
        if (success) {
            player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator has been called");
        } else {
            player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "No Elevator in range");
        }
    }

    private void cmdStop(Player player) {
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
    }

    private void cmdStop2(Player player) {
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
        }
    }
}