package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

public class DialogCategory implements IDialogCategory {
   public int id = -1;
   public String title = "";
   public HashMap<Integer, Dialog> dialogs = new HashMap();

   public void readNBT(NBTTagCompound compound) {
      this.id = compound.getInteger("Slot");
      this.title = compound.getString("Title");
      NBTTagList dialogsList = compound.getTagList("Dialogs", 10);
      if (dialogsList != null) {
         for(int ii = 0; ii < dialogsList.tagCount(); ++ii) {
            Dialog dialog = new Dialog(this);
            NBTTagCompound comp = dialogsList.getCompoundTagAt(ii);
            dialog.readNBT(comp);
            dialog.id = comp.getInteger("DialogId");
            this.dialogs.put(Integer.valueOf(dialog.id), dialog);
         }
      }

   }

   public NBTTagCompound writeNBT(NBTTagCompound nbtfactions) {
      nbtfactions.setInteger("Slot", this.id);
      nbtfactions.setString("Title", this.title);
      NBTTagList dialogs = new NBTTagList();

      for(Dialog dialog : this.dialogs.values()) {
         dialogs.appendTag(dialog.writeToNBT(new NBTTagCompound()));
      }

      nbtfactions.setTag("Dialogs", dialogs);
      return nbtfactions;
   }

   public List<IDialog> dialogs() {
      return new ArrayList(this.dialogs.values());
   }

   public String getName() {
      return this.title;
   }

   public IDialog create() {
      return new Dialog(this);
   }
}
