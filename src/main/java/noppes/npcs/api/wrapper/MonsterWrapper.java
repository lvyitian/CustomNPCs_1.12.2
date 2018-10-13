package noppes.npcs.api.wrapper;

import net.minecraft.entity.monster.EntityMob;
import noppes.npcs.api.entity.IMonster;

public class MonsterWrapper<T extends EntityMob> extends EntityLivingWrapper<T> implements IMonster {
   public MonsterWrapper(T entity) {
      super(entity);
   }

   public int getType() {
      return 3;
   }

   public boolean typeOf(int type) {
      return type == 3 ? true : super.typeOf(type);
   }
}
