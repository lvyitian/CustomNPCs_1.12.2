package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIPounceTarget extends EntityAIBase {
   private EntityNPCInterface npc;
   private EntityLivingBase leapTarget;
   private float leapSpeed = 1.3F;

   public EntityAIPounceTarget(EntityNPCInterface leapingEntity) {
      this.npc = leapingEntity;
      this.setMutexBits(4);
   }

   public boolean shouldExecute() {
      if (!this.npc.onGround) {
         return false;
      } else {
         this.leapTarget = this.npc.getAttackTarget();
         if (this.leapTarget != null && this.npc.getEntitySenses().canSee(this.leapTarget)) {
            return !this.npc.isInRange(this.leapTarget, 4.0D) && this.npc.isInRange(this.leapTarget, 8.0D) ? this.npc.getRNG().nextInt(5) == 0 : false;
         } else {
            return false;
         }
      }
   }

   public boolean shouldContinueExecuting() {
      return !this.npc.onGround;
   }

   public void startExecuting() {
      double varX = this.leapTarget.posX - this.npc.posX;
      double varY = this.leapTarget.getEntityBoundingBox().minY - this.npc.getEntityBoundingBox().minY;
      double varZ = this.leapTarget.posZ - this.npc.posZ;
      float varF = MathHelper.sqrt(varX * varX + varZ * varZ);
      float angle = this.getAngleForXYZ(varX, varY, varZ, (double)varF);
      float yaw = (float)(Math.atan2(varX, varZ) * 180.0D / 3.141592653589793D);
      this.npc.motionX = (double)(MathHelper.sin(yaw / 180.0F * 3.1415927F) * MathHelper.cos(angle / 180.0F * 3.1415927F));
      this.npc.motionZ = (double)(MathHelper.cos(yaw / 180.0F * 3.1415927F) * MathHelper.cos(angle / 180.0F * 3.1415927F));
      this.npc.motionY = (double)MathHelper.sin((angle + 1.0F) / 180.0F * 3.1415927F);
      this.npc.motionX *= (double)this.leapSpeed;
      this.npc.motionZ *= (double)this.leapSpeed;
      this.npc.motionY *= (double)this.leapSpeed;
   }

   public float getAngleForXYZ(double varX, double varY, double varZ, double horiDist) {
      float g = 0.1F;
      float var1 = this.leapSpeed * this.leapSpeed;
      double var2 = (double)g * horiDist;
      double var3 = (double)g * horiDist * horiDist + 2.0D * varY * (double)var1;
      double var4 = (double)(var1 * var1) - (double)g * var3;
      if (var4 < 0.0D) {
         return 90.0F;
      } else {
         float var6 = var1 - MathHelper.sqrt(var4);
         float var7 = (float)(Math.atan2((double)var6, var2) * 180.0D / 3.141592653589793D);
         return var7;
      }
   }
}
