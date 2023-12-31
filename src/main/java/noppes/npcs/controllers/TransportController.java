package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;

public class TransportController {
   private HashMap<Integer, TransportLocation> locations = new HashMap();
   public HashMap<Integer, TransportCategory> categories = new HashMap();
   private int lastUsedID = 0;
   private static TransportController instance;

   public TransportController() {
      instance = this;
      this.loadCategories();
      if (this.categories.isEmpty()) {
         TransportCategory cat = new TransportCategory();
         cat.id = 1;
         cat.title = "Default";
         this.categories.put(Integer.valueOf(cat.id), cat);
      }

   }

   private void loadCategories() {
      File saveDir = CustomNpcs.getWorldSaveDirectory();
      if (saveDir != null) {
         try {
            File file = new File(saveDir, "transport.dat");
            if (!file.exists()) {
               return;
            }

            this.loadCategories(file);
         } catch (IOException var5) {
            try {
               File file = new File(saveDir, "transport.dat_old");
               if (!file.exists()) {
                  return;
               }

               this.loadCategories(file);
            } catch (IOException var4) {
               ;
            }
         }

      }
   }

   public void loadCategories(File file) throws IOException {
      HashMap<Integer, TransportLocation> locations = new HashMap();
      HashMap<Integer, TransportCategory> categories = new HashMap();
      NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
      this.lastUsedID = nbttagcompound1.getInteger("lastID");
      NBTTagList list = nbttagcompound1.getTagList("NPCTransportCategories", 10);
      if (list != null) {
         for(int i = 0; i < list.tagCount(); ++i) {
            TransportCategory category = new TransportCategory();
            NBTTagCompound compound = list.getCompoundTagAt(i);
            category.readNBT(compound);

            for(TransportLocation location : category.locations.values()) {
               locations.put(Integer.valueOf(location.id), location);
            }

            categories.put(Integer.valueOf(category.id), category);
         }

         this.locations = locations;
         this.categories = categories;
      }
   }

   public NBTTagCompound getNBT() {
      NBTTagList list = new NBTTagList();

      for(TransportCategory category : this.categories.values()) {
         NBTTagCompound compound = new NBTTagCompound();
         category.writeNBT(compound);
         list.appendTag(compound);
      }

      NBTTagCompound nbttagcompound = new NBTTagCompound();
      nbttagcompound.setInteger("lastID", this.lastUsedID);
      nbttagcompound.setTag("NPCTransportCategories", list);
      return nbttagcompound;
   }

   public void saveCategories() {
      try {
         File saveDir = CustomNpcs.getWorldSaveDirectory();
         File file = new File(saveDir, "transport.dat_new");
         File file1 = new File(saveDir, "transport.dat_old");
         File file2 = new File(saveDir, "transport.dat");
         CompressedStreamTools.writeCompressed(this.getNBT(), new FileOutputStream(file));
         if (file1.exists()) {
            file1.delete();
         }

         file2.renameTo(file1);
         if (file2.exists()) {
            file2.delete();
         }

         file.renameTo(file2);
         if (file.exists()) {
            file.delete();
         }
      } catch (Exception var5) {
         LogWriter.except(var5);
      }

   }

   public TransportLocation getTransport(int transportId) {
      return (TransportLocation)this.locations.get(Integer.valueOf(transportId));
   }

   public TransportLocation getTransport(String name) {
      for(TransportLocation loc : this.locations.values()) {
         if (loc.name.equals(name)) {
            return loc;
         }
      }

      return null;
   }

   private int getUniqueIdLocation() {
      if (this.lastUsedID == 0) {
         Iterator var1 = this.locations.keySet().iterator();

         while(var1.hasNext()) {
            int catid = ((Integer)var1.next()).intValue();
            if (catid > this.lastUsedID) {
               this.lastUsedID = catid;
            }
         }
      }

      ++this.lastUsedID;
      return this.lastUsedID;
   }

   private int getUniqueIdCategory() {
      int id = 0;
      Iterator var2 = this.categories.keySet().iterator();

      while(var2.hasNext()) {
         int catid = ((Integer)var2.next()).intValue();
         if (catid > id) {
            id = catid;
         }
      }

      ++id;
      return id;
   }

   public void setLocation(TransportLocation location) {
      if (this.locations.containsKey(Integer.valueOf(location.id))) {
         for(TransportCategory cat : this.categories.values()) {
            cat.locations.remove(Integer.valueOf(location.id));
         }
      }

      this.locations.put(Integer.valueOf(location.id), location);
      location.category.locations.put(Integer.valueOf(location.id), location);
   }

   public TransportLocation removeLocation(int location) {
      TransportLocation loc = (TransportLocation)this.locations.get(Integer.valueOf(location));
      if (loc == null) {
         return null;
      } else {
         loc.category.locations.remove(Integer.valueOf(location));
         this.locations.remove(Integer.valueOf(location));
         this.saveCategories();
         return loc;
      }
   }

   private boolean containsCategoryName(String name) {
      name = name.toLowerCase();

      for(TransportCategory cat : this.categories.values()) {
         if (cat.title.toLowerCase().equals(name)) {
            return true;
         }
      }

      return false;
   }

   public void saveCategory(String name, int id) {
      if (id < 0) {
         id = this.getUniqueIdCategory();
      }

      if (this.categories.containsKey(Integer.valueOf(id))) {
         TransportCategory category = (TransportCategory)this.categories.get(Integer.valueOf(id));
         if (!category.title.equals(name)) {
            while(this.containsCategoryName(name)) {
               name = name + "_";
            }

            ((TransportCategory)this.categories.get(Integer.valueOf(id))).title = name;
         }
      } else {
         while(this.containsCategoryName(name)) {
            name = name + "_";
         }

         TransportCategory category = new TransportCategory();
         category.id = id;
         category.title = name;
         this.categories.put(Integer.valueOf(id), category);
      }

      this.saveCategories();
   }

   public void removeCategory(int id) {
      if (this.categories.size() != 1) {
         TransportCategory cat = (TransportCategory)this.categories.get(Integer.valueOf(id));
         if (cat != null) {
            Iterator var3 = cat.locations.keySet().iterator();

            while(var3.hasNext()) {
               int i = ((Integer)var3.next()).intValue();
               this.locations.remove(Integer.valueOf(i));
            }

            this.categories.remove(Integer.valueOf(id));
            this.saveCategories();
         }
      }
   }

   public boolean containsLocationName(String name) {
      name = name.toLowerCase();

      for(TransportLocation loc : this.locations.values()) {
         if (loc.name.toLowerCase().equals(name)) {
            return true;
         }
      }

      return false;
   }

   public static TransportController getInstance() {
      return instance;
   }

   public TransportLocation saveLocation(int categoryId, NBTTagCompound compound, EntityPlayerMP player, EntityNPCInterface npc) {
      TransportCategory category = (TransportCategory)this.categories.get(Integer.valueOf(categoryId));
      if (category != null && npc.advanced.role == 4) {
         RoleTransporter role = (RoleTransporter)npc.roleInterface;
         TransportLocation location = new TransportLocation();
         location.readNBT(compound);
         location.category = category;
         if (role.hasTransport()) {
            location.id = role.transportId;
         }

         if (location.id < 0 || !((TransportLocation)this.locations.get(Integer.valueOf(location.id))).name.equals(location.name)) {
            while(this.containsLocationName(location.name)) {
               location.name = location.name + "_";
            }
         }

         if (location.id < 0) {
            location.id = this.getUniqueIdLocation();
         }

         category.locations.put(Integer.valueOf(location.id), location);
         this.locations.put(Integer.valueOf(location.id), location);
         this.saveCategories();
         return location;
      } else {
         return null;
      }
   }
}
