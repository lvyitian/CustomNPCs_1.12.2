package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom.Item;
import noppes.npcs.NBTTags;

public class SpawnData extends Item {
   public List<String> biomes = new ArrayList();
   public int id = -1;
   public String name = "";
   public NBTTagCompound compound1 = new NBTTagCompound();
   public boolean liquid = false;
   public int type = 0;

   public SpawnData() {
      super(10);
   }

   public void readNBT(NBTTagCompound compound) {
      this.id = compound.getInteger("SpawnId");
      this.name = compound.getString("SpawnName");
      this.itemWeight = compound.getInteger("SpawnWeight");
      if (this.itemWeight == 0) {
         this.itemWeight = 1;
      }

      this.biomes = NBTTags.getStringList(compound.getTagList("SpawnBiomes", 10));
      this.compound1 = compound.getCompoundTag("SpawnCompound1");
      this.type = compound.getInteger("SpawnType");
   }

   public NBTTagCompound writeNBT(NBTTagCompound compound) {
      compound.setInteger("SpawnId", this.id);
      compound.setString("SpawnName", this.name);
      compound.setInteger("SpawnWeight", this.itemWeight);
      compound.setTag("SpawnBiomes", NBTTags.nbtStringList(this.biomes));
      compound.setTag("SpawnCompound1", this.compound1);
      compound.setInteger("SpawnType", this.type);
      return compound;
   }
}
