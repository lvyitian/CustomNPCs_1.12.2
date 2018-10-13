package noppes.npcs.client.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderNpcDragon<T extends EntityNPCInterface> extends RenderNPCInterface<T> {
   public RenderNpcDragon(ModelBase model, float f) {
      super(model, f);
   }

   @Override
   protected void preRenderCallback(T npc, float f) {
      GlStateManager.translate(0.0F, 0.0F, 0.120000005F * (float)npc.display.getSize());
      super.preRenderCallback(npc, f);
   }

}
