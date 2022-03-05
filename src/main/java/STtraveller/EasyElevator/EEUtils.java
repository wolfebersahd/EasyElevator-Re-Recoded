package STtraveller.EasyElevator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class EEUtils {

    private final static String EE_SIGN_STRING = "[EElevator]";

    public static boolean isEESign(Sign s) {
        return s.getLine(0).equals(EE_SIGN_STRING) || s.getLine(0).equals(ChatColor.DARK_GRAY + EE_SIGN_STRING);
    }

    public static void playerMsg(Player p, String msg) {
        p.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + msg);
    }

    public static boolean isSign(Block b) {
        return b.getType().equals(Material.SPRUCE_SIGN) ||
                b.getType().equals(Material.OAK_SIGN) ||
                b.getType().equals(Material.JUNGLE_SIGN) ||
                b.getType().equals(Material.BIRCH_SIGN) ||
                b.getType().equals(Material.ACACIA_SIGN) ||
                b.getType().equals(Material.DARK_OAK_SIGN) ||
                b.getType().equals(Material.CRIMSON_SIGN) ||
                b.getType().equals(Material.WARPED_SIGN);
    }

    public static boolean isWallSign(Block b) {
        return b.getType().equals(Material.SPRUCE_WALL_SIGN) ||
                b.getType().equals(Material.OAK_WALL_SIGN) ||
                b.getType().equals(Material.JUNGLE_WALL_SIGN) ||
                b.getType().equals(Material.BIRCH_WALL_SIGN) ||
                b.getType().equals(Material.ACACIA_WALL_SIGN) ||
                b.getType().equals(Material.DARK_OAK_WALL_SIGN) ||
                b.getType().equals(Material.CRIMSON_WALL_SIGN) ||
                b.getType().equals(Material.WARPED_WALL_SIGN);
    }
}
