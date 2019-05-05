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

        if (true) {
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
        if (true) {
            this.ee.getLogger().info("command reload activated");
        }
        this.ee.reloadConfig();
        for (Elevator e : this.ee.getElevators().getElevators()) {
            if (e.currentFloor != null) {
                e.currentFloor.switchRedstoneFloorOn(false);
            }
        }
        this.ee.getElevators().getElevators().clear();
        player.sendMessage(ChatColor.DARK_GRAY +
                "[EasyElevator] " + ChatColor.GRAY +
                "The plugin has been reloaded");
    }

    private void cmdCall(Player player) {
        if (true) {
            this.ee.getLogger().info("command call activated");
        }
        boolean success = false;
        Sign sign = this.ee.getElevators().getSurroundingElevatorSign(player);
        if (sign != null)
        {
            Elevator e = this.ee.getElevators().getElevator(sign);
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
        if (true) {
            this.ee.getLogger().info("command stop activated");
        }
        for (int i = 0; i < this.ee.getElevators().getElevators().size(); i++)
        {
            Elevator e = (Elevator)this.ee.getElevators().getElevators().get(i);
            if (e.isInElevator(player))
            {
                int target = e.getFloorNumberFromHeight(e.getNextFloorHeight_2());
                if (target != -1)
                {
                    e.addStops(target);
                    player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + target);
                    break;
                }
            }
        }
    }

    private void cmdStop2(Player player, String arg) {
        if (true) {
            this.ee.getLogger().info("command stop2 activated");
        }
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
                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Floor '" + target + "' is not in range");
                    }
                    e.addStops(target);
                    player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + target);
                    i = this.ee.getElevators().getElevators().size();
                }
            }
        }
        catch (Exception e)
        {
            player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Floor '" + arg + "' is not a valid value");
        }
    }
}