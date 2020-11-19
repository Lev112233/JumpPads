import java.util.Set;
import me.Commands;
import me.JumpPad;
import me.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
  public Commands() {
    Main.plugin.getCommand("jumppads").setExecutor(this);
  }
  
  public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
    if (commandSender instanceof Player) {
      Player p = (Player)commandSender;
      if (args.length > 0)
        if (args[0].equalsIgnoreCase("info")) {
          if (args.length > 1) {
            JumpPad pad = JumpPad.getJumpPadByName(args[1]);
            if (pad != null) {
              pad.getInfo(p);
            } else {
              p.sendMessage(Main.chat("&cThat jump pad does not exist."));
            } 
            return true;
          } 
        } else if (args[0].equalsIgnoreCase("create")) {
          if (args.length > 1) {
            new JumpPad(p, null, args[1]);
            return true;
          } 
        } else if (args[0].equalsIgnoreCase("pos1")) {
          if (args.length > 1) {
            JumpPad pad = JumpPad.getJumpPadByName(args[1]);
            if (pad != null) {
              Set<Material> nullSet = null;
              Block block = p.getTargetBlock(nullSet, 5);
              if (block.getType().equals(Material.SLIME_BLOCK)) {
                pad.setPos1(block.getLocation());
                p.sendMessage(Main.chat("&aPos1 set!"));
              } else {
                p.sendMessage(Main.chat("&cYou must look at a slime block."));
              } 
            } else {
              p.sendMessage(Main.chat("&cThat jump pad does not exist."));
            } 
            return true;
          } 
        } else if (args[0].equalsIgnoreCase("pos2")) {
          if (args.length > 1) {
            JumpPad pad = JumpPad.getJumpPadByName(args[1]);
            if (pad != null) {
              Set<Material> nullSet = null;
              Block block = p.getTargetBlock(nullSet, 5);
              if (block.getType().equals(Material.SLIME_BLOCK)) {
                pad.setPos2(block.getLocation());
                p.sendMessage(Main.chat("&aPos2 set!"));
              } else {
                p.sendMessage(Main.chat("&cYou must look at a slime block."));
              } 
            } else {
              p.sendMessage(Main.chat("&cThat jump pad does not exist."));
            } 
            return true;
          } 
        } else {
          if (args[0].equalsIgnoreCase("list")) {
            p.sendMessage(Main.chat("&7Jump Pads List:"));
            JumpPad.getList(p);
            return true;
          } 
          if (args[0].equalsIgnoreCase("setFlyLocation")) {
            JumpPad pad = JumpPad.getJumpPadByName(args[1]);
            if (pad != null) {
              pad.setFlyLocation(p.getLocation());
              p.sendMessage(Main.chat("&aFly location set!"));
            } else {
              p.sendMessage(Main.chat("&cThat jump pad does not exist."));
            } 
            return true;
          } 
          if (args[0].equalsIgnoreCase("delete")) {
            JumpPad pad = JumpPad.getJumpPadByName(args[1]);
            if (pad != null) {
              pad.delete();
              p.sendMessage(Main.chat("&aJump pad deleted!"));
            } else {
              p.sendMessage(Main.chat("&cThat jump pad does not exist."));
            } 
            return true;
          } 
          if (args[0].equalsIgnoreCase("setPermission")) {
            if (args.length > 2) {
              JumpPad pad = JumpPad.getJumpPadByName(args[1]);
              if (pad != null) {
                pad.setPermission(args[1]);
                p.sendMessage(Main.chat("&aPermission set!"));
              } else {
                p.sendMessage(Main.chat("&cThat jump pad does not exist."));
              } 
              return true;
            } 
          } else if (args[0].equalsIgnoreCase("setPermissionMessage") && 
            args.length > 2) {
            JumpPad pad = JumpPad.getJumpPadByName(args[1]);
            if (pad != null) {
              pad.setPermissionMessage(args[2]);
              p.sendMessage(Main.chat("&aPermission message set!"));
            } else {
              p.sendMessage(Main.chat("&cThat jump pad does not exist."));
            } 
            return true;
          } 
        }  
      p.sendMessage(Main.chat("&a/jumppads info <padName>"));
      p.sendMessage(Main.chat("&a/jumppads list"));
      p.sendMessage(Main.chat("&a/jumppads setPermission <padName> <perm>"));
      p.sendMessage(Main.chat("&a/jumppads setPermissionMessage <padName> <permMessage>"));
      p.sendMessage(Main.chat("&a/jumppads pos1 <padName>"));
      p.sendMessage(Main.chat("&a/jumppads pos2 <padName>"));
      p.sendMessage(Main.chat("&a/jumppads setFlyLocation"));
      p.sendMessage(Main.chat("&a/jumppads delete <padName>"));
      p.sendMessage(Main.chat("&a/jumppads create <padName>"));
    } 
    return false;
  }
}
