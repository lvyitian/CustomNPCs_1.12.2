package noppes.npcs.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.ModelData;

public class EntityNpcDragon extends EntityNPCInterface {
   public double[][] field_40162_d = new double[64][3];
   public int field_40164_e = -1;
   public float field_40173_aw = 0.0F;
   public float field_40172_ax = 0.0F;
   public int field_40178_aA = 0;
   public boolean isFlying = false;
   private boolean exploded = false;

   public EntityNpcDragon(World world) {
      super(world);
      this.scaleX = 0.4F;
      this.scaleY = 0.4F;
      this.scaleZ = 0.4F;
      this.display.setSkinTexture("customnpcs:textures/entity/dragon/BlackDragon.png");
      this.width = 1.8F;
      this.height = 1.4F;
   }

   public double getMountedYOffset() {
      return 1.1D;
   }

   public double[] func_40160_a(int i, float f) {
      f = 1.0F - f;
      int j = this.field_40164_e - i * 1 & 63;
      int k = this.field_40164_e - i * 1 - 1 & 63;
      double[] ad = new double[3];
      double d = this.field_40162_d[j][0];

      double d1;
      for(d1 = this.field_40162_d[k][0] - d; d1 < -180.0D; d1 += 360.0D) {
         ;
      }

      while(d1 >= 180.0D) {
         d1 -= 360.0D;
      }

      ad[0] = d + d1 * (double)f;
      d = this.field_40162_d[j][1];
      d1 = this.field_40162_d[k][1] - d;
      ad[1] = d + d1 * (double)f;
      ad[2] = this.field_40162_d[j][2] + (this.field_40162_d[k][2] - this.field_40162_d[j][2]) * (double)f;
      return ad;
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
         data.setEntityClass(EntityNpcDragon.class);
         this.world.spawnEntity(npc);
      }

      super.onUpdate();
   }

   public void onLivingUpdate() {
      this.field_40173_aw = this.field_40172_ax;
      if (this.world.isRemote && this.getHealth() <= 0.0F) {
         if (!this.exploded) {
            this.exploded = true;
            float f = (this.rand.nextFloat() - 0.5F) * 8.0F;
            float f2 = (this.rand.nextFloat() - 0.5F) * 4.0F;
            float f4 = (this.rand.nextFloat() - 0.5F) * 8.0F;
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + (double)f, this.posY + 2.0D + (double)f2, this.posZ + (double)f4, 0.0D, 0.0D, 0.0D, new int[0]);
         }
      } else {
         this.exploded = false;
         float f1 = 0.2F / (MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) * 10.0F + 1.0F);
         f1 = 0.045F;
         f1 = f1 * (float)Math.pow(2.0D, this.motionY);
         this.field_40172_ax += f1 * 0.5F;
      }

      super.onLivingUpdate();
   }

   public void updateHitbox() {
      this.width = 1.8F;
      this.height = 1.4F;
   }
}
