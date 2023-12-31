package noppes.npcs.util;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import noppes.npcs.entity.EntityNPCInterface;

public class GameProfileAlt extends GameProfile {
   private static final UUID id = UUID.fromString("c9c843f8-4cb1-4c82-aa61-e264291b7bd6");
   public EntityNPCInterface npc;

   public GameProfileAlt() {
      super((UUID)null, "[customnpcs]");
   }

   public String getName() {
      return this.npc == null ? super.getName() : this.npc.getName();
   }

   public UUID getId() {
      return this.npc == null ? id : this.npc.getPersistentID();
   }
}
