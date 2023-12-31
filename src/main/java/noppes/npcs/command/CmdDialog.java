package noppes.npcs.command;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.CommandNoppesBase$SubCommand;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityDialogNpc;

public class CmdDialog extends CommandNoppesBase {
   public String getName() {
      return "dialog";
   }

   public String getDescription() {
      return "Dialog operations";
   }

   @CommandNoppesBase$SubCommand(
      desc = "force read",
      usage = "<player> <dialog>",
      permission = 2
   )
   public void read(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      String playername = args[0];

      int diagid;
      try {
         diagid = Integer.parseInt(args[1]);
      } catch (NumberFormatException var9) {
         throw new CommandException("DialogID must be an integer", new Object[0]);
      }

      List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
      if (data.isEmpty()) {
         throw new CommandException("Unknow player '%s'", new Object[]{playername});
      } else {
         for(PlayerData playerdata : data) {
            playerdata.dialogData.dialogsRead.add(Integer.valueOf(diagid));
            playerdata.save(true);
         }

      }
   }

   @CommandNoppesBase$SubCommand(
      desc = "force unread dialog",
      usage = "<player> <dialog>",
      permission = 2
   )
   public void unread(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      String playername = args[0];

      int diagid;
      try {
         diagid = Integer.parseInt(args[1]);
      } catch (NumberFormatException var9) {
         throw new CommandException("DialogID must be an integer", new Object[0]);
      }

      List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
      if (data.isEmpty()) {
         throw new CommandException("Unknow player '%s'", new Object[]{playername});
      } else {
         for(PlayerData playerdata : data) {
            playerdata.dialogData.dialogsRead.remove(Integer.valueOf(diagid));
            playerdata.save(true);
         }

      }
   }

   @CommandNoppesBase$SubCommand(
      desc = "reload dialogs from disk",
      permission = 4
   )
   public void reload(MinecraftServer server, ICommandSender sender, String[] args) {
      (new DialogController()).load();
      SyncController.syncAllDialogs(server);
   }

   @CommandNoppesBase$SubCommand(
      desc = "show dialog",
      usage = "<player> <dialog> <name>",
      permission = 2
   )
   public void show(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      List<EntityPlayerMP> players = CommandBase.getPlayers(server, sender, args[0]);
      if (players == null) {
         throw new CommandException("Unknow player '%s'", new Object[]{args[0]});
      } else {
         int diagid;
         try {
            diagid = Integer.parseInt(args[1]);
         } catch (NumberFormatException var11) {
            throw new CommandException("DialogID must be an integer: " + args[1], new Object[0]);
         }

         Dialog dialog = (Dialog)DialogController.instance.dialogs.get(Integer.valueOf(diagid));
         if (dialog == null) {
            throw new CommandException("Unknown dialog id: " + args[1], new Object[0]);
         } else {
            EntityDialogNpc npc = new EntityDialogNpc(sender.getEntityWorld());
            DialogOption option = new DialogOption();
            option.dialogId = diagid;
            option.title = dialog.title;
            npc.dialogs.put(Integer.valueOf(0), option);
            npc.display.setName(args[2]);

            for(EntityPlayer player : players) {
               EntityUtil.Copy(player, npc);
               NoppesUtilServer.openDialog(player, npc, dialog);
            }

         }
      }
   }
}
