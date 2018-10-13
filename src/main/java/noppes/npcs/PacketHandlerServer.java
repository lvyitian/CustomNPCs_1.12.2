package noppes.npcs;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.LinkedNpcController$LinkedData;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScenes;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.IPermission;

public class PacketHandlerServer {
   @SubscribeEvent
   public void onServerPacket(ServerCustomPacketEvent event) {
      EntityPlayerMP player = ((NetHandlerPlayServer)event.getHandler()).player;
      if (CustomNpcs.OpsOnly && !NoppesUtilServer.isOp(player)) {
         this.warn(player, "tried to use custom npcs without being an op");
      } else {
         ByteBuf buffer = event.getPacket().payload();
         player.getServer().addScheduledTask(new PacketHandlerServer$1(this, buffer, player));
      }
   }

   private boolean allowItem(ItemStack stack, EnumPacketServer type) {
      if (stack != null && stack.getItem() != null) {
         Item item = stack.getItem();
         IPermission permission = null;
         if (item instanceof IPermission) {
            permission = (IPermission)item;
         } else if (item instanceof ItemBlock && ((ItemBlock)item).getBlock() instanceof IPermission) {
            permission = (IPermission)((ItemBlock)item).getBlock();
         }

         return permission != null && permission.isAllowed(type);
      } else {
         return false;
      }
   }

   private void handlePacket(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws Exception {
      if (type == EnumPacketServer.Delete) {
         npc.delete();
         NoppesUtilServer.deleteNpc(npc, player);
      } else if (type == EnumPacketServer.SceneStart) {
         DataScenes.Toggle(player, buffer.readInt() + "btn");
      } else if (type == EnumPacketServer.SceneReset) {
         DataScenes.Reset(player, (String)null);
      } else if (type == EnumPacketServer.LinkedAdd) {
         LinkedNpcController.Instance.addData(Server.readString(buffer));
         List<String> list = new ArrayList();

         for(LinkedNpcController$LinkedData data : LinkedNpcController.Instance.list) {
            list.add(data.name);
         }

         Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
      } else if (type == EnumPacketServer.LinkedRemove) {
         LinkedNpcController.Instance.removeData(Server.readString(buffer));
         List<String> list = new ArrayList();

         for(LinkedNpcController$LinkedData data : LinkedNpcController.Instance.list) {
            list.add(data.name);
         }

         Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
      } else if (type == EnumPacketServer.LinkedGetAll) {
         List<String> list = new ArrayList();

         for(LinkedNpcController$LinkedData data : LinkedNpcController.Instance.list) {
            list.add(data.name);
         }

         Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
         if (npc != null) {
            Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, npc.linkedName);
         }
      } else if (type == EnumPacketServer.LinkedSet) {
         npc.linkedName = Server.readString(buffer);
         LinkedNpcController.Instance.loadNpcData(npc);
      } else if (type == EnumPacketServer.NpcMenuClose) {
         npc.reset();
         if (npc.linkedData != null) {
            LinkedNpcController.Instance.saveNpcData(npc);
         }

         NoppesUtilServer.setEditingNpc(player, (EntityNPCInterface)null);
      } else if (type == EnumPacketServer.BanksGet) {
         NoppesUtilServer.sendBankDataAll(player);
      } else if (type == EnumPacketServer.BankGet) {
         Bank bank = BankController.getInstance().getBank(buffer.readInt());
         NoppesUtilServer.sendBank(player, bank);
      } else if (type == EnumPacketServer.BankSave) {
         Bank bank = new Bank();
         bank.readEntityFromNBT(Server.readNBT(buffer));
         BankController.getInstance().saveBank(bank);
         NoppesUtilServer.sendBankDataAll(player);
         NoppesUtilServer.sendBank(player, bank);
      } else if (type == EnumPacketServer.BankRemove) {
         BankController.getInstance().removeBank(buffer.readInt());
         NoppesUtilServer.sendBankDataAll(player);
         NoppesUtilServer.sendBank(player, new Bank());
      } else if (type == EnumPacketServer.RemoteMainMenu) {
         Entity entity = player.world.getEntityByID(buffer.readInt());
         if (entity == null || !(entity instanceof EntityNPCInterface)) {
            return;
         }

         NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, (EntityNPCInterface)entity);
      } else if (type == EnumPacketServer.RemoteDelete) {
         Entity entity = player.world.getEntityByID(buffer.readInt());
         if (entity == null || !(entity instanceof EntityNPCInterface)) {
            return;
         }

         npc = (EntityNPCInterface)entity;
         npc.delete();
         NoppesUtilServer.deleteNpc(npc, player);
         NoppesUtilServer.sendNearbyNpcs(player);
      } else if (type == EnumPacketServer.RemoteNpcsGet) {
         NoppesUtilServer.sendNearbyNpcs(player);
         Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, CustomNpcs.FreezeNPCs ? "Unfreeze Npcs" : "Freeze Npcs");
      } else if (type == EnumPacketServer.RemoteFreeze) {
         CustomNpcs.FreezeNPCs = !CustomNpcs.FreezeNPCs;
         Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, CustomNpcs.FreezeNPCs ? "Unfreeze Npcs" : "Freeze Npcs");
      } else if (type == EnumPacketServer.RemoteReset) {
         Entity entity = player.world.getEntityByID(buffer.readInt());
         if (entity == null || !(entity instanceof EntityNPCInterface)) {
            return;
         }

         npc = (EntityNPCInterface)entity;
         npc.reset();
      } else if (type == EnumPacketServer.RemoteTpToNpc) {
         Entity entity = player.world.getEntityByID(buffer.readInt());
         if (entity == null || !(entity instanceof EntityNPCInterface)) {
            return;
         }

         npc = (EntityNPCInterface)entity;
         player.connection.setPlayerLocation(npc.posX, npc.posY, npc.posZ, 0.0F, 0.0F);
      } else if (type == EnumPacketServer.Gui) {
         EnumGuiType gui = EnumGuiType.values()[buffer.readInt()];
         int i = buffer.readInt();
         int j = buffer.readInt();
         int k = buffer.readInt();
         NoppesUtilServer.sendOpenGui(player, gui, npc, i, j, k);
      } else if (type == EnumPacketServer.RecipesGet) {
         NoppesUtilServer.sendRecipeData(player, buffer.readInt());
      } else if (type == EnumPacketServer.RecipeGet) {
         RecipeCarpentry recipe = RecipeController.instance.getRecipe(buffer.readInt());
         NoppesUtilServer.setRecipeGui(player, recipe);
      } else if (type == EnumPacketServer.RecipeRemove) {
         RecipeCarpentry recipe = RecipeController.instance.delete(buffer.readInt());
         NoppesUtilServer.sendRecipeData(player, recipe.isGlobal ? 3 : 4);
         NoppesUtilServer.setRecipeGui(player, new RecipeCarpentry(""));
      } else if (type == EnumPacketServer.RecipeSave) {
         RecipeCarpentry recipe = RecipeCarpentry.read(Server.readNBT(buffer));
         RecipeController.instance.saveRecipe(recipe);
         NoppesUtilServer.sendRecipeData(player, recipe.isGlobal ? 3 : 4);
         NoppesUtilServer.setRecipeGui(player, recipe);
      } else if (type == EnumPacketServer.NaturalSpawnGetAll) {
         NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
      } else if (type == EnumPacketServer.NaturalSpawnGet) {
         SpawnData spawn = SpawnController.instance.getSpawnData(buffer.readInt());
         if (spawn != null) {
            Server.sendData(player, EnumPacketClient.GUI_DATA, spawn.writeNBT(new NBTTagCompound()));
         }
      } else if (type == EnumPacketServer.NaturalSpawnSave) {
         SpawnData data = new SpawnData();
         data.readNBT(Server.readNBT(buffer));
         SpawnController.instance.saveSpawnData(data);
         NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
      } else if (type == EnumPacketServer.NaturalSpawnRemove) {
         SpawnController.instance.removeSpawnData(buffer.readInt());
         NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
      } else if (type == EnumPacketServer.DialogCategorySave) {
         DialogCategory category = new DialogCategory();
         category.readNBT(Server.readNBT(buffer));
         DialogController.instance.saveCategory(category);
         NoppesUtilServer.sendScrollData(player, DialogController.instance.getScroll());
      } else if (type == EnumPacketServer.DialogCategoryRemove) {
         DialogController.instance.removeCategory(buffer.readInt());
         NoppesUtilServer.sendScrollData(player, DialogController.instance.getScroll());
      } else if (type == EnumPacketServer.DialogCategoryGet) {
         DialogCategory category = (DialogCategory)DialogController.instance.categories.get(Integer.valueOf(buffer.readInt()));
         if (category != null) {
            NBTTagCompound comp = category.writeNBT(new NBTTagCompound());
            comp.removeTag("Dialogs");
            Server.sendData(player, EnumPacketClient.GUI_DATA, comp);
         }
      } else if (type == EnumPacketServer.DialogSave) {
         DialogCategory category = (DialogCategory)DialogController.instance.categories.get(Integer.valueOf(buffer.readInt()));
         if (category == null) {
            return;
         }

         Dialog dialog = new Dialog(category);
         dialog.readNBT(Server.readNBT(buffer));
         DialogController.instance.saveDialog(category, dialog);
         NoppesUtilServer.sendDialogData(player, dialog.category);
      } else if (type == EnumPacketServer.QuestOpenGui) {
         Quest quest = new Quest((QuestCategory)null);
         int gui = buffer.readInt();
         quest.readNBT(Server.readNBT(buffer));
         NoppesUtilServer.setEditingQuest(player, quest);
         player.openGui(CustomNpcs.instance, gui, player.world, 0, 0, 0);
      } else if (type == EnumPacketServer.DialogRemove) {
         Dialog dialog = (Dialog)DialogController.instance.dialogs.get(Integer.valueOf(buffer.readInt()));
         if (dialog != null && dialog.category != null) {
            DialogController.instance.removeDialog(dialog);
            NoppesUtilServer.sendDialogData(player, dialog.category);
         }
      } else if (type == EnumPacketServer.DialogNpcGet) {
         NoppesUtilServer.sendNpcDialogs(player);
      } else if (type == EnumPacketServer.DialogNpcSet) {
         int slot = buffer.readInt();
         int dialog = buffer.readInt();
         DialogOption option = NoppesUtilServer.setNpcDialog(slot, dialog, player);
         if (option != null && option.hasDialog()) {
            NBTTagCompound compound = option.writeNBT();
            compound.setInteger("Position", slot);
            Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
         }
      } else if (type == EnumPacketServer.DialogNpcRemove) {
         npc.dialogs.remove(Integer.valueOf(buffer.readInt()));
      } else if (type == EnumPacketServer.QuestCategoryGet) {
         QuestCategory category = (QuestCategory)QuestController.instance.categories.get(Integer.valueOf(buffer.readInt()));
         if (category != null) {
            NBTTagCompound comp = category.writeNBT(new NBTTagCompound());
            comp.removeTag("Dialogs");
            Server.sendData(player, EnumPacketClient.GUI_DATA, comp);
         }
      } else if (type == EnumPacketServer.QuestCategorySave) {
         QuestCategory category = new QuestCategory();
         category.readNBT(Server.readNBT(buffer));
         QuestController.instance.saveCategory(category);
         NoppesUtilServer.sendQuestCategoryData(player);
      } else if (type == EnumPacketServer.QuestCategoryRemove) {
         QuestController.instance.removeCategory(buffer.readInt());
         NoppesUtilServer.sendQuestCategoryData(player);
      } else if (type == EnumPacketServer.QuestSave) {
         QuestCategory category = (QuestCategory)QuestController.instance.categories.get(Integer.valueOf(buffer.readInt()));
         if (category == null) {
            return;
         }

         Quest quest = new Quest(category);
         quest.readNBT(Server.readNBT(buffer));
         QuestController.instance.saveQuest(category, quest);
         NoppesUtilServer.sendQuestData(player, quest.category);
      } else if (type == EnumPacketServer.QuestDialogGetTitle) {
         Dialog quest = (Dialog)DialogController.instance.dialogs.get(Integer.valueOf(buffer.readInt()));
         Dialog quest2 = (Dialog)DialogController.instance.dialogs.get(Integer.valueOf(buffer.readInt()));
         Dialog quest3 = (Dialog)DialogController.instance.dialogs.get(Integer.valueOf(buffer.readInt()));
         NBTTagCompound compound = new NBTTagCompound();
         if (quest != null) {
            compound.setString("1", quest.title);
         }

         if (quest2 != null) {
            compound.setString("2", quest2.title);
         }

         if (quest3 != null) {
            compound.setString("3", quest3.title);
         }

         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.QuestRemove) {
         Quest quest = (Quest)QuestController.instance.quests.get(Integer.valueOf(buffer.readInt()));
         if (quest != null) {
            QuestController.instance.removeQuest(quest);
            NoppesUtilServer.sendQuestData(player, quest.category);
         }
      } else if (type == EnumPacketServer.TransportCategoriesGet) {
         NoppesUtilServer.sendTransportCategoryData(player);
      } else if (type == EnumPacketServer.TransportCategorySave) {
         TransportController.getInstance().saveCategory(Server.readString(buffer), buffer.readInt());
      } else if (type == EnumPacketServer.TransportCategoryRemove) {
         TransportController.getInstance().removeCategory(buffer.readInt());
         NoppesUtilServer.sendTransportCategoryData(player);
      } else if (type == EnumPacketServer.TransportRemove) {
         int id = buffer.readInt();
         TransportLocation loc = TransportController.getInstance().removeLocation(id);
         if (loc != null) {
            NoppesUtilServer.sendTransportData(player, loc.category.id);
         }
      } else if (type == EnumPacketServer.TransportsGet) {
         NoppesUtilServer.sendTransportData(player, buffer.readInt());
      } else if (type == EnumPacketServer.TransportSave) {
         int cat = buffer.readInt();
         TransportLocation location = TransportController.getInstance().saveLocation(cat, Server.readNBT(buffer), player, npc);
         if (location != null) {
            if (npc.advanced.role != 4) {
               return;
            }

            RoleTransporter role = (RoleTransporter)npc.roleInterface;
            role.setTransport(location);
         }
      } else if (type == EnumPacketServer.TransportGetLocation) {
         if (npc.advanced.role != 4) {
            return;
         }

         RoleTransporter role = (RoleTransporter)npc.roleInterface;
         if (role.hasTransport()) {
            Server.sendData(player, EnumPacketClient.GUI_DATA, role.getLocation().writeNBT());
            Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, role.getLocation().category.title);
         }
      } else if (type == EnumPacketServer.FactionSet) {
         npc.setFaction(buffer.readInt());
      } else if (type == EnumPacketServer.FactionSave) {
         Faction faction = new Faction();
         faction.readNBT(Server.readNBT(buffer));
         FactionController.instance.saveFaction(faction);
         NoppesUtilServer.sendFactionDataAll(player);
         NBTTagCompound compound = new NBTTagCompound();
         faction.writeNBT(compound);
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.FactionRemove) {
         FactionController.instance.delete(buffer.readInt());
         NoppesUtilServer.sendFactionDataAll(player);
         NBTTagCompound compound = new NBTTagCompound();
         (new Faction()).writeNBT(compound);
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.PlayerDataGet) {
         int id = buffer.readInt();
         if (EnumPlayerData.values().length <= id) {
            return;
         }

         String name = null;
         EnumPlayerData datatype = EnumPlayerData.values()[id];
         if (datatype != EnumPlayerData.Players) {
            name = Server.readString(buffer);
         }

         NoppesUtilServer.sendPlayerData(datatype, player, name);
      } else if (type == EnumPacketServer.PlayerDataRemove) {
         NoppesUtilServer.removePlayerData(buffer, player);
      } else if (type == EnumPacketServer.MainmenuDisplayGet) {
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.display.writeToNBT(new NBTTagCompound()));
      } else if (type == EnumPacketServer.MainmenuDisplaySave) {
         npc.display.readToNBT(Server.readNBT(buffer));
         npc.updateClient = true;
      } else if (type == EnumPacketServer.MainmenuStatsGet) {
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.stats.writeToNBT(new NBTTagCompound()));
      } else if (type == EnumPacketServer.MainmenuStatsSave) {
         npc.stats.readToNBT(Server.readNBT(buffer));
         npc.updateClient = true;
      } else if (type == EnumPacketServer.MainmenuInvGet) {
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
      } else if (type == EnumPacketServer.MainmenuInvSave) {
         npc.inventory.readEntityFromNBT(Server.readNBT(buffer));
         npc.updateAI = true;
         npc.updateClient = true;
      } else if (type == EnumPacketServer.MainmenuAIGet) {
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.ais.writeToNBT(new NBTTagCompound()));
      } else if (type == EnumPacketServer.MainmenuAISave) {
         npc.ais.readToNBT(Server.readNBT(buffer));
         npc.setHealth(npc.getMaxHealth());
         npc.updateAI = true;
         npc.updateClient = true;
      } else if (type == EnumPacketServer.MainmenuAdvancedGet) {
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.advanced.writeToNBT(new NBTTagCompound()));
      } else if (type == EnumPacketServer.MainmenuAdvancedSave) {
         npc.advanced.readToNBT(Server.readNBT(buffer));
         npc.updateAI = true;
         npc.updateClient = true;
      } else if (type == EnumPacketServer.MainmenuAdvancedMarkData) {
         MarkData data = MarkData.get(npc);
         data.setNBT(Server.readNBT(buffer));
         data.syncClients();
      } else if (type == EnumPacketServer.JobSave) {
         NBTTagCompound original = npc.jobInterface.writeToNBT(new NBTTagCompound());
         NBTTagCompound compound = Server.readNBT(buffer);

         for(String name : compound.getKeySet()) {
            original.setTag(name, compound.getTag(name));
         }

         npc.jobInterface.readFromNBT(original);
         npc.updateClient = true;
      } else if (type == EnumPacketServer.JobGet) {
         if (npc.jobInterface == null) {
            return;
         }

         NBTTagCompound compound = new NBTTagCompound();
         compound.setBoolean("JobData", true);
         npc.jobInterface.writeToNBT(compound);
         if (npc.advanced.job == 6) {
            ((JobSpawner)npc.jobInterface).cleanCompound(compound);
         }

         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
         if (npc.advanced.job == 6) {
            Server.sendData(player, EnumPacketClient.GUI_DATA, ((JobSpawner)npc.jobInterface).getTitles());
         }
      } else if (type == EnumPacketServer.JobSpawnerAdd) {
         if (npc.advanced.job != 6) {
            return;
         }

         JobSpawner job = (JobSpawner)npc.jobInterface;
         if (buffer.readBoolean()) {
            NBTTagCompound compound = ServerCloneController.Instance.getCloneData((ICommandSender)null, Server.readString(buffer), buffer.readInt());
            job.setJobCompound(buffer.readInt(), compound);
         } else {
            job.setJobCompound(buffer.readInt(), Server.readNBT(buffer));
         }

         Server.sendData(player, EnumPacketClient.GUI_DATA, job.getTitles());
      } else if (type == EnumPacketServer.RoleCompanionUpdate) {
         if (npc.advanced.role != 6) {
            return;
         }

         ((RoleCompanion)npc.roleInterface).matureTo(EnumCompanionStage.values()[buffer.readInt()]);
         npc.updateClient = true;
      } else if (type == EnumPacketServer.JobSpawnerRemove) {
         if (npc.advanced.job != 6) {
            return;
         }
      } else if (type == EnumPacketServer.RoleSave) {
         npc.roleInterface.readFromNBT(Server.readNBT(buffer));
         npc.updateClient = true;
      } else if (type == EnumPacketServer.RoleGet) {
         if (npc.roleInterface == null) {
            return;
         }

         NBTTagCompound compound = new NBTTagCompound();
         compound.setBoolean("RoleData", true);
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.roleInterface.writeToNBT(compound));
      } else if (type == EnumPacketServer.MerchantUpdate) {
         Entity entity = player.world.getEntityByID(buffer.readInt());
         if (entity == null || !(entity instanceof EntityVillager)) {
            return;
         }

         MerchantRecipeList list = MerchantRecipeList.readFromBuf(new PacketBuffer(buffer));
         ((EntityVillager)entity).setRecipes(list);
      } else if (type == EnumPacketServer.ModelDataSave) {
         if (npc instanceof EntityCustomNpc) {
            ((EntityCustomNpc)npc).modelData.readFromNBT(Server.readNBT(buffer));
         }
      } else if (type == EnumPacketServer.MailOpenSetup) {
         PlayerMail mail = new PlayerMail();
         mail.readNBT(Server.readNBT(buffer));
         ContainerMail.staticmail = mail;
         player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), player.world, 1, 0, 0);
      } else if (type == EnumPacketServer.TransformSave) {
         boolean isValid = npc.transform.isValid();
         npc.transform.readOptions(Server.readNBT(buffer));
         if (isValid != npc.transform.isValid()) {
            npc.updateAI = true;
         }
      } else if (type == EnumPacketServer.TransformGet) {
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.transform.writeOptions(new NBTTagCompound()));
      } else if (type == EnumPacketServer.TransformLoad) {
         if (npc.transform.isValid()) {
            npc.transform.transform(buffer.readBoolean());
         }
      } else if (type == EnumPacketServer.TraderMarketSave) {
         String market = Server.readString(buffer);
         boolean bo = buffer.readBoolean();
         if (npc.roleInterface instanceof RoleTrader) {
            if (bo) {
               RoleTrader.setMarket(npc, market);
            } else {
               RoleTrader.save((RoleTrader)npc.roleInterface, market);
            }
         }
      } else if (type == EnumPacketServer.MovingPathGet) {
         Server.sendData(player, EnumPacketClient.GUI_DATA, npc.ais.writeToNBT(new NBTTagCompound()));
      } else if (type == EnumPacketServer.MovingPathSave) {
         npc.ais.setMovingPath(NBTTags.getIntegerArraySet(Server.readNBT(buffer).getTagList("MovingPathNew", 10)));
      } else if (type == EnumPacketServer.SpawnRider) {
         Entity entity = EntityList.createEntityFromNBT(Server.readNBT(buffer), player.world);
         player.world.spawnEntity(entity);
         entity.startRiding(ServerEventsHandler.mounted, true);
      } else if (type == EnumPacketServer.PlayerRider) {
         player.startRiding(ServerEventsHandler.mounted, true);
      } else if (type == EnumPacketServer.SpawnMob) {
         boolean server = buffer.readBoolean();
         int x = buffer.readInt();
         int y = buffer.readInt();
         int z = buffer.readInt();
         NBTTagCompound compound;
         if (server) {
            compound = ServerCloneController.Instance.getCloneData(player, Server.readString(buffer), buffer.readInt());
         } else {
            compound = Server.readNBT(buffer);
         }

         if (compound == null) {
            return;
         }

         Entity entity = NoppesUtilServer.spawnClone(compound, (double)x + 0.5D, (double)(y + 1), (double)z + 0.5D, player.world);
         if (entity == null) {
            player.sendMessage(new TextComponentString("Failed to create an entity out of your clone"));
            return;
         }
      } else if (type == EnumPacketServer.MobSpawner) {
         boolean server = buffer.readBoolean();
         BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
         NBTTagCompound compound;
         if (server) {
            compound = ServerCloneController.Instance.getCloneData(player, Server.readString(buffer), buffer.readInt());
         } else {
            compound = Server.readNBT(buffer);
         }

         if (compound != null) {
            NoppesUtilServer.createMobSpawner(pos, compound, player);
         }
      } else if (type == EnumPacketServer.ClonePreSave) {
         boolean bo = ServerCloneController.Instance.getCloneData((ICommandSender)null, Server.readString(buffer), buffer.readInt()) != null;
         NBTTagCompound compound = new NBTTagCompound();
         compound.setBoolean("NameExists", bo);
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.CloneSave) {
         PlayerData data = PlayerData.get(player);
         if (data.cloned == null) {
            return;
         }

         ServerCloneController.Instance.addClone(data.cloned, Server.readString(buffer), buffer.readInt());
      } else if (type == EnumPacketServer.CloneRemove) {
         int tab = buffer.readInt();
         ServerCloneController.Instance.removeClone(Server.readString(buffer), tab);
         NBTTagList list = new NBTTagList();

         for(String name : ServerCloneController.Instance.getClones(tab)) {
            list.appendTag(new NBTTagString(name));
         }

         NBTTagCompound compound = new NBTTagCompound();
         compound.setTag("List", list);
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.CloneList) {
         NBTTagList list = new NBTTagList();

         for(String name : ServerCloneController.Instance.getClones(buffer.readInt())) {
            list.appendTag(new NBTTagString(name));
         }

         NBTTagCompound compound = new NBTTagCompound();
         compound.setTag("List", list);
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.ScriptDataSave) {
         npc.script.readFromNBT(Server.readNBT(buffer));
         npc.updateAI = true;
         npc.script.lastInited = -1L;
      } else if (type == EnumPacketServer.ScriptDataGet) {
         NBTTagCompound compound = npc.script.writeToNBT(new NBTTagCompound());
         compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.DimensionsGet) {
         HashMap<String, Integer> map = new HashMap();
         Integer[] var108 = DimensionManager.getStaticDimensionIDs();
         int var139 = var108.length;

         for(int var152 = 0; var152 < var139; ++var152) {
            int id = var108[var152].intValue();
            WorldProvider provider = DimensionManager.createProviderFor(id);
            map.put(provider.getDimensionType().getName(), Integer.valueOf(id));
         }

         NoppesUtilServer.sendScrollData(player, map);
      } else if (type == EnumPacketServer.DimensionTeleport) {
         int dimension = buffer.readInt();
         WorldServer world = player.getServer().getWorld(dimension);
         BlockPos coords = world.getSpawnCoordinate();
         if (coords == null) {
            coords = world.getSpawnPoint();
            if (!world.isAirBlock(coords)) {
               coords = world.getTopSolidOrLiquidBlock(coords);
            } else {
               while(world.isAirBlock(coords) && coords.getY() > 0) {
                  coords = coords.down();
               }

               if (coords.getY() == 0) {
                  coords = world.getTopSolidOrLiquidBlock(coords);
               }
            }
         }

         NoppesUtilPlayer.teleportPlayer(player, (double)coords.getX(), (double)coords.getY(), (double)coords.getZ(), dimension);
      } else if (type == EnumPacketServer.ScriptBlockDataGet) {
         TileEntity tile = player.world.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
         if (!(tile instanceof TileScripted)) {
            return;
         }

         NBTTagCompound compound = ((TileScripted)tile).getNBT(new NBTTagCompound());
         compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.ScriptItemDataGet) {
         ItemScriptedWrapper iw = (ItemScriptedWrapper)NpcAPI.Instance().getIItemStack(player.getHeldItemMainhand());
         NBTTagCompound compound = iw.getNBT();
         compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.ScriptItemDataSave) {
         if (!player.isCreative()) {
            return;
         }

         NBTTagCompound compound = Server.readNBT(buffer);
         ItemStack item = player.getHeldItemMainhand();
         ItemScriptedWrapper wrapper = (ItemScriptedWrapper)NpcAPI.Instance().getIItemStack(player.getHeldItemMainhand());
         wrapper.setNBT(compound);
         wrapper.lastInited = -1L;
         wrapper.saveScriptData();
         wrapper.updateClient = true;
         player.sendContainerToPlayer(player.inventoryContainer);
      } else if (type == EnumPacketServer.ScriptForgeGet) {
         ForgeScriptData data = ScriptController.Instance.forgeScripts;
         NBTTagCompound compound = data.writeToNBT(new NBTTagCompound());
         compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.ScriptForgeSave) {
         ScriptController.Instance.setForgeScripts(Server.readNBT(buffer));
      } else if (type == EnumPacketServer.ScriptPlayerGet) {
         NBTTagCompound compound = ScriptController.Instance.playerScripts.writeToNBT(new NBTTagCompound());
         compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.ScriptPlayerSave) {
         ScriptController.Instance.setPlayerScripts(Server.readNBT(buffer));
      } else if (type == EnumPacketServer.DialogCategoriesGet) {
         NoppesUtilServer.sendScrollData(player, DialogController.instance.getScroll());
      } else if (type == EnumPacketServer.DialogsGetFromDialog) {
         Dialog dialog = (Dialog)DialogController.instance.dialogs.get(Integer.valueOf(buffer.readInt()));
         if (dialog == null) {
            return;
         }

         NoppesUtilServer.sendDialogData(player, dialog.category);
      } else if (type == EnumPacketServer.DialogsGet) {
         NoppesUtilServer.sendDialogData(player, (DialogCategory)DialogController.instance.categories.get(Integer.valueOf(buffer.readInt())));
      } else if (type == EnumPacketServer.QuestsGetFromQuest) {
         Quest quest = (Quest)QuestController.instance.quests.get(Integer.valueOf(buffer.readInt()));
         if (quest == null) {
            return;
         }

         NoppesUtilServer.sendQuestData(player, quest.category);
      } else if (type == EnumPacketServer.QuestCategoriesGet) {
         NoppesUtilServer.sendQuestCategoryData(player);
      } else if (type == EnumPacketServer.QuestsGet) {
         QuestCategory category = (QuestCategory)QuestController.instance.categories.get(Integer.valueOf(buffer.readInt()));
         NoppesUtilServer.sendQuestData(player, category);
      } else if (type == EnumPacketServer.FactionsGet) {
         NoppesUtilServer.sendFactionDataAll(player);
      } else if (type == EnumPacketServer.DialogGet) {
         Dialog dialog = (Dialog)DialogController.instance.dialogs.get(Integer.valueOf(buffer.readInt()));
         if (dialog != null) {
            NBTTagCompound compound = dialog.writeToNBT(new NBTTagCompound());
            Quest quest = (Quest)QuestController.instance.quests.get(Integer.valueOf(dialog.quest));
            if (quest != null) {
               compound.setString("DialogQuestName", quest.title);
            }

            Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
         }
      } else if (type == EnumPacketServer.QuestGet) {
         Quest quest = (Quest)QuestController.instance.quests.get(Integer.valueOf(buffer.readInt()));
         if (quest != null) {
            NBTTagCompound compound = new NBTTagCompound();
            if (quest.hasNewQuest()) {
               compound.setString("NextQuestTitle", quest.getNextQuest().title);
            }

            Server.sendData(player, EnumPacketClient.GUI_DATA, quest.writeToNBT(compound));
         }
      } else if (type == EnumPacketServer.FactionGet) {
         NBTTagCompound compound = new NBTTagCompound();
         Faction faction = FactionController.instance.getFaction(buffer.readInt());
         faction.writeNBT(compound);
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.SaveTileEntity) {
         NoppesUtilServer.saveTileEntity(player, Server.readNBT(buffer));
      } else if (type == EnumPacketServer.GetTileEntity) {
         BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
         TileEntity tile = player.world.getTileEntity(pos);
         NBTTagCompound compound = new NBTTagCompound();
         tile.writeToNBT(compound);
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.ScriptBlockDataSave) {
         TileEntity tile = player.world.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
         if (!(tile instanceof TileScripted)) {
            return;
         }

         TileScripted script = (TileScripted)tile;
         script.setNBT(Server.readNBT(buffer));
         script.lastInited = -1L;
      } else if (type == EnumPacketServer.ScriptDoorDataSave) {
         TileEntity tile = player.world.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
         if (!(tile instanceof TileScriptedDoor)) {
            return;
         }

         TileScriptedDoor script = (TileScriptedDoor)tile;
         script.setNBT(Server.readNBT(buffer));
         script.lastInited = -1L;
      } else if (type == EnumPacketServer.ScriptDoorDataGet) {
         TileEntity tile = player.world.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
         if (!(tile instanceof TileScriptedDoor)) {
            return;
         }

         NBTTagCompound compound = ((TileScriptedDoor)tile).getNBT(new NBTTagCompound());
         compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
         Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
      } else if (type == EnumPacketServer.SchematicsTile) {
         BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
         TileBuilder tile = (TileBuilder)player.world.getTileEntity(pos);
         if (tile == null) {
            return;
         }

         Server.sendData(player, EnumPacketClient.GUI_DATA, tile.writePartNBT(new NBTTagCompound()));
         Server.sendData(player, EnumPacketClient.SCROLL_LIST, SchematicController.Instance.list());
         if (tile.hasSchematic()) {
            Server.sendData(player, EnumPacketClient.GUI_DATA, tile.getSchematic().getNBTSmall());
         }
      } else if (type == EnumPacketServer.SchematicsSet) {
         BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
         TileBuilder tile = (TileBuilder)player.world.getTileEntity(pos);
         String name = Server.readString(buffer);
         tile.setSchematic(SchematicController.Instance.load(name));
         if (tile.hasSchematic()) {
            Server.sendData(player, EnumPacketClient.GUI_DATA, tile.getSchematic().getNBTSmall());
         }
      } else if (type == EnumPacketServer.SchematicsBuild) {
         BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
         TileBuilder tile = (TileBuilder)player.world.getTileEntity(pos);
         SchematicWrapper schem = tile.getSchematic();
         schem.init(pos.add(1, tile.yOffest, 1), player.world, tile.rotation);
         SchematicController.Instance.build(tile.getSchematic(), player);
         player.world.setBlockToAir(pos);
      } else if (type == EnumPacketServer.SchematicsTileSave) {
         BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
         TileBuilder tile = (TileBuilder)player.world.getTileEntity(pos);
         if (tile != null) {
            tile.readPartNBT(Server.readNBT(buffer));
         }
      } else if (type == EnumPacketServer.SchematicStore) {
         String name = Server.readString(buffer);
         int t = buffer.readInt();
         TileCopy tile = (TileCopy)NoppesUtilServer.saveTileEntity(player, Server.readNBT(buffer));
         if (tile == null || name.isEmpty()) {
            return;
         }

         SchematicController.Instance.save(player, name, t, tile.getPos(), tile.height, tile.width, tile.length);
      }

   }

   private void warn(EntityPlayer player, String warning) {
      player.getServer().logWarning(player.getName() + ": " + warning);
   }

   // $FF: synthetic method
   static boolean access$000(PacketHandlerServer x0, ItemStack x1, EnumPacketServer x2) {
      return x0.allowItem(x1, x2);
   }

   // $FF: synthetic method
   static void access$100(PacketHandlerServer x0, EntityPlayer x1, String x2) {
      x0.warn(x1, x2);
   }

   // $FF: synthetic method
   static void access$200(PacketHandlerServer x0, EnumPacketServer x1, ByteBuf x2, EntityPlayerMP x3, EntityNPCInterface x4) throws Exception {
      x0.handlePacket(x1, x2, x3, x4);
   }
}
