package noppes.npcs.roles;

import java.util.HashMap;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.IRoleDialog;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleDialog extends RoleInterface implements IRoleDialog {
   public String dialog = "";
   public int questId = -1;
   public HashMap<Integer, String> options = new HashMap();
   public HashMap<Integer, String> optionsTexts = new HashMap();

   public RoleDialog(EntityNPCInterface npc) {
      super(npc);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound compound) {
      compound.setInteger("RoleQuestId", this.questId);
      compound.setString("RoleDialog", this.dialog);
      compound.setTag("RoleOptions", NBTTags.nbtIntegerStringMap(this.options));
      compound.setTag("RoleOptionTexts", NBTTags.nbtIntegerStringMap(this.optionsTexts));
      return compound;
   }

   public void readFromNBT(NBTTagCompound compound) {
      this.questId = compound.getInteger("RoleQuestId");
      this.dialog = compound.getString("RoleDialog");
      this.options = NBTTags.getIntegerStringMap(compound.getTagList("RoleOptions", 10));
      this.optionsTexts = NBTTags.getIntegerStringMap(compound.getTagList("RoleOptionTexts", 10));
   }

   public void interact(EntityPlayer player) {
      if (this.dialog.isEmpty()) {
         this.npc.say(player, this.npc.advanced.getInteractLine());
      } else {
         Dialog d = new Dialog((DialogCategory)null);
         d.text = this.dialog;

         for(Entry<Integer, String> entry : this.options.entrySet()) {
            if (!((String)entry.getValue()).isEmpty()) {
               DialogOption option = new DialogOption();
               String text = (String)this.optionsTexts.get(entry.getKey());
               if (text != null && !text.isEmpty()) {
                  option.optionType = 3;
               } else {
                  option.optionType = 0;
               }

               option.title = (String)entry.getValue();
               d.options.put(entry.getKey(), option);
            }
         }

         NoppesUtilServer.openDialog(player, this.npc, d);
      }

      Quest quest = (Quest)QuestController.instance.quests.get(Integer.valueOf(this.questId));
      if (quest != null) {
         PlayerQuestController.addActiveQuest(quest, player);
      }

   }

   public String getDialog() {
      return this.dialog;
   }

   public void setDialog(String text) {
      this.dialog = text;
   }

   public String getOption(int option) {
      return (String)this.options.get(Integer.valueOf(option));
   }

   public void setOption(int option, String text) {
      if (option >= 1 && option <= 6) {
         this.options.put(Integer.valueOf(option), text);
      } else {
         throw new CustomNPCsException("Wrong dialog option slot given: " + option, new Object[0]);
      }
   }

   public String getOptionDialog(int option) {
      return (String)this.optionsTexts.get(Integer.valueOf(option));
   }

   public void setOptionDialog(int option, String text) {
      if (option >= 1 && option <= 6) {
         this.optionsTexts.put(Integer.valueOf(option), text);
      } else {
         throw new CustomNPCsException("Wrong dialog option slot given: " + option, new Object[0]);
      }
   }
}
