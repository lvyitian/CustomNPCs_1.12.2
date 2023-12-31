package noppes.npcs.command;

import java.lang.reflect.Method;
import java.util.Map.Entry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.CommandNoppesBase$SubCommand;

public class CmdHelp extends CommandNoppesBase {
   private CommandNoppes parent;

   public CmdHelp(CommandNoppes parent) {
      this.parent = parent;
   }

   public String getName() {
      return "help";
   }

   public String getDescription() {
      return "help [command]";
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendMessage(sender, "------Noppes Commands------", new Object[0]);

         for(Entry<String, CommandNoppesBase> entry : this.parent.map.entrySet()) {
            this.sendMessage(sender, (String)entry.getKey() + ": " + ((CommandNoppesBase)entry.getValue()).getUsage(sender), new Object[0]);
         }

      } else {
         CommandNoppesBase command = this.parent.getCommand(args);
         if (command == null) {
            throw new CommandException("Unknown command " + args[0], new Object[0]);
         } else if (command.subcommands.isEmpty()) {
            sender.sendMessage(new TextComponentTranslation(command.getUsage(sender), new Object[0]));
         } else {
            Method m = null;
            if (args.length > 1) {
               m = (Method)command.subcommands.get(args[1].toLowerCase());
            }

            if (m == null) {
               this.sendMessage(sender, "------" + command.getName() + " SubCommands------", new Object[0]);

               for(Entry<String, Method> entry : command.subcommands.entrySet()) {
                  sender.sendMessage(new TextComponentTranslation((String)entry.getKey() + ": " + ((CommandNoppesBase$SubCommand)((Method)entry.getValue()).getAnnotation(CommandNoppesBase$SubCommand.class)).desc(), new Object[0]));
               }
            } else {
               this.sendMessage(sender, "------" + command.getName() + "." + args[1].toLowerCase() + " Command------", new Object[0]);
               CommandNoppesBase$SubCommand sc = (CommandNoppesBase$SubCommand)m.getAnnotation(CommandNoppesBase$SubCommand.class);
               sender.sendMessage(new TextComponentTranslation(sc.desc(), new Object[0]));
               if (!sc.usage().isEmpty()) {
                  sender.sendMessage(new TextComponentTranslation("Usage: " + sc.usage(), new Object[0]));
               }
            }

         }
      }
   }
}
