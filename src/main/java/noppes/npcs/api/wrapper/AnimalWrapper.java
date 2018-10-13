package noppes.npcs.api.wrapper;

import net.minecraft.entity.passive.EntityAnimal;
import noppes.npcs.api.entity.IAnimal;

public class AnimalWrapper<T extends EntityAnimal> extends EntityLivingWrapper<T> implements IAnimal {
   public AnimalWrapper(T entity) {
      super(entity);
   }

   public int getType() {
      return 4;
   }

   public boolean typeOf(int type) {
      return type == 4 ? true : super.typeOf(type);
   }

}
