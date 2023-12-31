package noppes.npcs.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

public abstract class CommandNoppesBase extends CommandBase {
   public Map<String, Method> subcommands = new HashMap();

   public CommandNoppesBase() {
      for(Method m : this.getClass().getDeclaredMethods()) {
         CommandNoppesBase$SubCommand sc = (CommandNoppesBase$SubCommand)m.getAnnotation(CommandNoppesBase$SubCommand.class);
         if (sc != null) {
            String name = sc.name();
            if (name.equals("")) {
               name = m.getName();
            }

            this.subcommands.put(name.toLowerCase(), m);
         }
      }

   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
   }

   public String getUsage(ICommandSender sender) {
      return this.getDescription();
   }

   public abstract String getDescription();

   public String getUsage() {
      return "";
   }

   public boolean runSubCommands() {
      return !this.subcommands.isEmpty();
   }

   protected void sendMessage(ICommandSender sender, String message, Object... obs) {
      sender.sendMessage(new TextComponentTranslation(message, obs));
   }

   public void executeSub(MinecraftServer server, ICommandSender sender, String command, String[] args) throws CommandException {
      Method m = (Method)this.subcommands.get(command.toLowerCase());
      if (m == null) {
         throw new CommandException("Unknown subcommand " + command, new Object[0]);
      } else {
         CommandNoppesBase$SubCommand sc = (CommandNoppesBase$SubCommand)m.getAnnotation(CommandNoppesBase$SubCommand.class);
         if (!sender.canUseCommand(sc.permission(), "commands.noppes." + this.getName().toLowerCase() + "." + command.toLowerCase())) {
            throw new CommandException("You are not allowed to use this command", new Object[0]);
         } else {
            this.canRun(server, sender, sc.usage(), args);

            try {
               m.invoke(this, server, sender, args);
            } catch (Exception var8) {
               if (var8.getCause() instanceof CommandException) {
                  throw (CommandException)var8.getCause();
               }

               var8.printStackTrace();
            }

         }
      }
   }

   public void canRun(MinecraftServer server, ICommandSender sender, String usage, String[] args) throws CommandException {
      String[] np = usage.split(" ");
      List<String> required = new ArrayList();

      for(int i = 0; i < np.length; ++i) {
         String command = np[i];
         if (command.startsWith("<")) {
            required.add(command);
         }

         if (command.equals("<player>") && args.length > i) {
            CommandBase.getPlayer(server, sender, args[i]);
         }
      }

      if (args.length < required.size()) {
         throw new CommandException("Missing parameter: " + (String)required.get(args.length), new Object[0]);
      }
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }
}
