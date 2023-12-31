package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNPCElfMale extends EntityNPCInterface {
   public EntityNPCElfMale(World world) {
      super(world);
      this.scaleX = 0.85F;
      this.scaleY = 1.07F;
      this.scaleZ = 0.85F;
      this.display.setSkinTexture("customnpcs:textures/entity/elfmale/ElfMale.png");
   }

   public void onUpdate() {
      this.isDead = true;
      this.setNoAI(true);
      if (!this.world.isRemote) {
         NBTTagCompound compound = new NBTTagCompound();
         this.writeToNBT(compound);
         EntityCustomNpc npc = new EntityCustomNpc(this.world);
         npc.readFromNBT(compound);
         ModelData data = npc.modelData;
         data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.85F, 1.15F);
         data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.85F, 1.15F);
         data.getPartConfig(EnumParts.BODY).setScale(0.85F, 1.15F);
         data.getPartConfig(EnumParts.HEAD).setScale(0.85F, 0.95F);
         this.world.spawnEntity(npc);
      }

      super.onUpdate();
   }
}
