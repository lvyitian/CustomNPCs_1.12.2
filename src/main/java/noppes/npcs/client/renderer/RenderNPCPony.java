package noppes.npcs.client.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.model.ModelPony;
import noppes.npcs.client.model.ModelPonyArmor;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcPony;

public class RenderNPCPony<T extends EntityNpcPony> extends RenderNPCInterface<T> {
   private ModelPony modelBipedMain;
   private ModelPonyArmor modelArmorChestplate;
   private ModelPonyArmor modelArmor;

   public RenderNPCPony() {
      super(new ModelPony(0.0F), 0.5F);
      this.modelBipedMain = (ModelPony)this.mainModel;
      this.modelArmorChestplate = new ModelPonyArmor(1.0F);
      this.modelArmor = new ModelPonyArmor(0.5F);
   }

   @Override
   public ResourceLocation getEntityTexture(T pony) {
      boolean check = pony.textureLocation == null || pony.textureLocation != pony.checked;
      ResourceLocation loc = super.getEntityTexture(pony);
      if (check) {
         try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(loc);
            BufferedImage bufferedimage = ImageIO.read(resource.getInputStream());
            pony.isPegasus = false;
            pony.isUnicorn = false;
            Color color = new Color(bufferedimage.getRGB(0, 0), true);
            Color color1 = new Color(249, 177, 49, 255);
            Color color2 = new Color(136, 202, 240, 255);
            Color color3 = new Color(209, 159, 228, 255);
            Color color4 = new Color(254, 249, 252, 255);
            if (color.equals(color1)) {
               ;
            }

            if (color.equals(color2)) {
               pony.isPegasus = true;
            }

            if (color.equals(color3)) {
               pony.isUnicorn = true;
            }

            if (color.equals(color4)) {
               pony.isPegasus = true;
               pony.isUnicorn = true;
            }

            pony.checked = loc;
         } catch (IOException var11) {
            ;
         }
      }

      return loc;
   }

   @Override
   public void doRender(T pony, double d, double d1, double d2, float f, float f1) {
      ItemStack itemstack = pony.getHeldItemMainhand();
      this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = itemstack == null ? 0 : 1;
      this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = pony.isSneaking();
      this.modelArmorChestplate.isRiding = this.modelArmor.isRiding = this.modelBipedMain.isRiding = false;
      this.modelArmorChestplate.isSleeping = this.modelArmor.isSleeping = this.modelBipedMain.isSleeping = pony.isPlayerSleeping();
      this.modelArmorChestplate.isUnicorn = this.modelArmor.isUnicorn = this.modelBipedMain.isUnicorn = pony.isUnicorn;
      this.modelArmorChestplate.isPegasus = this.modelArmor.isPegasus = this.modelBipedMain.isPegasus = pony.isPegasus;
      if (pony.isSneaking()) {
         d1 -= 0.125D;
      }

      super.doRender(pony, d, d1, d2, f, f1);
      this.modelArmorChestplate.aimedBow = this.modelArmor.aimedBow = this.modelBipedMain.aimedBow = false;
      this.modelArmorChestplate.isRiding = this.modelArmor.isRiding = this.modelBipedMain.isRiding = false;
      this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = false;
      this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 0;
   }

}
