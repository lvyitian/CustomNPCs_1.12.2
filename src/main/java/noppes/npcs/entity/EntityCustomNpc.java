package noppes.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.constants.EnumParts;

public class EntityCustomNpc extends EntityNPCFlying {
   public ModelData modelData = new ModelData();

   public EntityCustomNpc(World world) {
      super(world);
      if (!CustomNpcs.EnableDefaultEyes) {
         this.modelData.eyes.type = -1;
      }

   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      if (compound.hasKey("NpcModelData")) {
         this.modelData.readFromNBT(compound.getCompoundTag("NpcModelData"));
      }

      super.readEntityFromNBT(compound);
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      compound.setTag("NpcModelData", this.modelData.writeToNBT());
   }

   public boolean writeToNBTOptional(NBTTagCompound compound) {
      boolean bo = super.writeToNBTOptional(compound);
      if (bo) {
         String s = this.getEntityString();
         if (s.equals("minecraft:customnpcs.customnpc")) {
            compound.setString("id", "customnpcs:customnpc");
         }
      }

      return bo;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.isRemote()) {
         ModelPartData particles = this.modelData.getPartData(EnumParts.PARTICLES);
         if (particles != null && !this.isKilled()) {
            ;
         }

         EntityLivingBase entity = this.modelData.getEntity(this);
         if (entity != null) {
            try {
               entity.onUpdate();
            } catch (Exception var4) {
               ;
            }

            EntityUtil.Copy(this, entity);
         }
      }

      this.modelData.eyes.update(this);
   }

   public boolean startRiding(Entity par1Entity, boolean force) {
      boolean b = super.startRiding(par1Entity, force);
      this.updateHitbox();
      return b;
   }

   public void updateHitbox() {
      Entity entity = this.modelData.getEntity(this);
      if (this.modelData != null && entity != null) {
         if (entity instanceof EntityNPCInterface) {
            ((EntityNPCInterface)entity).updateHitbox();
         }

         this.width = entity.width / 5.0F * (float)this.display.getSize();
         this.height = entity.height / 5.0F * (float)this.display.getSize();
         if (this.width < 0.1F) {
            this.width = 0.1F;
         }

         if (this.height < 0.1F) {
            this.height = 0.1F;
         }

         this.setPosition(this.posX, this.posY, this.posZ);
      } else {
         this.baseHeight = 1.9F - this.modelData.getBodyY() + (this.modelData.getPartConfig(EnumParts.HEAD).scaleY - 1.0F) / 2.0F;
         super.updateHitbox();
      }

   }
}
