package noppes.npcs.roles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.IJobPuppet;
import noppes.npcs.api.entity.data.role.IJobPuppet$IJobPuppetPart;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class JobPuppet extends JobInterface implements IJobPuppet {
   public JobPuppet$PartConfig head = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig larm = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig rarm = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig body = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig lleg = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig rleg = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig head2 = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig larm2 = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig rarm2 = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig body2 = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig lleg2 = new JobPuppet$PartConfig(this);
   public JobPuppet$PartConfig rleg2 = new JobPuppet$PartConfig(this);
   public boolean whileStanding = true;
   public boolean whileAttacking = false;
   public boolean whileMoving = false;
   public boolean animate = false;
   public int animationSpeed = 4;
   private int prevTicks = 0;
   private float val = 0.0F;
   private float valNext = 0.0F;

   public JobPuppet(EntityNPCInterface npc) {
      super(npc);
   }

   public IJobPuppet$IJobPuppetPart getPart(int part) {
      if (part == 0) {
         return this.head;
      } else if (part == 1) {
         return this.larm;
      } else if (part == 2) {
         return this.rarm;
      } else if (part == 3) {
         return this.body;
      } else if (part == 4) {
         return this.lleg;
      } else if (part == 5) {
         return this.rleg;
      } else if (part == 6) {
         return this.head2;
      } else if (part == 7) {
         return this.larm2;
      } else if (part == 8) {
         return this.rarm2;
      } else if (part == 9) {
         return this.body2;
      } else if (part == 10) {
         return this.lleg2;
      } else if (part == 11) {
         return this.rleg2;
      } else {
         throw new CustomNPCsException("Unknown part " + part, new Object[0]);
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound compound) {
      compound.setTag("PuppetHead", this.head.writeNBT());
      compound.setTag("PuppetLArm", this.larm.writeNBT());
      compound.setTag("PuppetRArm", this.rarm.writeNBT());
      compound.setTag("PuppetBody", this.body.writeNBT());
      compound.setTag("PuppetLLeg", this.lleg.writeNBT());
      compound.setTag("PuppetRLeg", this.rleg.writeNBT());
      compound.setTag("PuppetHead2", this.head2.writeNBT());
      compound.setTag("PuppetLArm2", this.larm2.writeNBT());
      compound.setTag("PuppetRArm2", this.rarm2.writeNBT());
      compound.setTag("PuppetBody2", this.body2.writeNBT());
      compound.setTag("PuppetLLeg2", this.lleg2.writeNBT());
      compound.setTag("PuppetRLeg2", this.rleg2.writeNBT());
      compound.setBoolean("PuppetStanding", this.whileStanding);
      compound.setBoolean("PuppetAttacking", this.whileAttacking);
      compound.setBoolean("PuppetMoving", this.whileMoving);
      compound.setBoolean("PuppetAnimate", this.animate);
      compound.setInteger("PuppetAnimationSpeed", this.animationSpeed);
      return compound;
   }

   public void readFromNBT(NBTTagCompound compound) {
      this.head.readNBT(compound.getCompoundTag("PuppetHead"));
      this.larm.readNBT(compound.getCompoundTag("PuppetLArm"));
      this.rarm.readNBT(compound.getCompoundTag("PuppetRArm"));
      this.body.readNBT(compound.getCompoundTag("PuppetBody"));
      this.lleg.readNBT(compound.getCompoundTag("PuppetLLeg"));
      this.rleg.readNBT(compound.getCompoundTag("PuppetRLeg"));
      this.head2.readNBT(compound.getCompoundTag("PuppetHead2"));
      this.larm2.readNBT(compound.getCompoundTag("PuppetLArm2"));
      this.rarm2.readNBT(compound.getCompoundTag("PuppetRArm2"));
      this.body2.readNBT(compound.getCompoundTag("PuppetBody2"));
      this.lleg2.readNBT(compound.getCompoundTag("PuppetLLeg2"));
      this.rleg2.readNBT(compound.getCompoundTag("PuppetRLeg2"));
      this.whileStanding = compound.getBoolean("PuppetStanding");
      this.whileAttacking = compound.getBoolean("PuppetAttacking");
      this.whileMoving = compound.getBoolean("PuppetMoving");
      this.animate = compound.getBoolean("PuppetAnimate");
      this.animationSpeed = compound.getInteger("PuppetAnimationSpeed");
   }

   public boolean aiShouldExecute() {
      return false;
   }

   private float calcRotation(float r, float r2, float partialTicks) {
      if (!this.animate) {
         return r;
      } else {
         if (this.prevTicks != this.npc.ticksExisted) {
            float speed = 0.0F;
            if (this.animationSpeed == 0) {
               speed = 40.0F;
            } else if (this.animationSpeed == 1) {
               speed = 24.0F;
            } else if (this.animationSpeed == 2) {
               speed = 13.0F;
            } else if (this.animationSpeed == 3) {
               speed = 10.0F;
            } else if (this.animationSpeed == 4) {
               speed = 7.0F;
            } else if (this.animationSpeed == 5) {
               speed = 4.0F;
            } else if (this.animationSpeed == 6) {
               speed = 3.0F;
            } else if (this.animationSpeed == 7) {
               speed = 2.0F;
            }

            this.val = (MathHelper.cos((float)this.npc.ticksExisted / speed * 3.1415927F / 2.0F) + 1.0F) / 2.0F;
            this.valNext = (MathHelper.cos((float)(this.npc.ticksExisted + 1) / speed * 3.1415927F / 2.0F) + 1.0F) / 2.0F;
            this.prevTicks = this.npc.ticksExisted;
         }

         float f = this.val + (this.valNext - this.val) * partialTicks;
         return r + (r2 - r) * f;
      }
   }

   public float getRotationX(JobPuppet$PartConfig part1, JobPuppet$PartConfig part2, float partialTicks) {
      return this.calcRotation(part1.rotationX, part2.rotationX, partialTicks);
   }

   public float getRotationY(JobPuppet$PartConfig part1, JobPuppet$PartConfig part2, float partialTicks) {
      return this.calcRotation(part1.rotationY, part2.rotationY, partialTicks);
   }

   public float getRotationZ(JobPuppet$PartConfig part1, JobPuppet$PartConfig part2, float partialTicks) {
      return this.calcRotation(part1.rotationZ, part2.rotationZ, partialTicks);
   }

   public void reset() {
   }

   public void delete() {
   }

   public boolean isActive() {
      if (!this.npc.isEntityAlive()) {
         return false;
      } else {
         return this.whileAttacking && this.npc.isAttacking() || this.whileMoving && this.npc.isWalking() || this.whileStanding && !this.npc.isWalking();
      }
   }

   public boolean getIsAnimated() {
      return this.animate;
   }

   public void setIsAnimated(boolean bo) {
      this.animate = bo;
   }

   public int getAnimationSpeed() {
      return this.animationSpeed;
   }

   public void setAnimationSpeed(int speed) {
      this.animationSpeed = ValueUtil.CorrectInt(speed, 0, 7);
   }
}
