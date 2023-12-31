package noppes.npcs.command;

import java.util.Map.Entry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.CommandNoppesBase$SubCommand;
import noppes.npcs.entity.data.DataScenes;
import noppes.npcs.entity.data.DataScenes$SceneState;

public class CmdScene extends CommandNoppesBase {
   public String getName() {
      return "scene";
   }

   public String getDescription() {
      return "Scene operations";
   }

   @CommandNoppesBase$SubCommand(
      desc = "Get/Set scene time",
      usage = "[time] [name]",
      permission = 2
   )
   public void time(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendMessage(sender, "Active scenes:", new Object[0]);

         for(Entry<String, DataScenes$SceneState> entry : DataScenes.StartedScenes.entrySet()) {
            this.sendMessage(sender, "Scene %s time is %s", new Object[]{entry.getKey(), ((DataScenes$SceneState)entry.getValue()).ticks});
         }
      } else if (args.length == 1) {
         int ticks = Integer.parseInt(args[0]);

         for(DataScenes$SceneState state : DataScenes.StartedScenes.values()) {
            state.ticks = ticks;
         }

         this.sendMessage(sender, "All Scene times are set to " + ticks, new Object[0]);
      } else {
         DataScenes$SceneState state = (DataScenes$SceneState)DataScenes.StartedScenes.get(args[1].toLowerCase());
         if (state == null) {
            throw new CommandException("Unknown scene name %s", new Object[]{args[1]});
         }

         state.ticks = Integer.parseInt(args[0]);
         this.sendMessage(sender, "Scene %s set to %s", new Object[]{args[1], state.ticks});
      }

   }

   @CommandNoppesBase$SubCommand(
      desc = "Reset scene",
      usage = "[name]",
      permission = 2
   )
   public void reset(MinecraftServer server, ICommandSender sender, String[] args) {
      DataScenes.Reset(sender, args.length == 0 ? null : args[0]);
   }

   @CommandNoppesBase$SubCommand(
      desc = "Start scene",
      usage = "<name>",
      permission = 2
   )
   public void start(MinecraftServer server, ICommandSender sender, String[] args) {
      DataScenes.Start(sender, args[0]);
   }

   @CommandNoppesBase$SubCommand(
      desc = "Pause scene",
      usage = "[name]",
      permission = 2
   )
   public void pause(MinecraftServer server, ICommandSender sender, String[] args) {
      DataScenes.Pause(sender, args.length == 0 ? null : args[0]);
   }
}
