package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCJob;

public interface IJobPuppet extends INPCJob {
   boolean getIsAnimated();

   void setIsAnimated(boolean var1);

   int getAnimationSpeed();

   void setAnimationSpeed(int var1);

   IJobPuppet$IJobPuppetPart getPart(int var1);
}
