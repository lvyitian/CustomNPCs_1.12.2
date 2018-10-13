package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.util.NBTJsonUtil;

public class DialogController implements IDialogHandler {
   public HashMap<Integer, DialogCategory> categoriesSync = new HashMap();
   public HashMap<Integer, DialogCategory> categories = new HashMap();
   public HashMap<Integer, Dialog> dialogs = new HashMap();
   public static DialogController instance = new DialogController();
   private int lastUsedDialogID = 0;
   private int lastUsedCatID = 0;

   public DialogController() {
      instance = this;
   }

   public void load() {
      LogWriter.info("Loading Dialogs");
      this.loadCategories();
      LogWriter.info("Done loading Dialogs");
   }

   private void loadCategories() {
      this.categories.clear();
      this.dialogs.clear();
      this.lastUsedCatID = 0;
      this.lastUsedDialogID = 0;

      try {
         File file = new File(CustomNpcs.getWorldSaveDirectory(), "dialog.dat");
         if (file.exists()) {
            this.loadCategoriesOld(file);
            file.delete();
            file = new File(CustomNpcs.getWorldSaveDirectory(), "dialog.dat_old");
            if (file.exists()) {
               file.delete();
            }

            return;
         }
      } catch (Exception var11) {
         LogWriter.except(var11);
      }

      File dir = this.getDir();
      if (!dir.exists()) {
         dir.mkdir();
         this.loadDefaultDialogs();
      } else {
         for(File file : dir.listFiles()) {
            if (file.isDirectory()) {
               DialogCategory category = this.loadCategoryDir(file);
               Iterator<Entry<Integer, Dialog>> ite = category.dialogs.entrySet().iterator();

               while(ite.hasNext()) {
                  Entry<Integer, Dialog> entry = (Entry)ite.next();
                  int id = ((Integer)entry.getKey()).intValue();
                  if (id > this.lastUsedDialogID) {
                     this.lastUsedDialogID = id;
                  }

                  Dialog dialog = (Dialog)entry.getValue();
                  if (this.dialogs.containsKey(Integer.valueOf(id))) {
                     LogWriter.error("Duplicate id " + dialog.id + " from category " + category.title);
                     ite.remove();
                  } else {
                     this.dialogs.put(Integer.valueOf(id), dialog);
                  }
               }

               ++this.lastUsedCatID;
               category.id = this.lastUsedCatID;
               this.categories.put(Integer.valueOf(category.id), category);
            }
         }
      }

   }

   private DialogCategory loadCategoryDir(File dir) {
      DialogCategory category = new DialogCategory();
      category.title = dir.getName();

      for(File file : dir.listFiles()) {
         if (file.isFile() && file.getName().endsWith(".json")) {
            try {
               Dialog dialog = new Dialog(category);
               dialog.id = Integer.parseInt(file.getName().substring(0, file.getName().length() - 5));
               dialog.readNBTPartial(NBTJsonUtil.LoadFile(file));
               category.dialogs.put(Integer.valueOf(dialog.id), dialog);
            } catch (Exception var8) {
               LogWriter.error("Error loading: " + file.getAbsolutePath(), var8);
            }
         }
      }

      return category;
   }

   private void loadCategoriesOld(File file) throws Exception {
      NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
      NBTTagList list = nbttagcompound1.getTagList("Data", 10);
      if (list != null) {
         for(int i = 0; i < list.tagCount(); ++i) {
            DialogCategory category = new DialogCategory();
            category.readNBT(list.getCompoundTagAt(i));
            this.saveCategory(category);
            Iterator<Entry<Integer, Dialog>> ita = category.dialogs.entrySet().iterator();

            while(ita.hasNext()) {
               Entry<Integer, Dialog> entry = (Entry)ita.next();
               Dialog dialog = (Dialog)entry.getValue();
               dialog.id = ((Integer)entry.getKey()).intValue();
               if (this.dialogs.containsKey(Integer.valueOf(dialog.id))) {
                  ita.remove();
               } else {
                  this.saveDialog(category, dialog);
               }
            }
         }

      }
   }

   private void loadDefaultDialogs() {
      DialogCategory cat = new DialogCategory();
      cat.id = this.lastUsedCatID++;
      cat.title = "Villager";
      Dialog dia1 = new Dialog(cat);
      dia1.id = 1;
      dia1.title = "Start";
      dia1.text = "Hello {player}, \n\nWelcome to our village. I hope you enjoy your stay";
      Dialog dia2 = new Dialog(cat);
      dia2.id = 2;
      dia2.title = "Ask about village";
      dia2.text = "This village has been around for ages. Enjoy your stay here.";
      Dialog dia3 = new Dialog(cat);
      dia3.id = 3;
      dia3.title = "Who are you";
      dia3.text = "I'm a villager here. I have lived in this village my whole life.";
      cat.dialogs.put(Integer.valueOf(dia1.id), dia1);
      cat.dialogs.put(Integer.valueOf(dia2.id), dia2);
      cat.dialogs.put(Integer.valueOf(dia3.id), dia3);
      DialogOption option = new DialogOption();
      option.title = "Tell me something about this village";
      option.dialogId = 2;
      option.optionType = 1;
      DialogOption option2 = new DialogOption();
      option2.title = "Who are you?";
      option2.dialogId = 3;
      option2.optionType = 1;
      DialogOption option3 = new DialogOption();
      option3.title = "Goodbye";
      option3.optionType = 0;
      dia1.options.put(Integer.valueOf(0), option2);
      dia1.options.put(Integer.valueOf(1), option);
      dia1.options.put(Integer.valueOf(2), option3);
      DialogOption option4 = new DialogOption();
      option4.title = "Back";
      option4.dialogId = 1;
      dia2.options.put(Integer.valueOf(1), option4);
      dia3.options.put(Integer.valueOf(1), option4);
      this.lastUsedDialogID = 3;
      this.lastUsedCatID = 1;
      this.saveCategory(cat);
      this.saveDialog(cat, dia1);
      this.saveDialog(cat, dia2);
      this.saveDialog(cat, dia3);
   }

   public void saveCategory(DialogCategory category) {
      category.title = NoppesStringUtils.cleanFileName(category.title);
      if (this.categories.containsKey(Integer.valueOf(category.id))) {
         DialogCategory currentCategory = (DialogCategory)this.categories.get(Integer.valueOf(category.id));
         if (!currentCategory.title.equals(category.title)) {
            while(this.containsCategoryName(category.title)) {
               category.title = category.title + "_";
            }

            File newdir = new File(this.getDir(), category.title);
            File olddir = new File(this.getDir(), currentCategory.title);
            if (newdir.exists()) {
               return;
            }

            if (!olddir.renameTo(newdir)) {
               return;
            }
         }

         category.dialogs = currentCategory.dialogs;
      } else {
         if (category.id < 0) {
            ++this.lastUsedCatID;
            category.id = this.lastUsedCatID;
         }

         while(this.containsCategoryName(category.title)) {
            category.title = category.title + "_";
         }

         File dir = new File(this.getDir(), category.title);
         if (!dir.exists()) {
            dir.mkdirs();
         }
      }

      this.categories.put(Integer.valueOf(category.id), category);
      Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, Integer.valueOf(5), category.writeNBT(new NBTTagCompound()));
   }

   public void removeCategory(int category) {
      DialogCategory cat = (DialogCategory)this.categories.get(Integer.valueOf(category));
      if (cat != null) {
         File dir = new File(this.getDir(), cat.title);
         if (dir.delete()) {
            Iterator var4 = cat.dialogs.keySet().iterator();

            while(var4.hasNext()) {
               int dia = ((Integer)var4.next()).intValue();
               this.dialogs.remove(Integer.valueOf(dia));
            }

            this.categories.remove(Integer.valueOf(category));
            Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, Integer.valueOf(5), category);
         }
      }
   }

   private boolean containsCategoryName(String name) {
      name = name.toLowerCase();

      for(DialogCategory cat : this.categories.values()) {
         if (cat.title.toLowerCase().equals(name)) {
            return true;
         }
      }

      return false;
   }

   private boolean containsDialogName(DialogCategory category, Dialog dialog) {
      for(Dialog dia : category.dialogs.values()) {
         if (dia.id != dialog.id && dia.title.equalsIgnoreCase(dialog.title)) {
            return true;
         }
      }

      return false;
   }

   public Dialog saveDialog(DialogCategory category, Dialog dialog) {
      if (category == null) {
         return dialog;
      } else {
         while(this.containsDialogName(dialog.category, dialog)) {
            dialog.title = dialog.title + "_";
         }

         if (dialog.id < 0) {
            ++this.lastUsedDialogID;
            dialog.id = this.lastUsedDialogID;
         }

         this.dialogs.put(Integer.valueOf(dialog.id), dialog);
         category.dialogs.put(Integer.valueOf(dialog.id), dialog);
         File dir = new File(this.getDir(), category.title);
         if (!dir.exists()) {
            dir.mkdirs();
         }

         File file = new File(dir, dialog.id + ".json_new");
         File file2 = new File(dir, dialog.id + ".json");

         try {
            NBTTagCompound compound = dialog.writeToNBT(new NBTTagCompound());
            NBTJsonUtil.SaveFile(file, compound);
            if (file2.exists()) {
               file2.delete();
            }

            file.renameTo(file2);
            Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, Integer.valueOf(4), compound, category.id);
         } catch (Exception var7) {
            LogWriter.except(var7);
         }

         return dialog;
      }
   }

   public void removeDialog(Dialog dialog) {
      DialogCategory category = dialog.category;
      File file = new File(new File(this.getDir(), category.title), dialog.id + ".json");
      if (file.delete()) {
         category.dialogs.remove(Integer.valueOf(dialog.id));
         this.dialogs.remove(Integer.valueOf(dialog.id));
         Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, Integer.valueOf(4), dialog.id);
      }
   }

   private File getDir() {
      return new File(CustomNpcs.getWorldSaveDirectory(), "dialogs");
   }

   public boolean hasDialog(int dialogId) {
      return this.dialogs.containsKey(Integer.valueOf(dialogId));
   }

   public Map<String, Integer> getScroll() {
      Map<String, Integer> map = new HashMap();

      for(DialogCategory category : this.categories.values()) {
         map.put(category.title, Integer.valueOf(category.id));
      }

      return map;
   }

   public List<IDialogCategory> categories() {
      return new ArrayList(this.categories.values());
   }

   public IDialog get(int id) {
      return (IDialog)this.dialogs.get(Integer.valueOf(id));
   }
}
