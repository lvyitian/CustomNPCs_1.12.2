package noppes.npcs.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.layer.LayerArms;
import noppes.npcs.client.layer.LayerBody;
import noppes.npcs.client.layer.LayerEyes;
import noppes.npcs.client.layer.LayerHead;
import noppes.npcs.client.layer.LayerHeadwear;
import noppes.npcs.client.layer.LayerLegs;
import noppes.npcs.client.layer.LayerNpcCloak;
import noppes.npcs.client.layer.LayerPreRender;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class RenderCustomNpc<T extends EntityCustomNpc> extends RenderNPCInterface<T> {
   private float partialTicks;
   private EntityLivingBase entity;
   private RenderLivingBase renderEntity;
   public ModelBiped npcmodel;

   public RenderCustomNpc(ModelBiped model) {
      super(model, 0.5F);
      this.npcmodel = (ModelBiped)this.mainModel;
      this.addLayer(new LayerEyes(this));
      this.addLayer(new LayerHeadwear(this));
      this.addLayer(new LayerHead(this));
      this.addLayer(new LayerArms(this));
      this.addLayer(new LayerLegs(this));
      this.addLayer(new LayerBody(this));
      this.addLayer(new LayerHeldItem(this));
      this.addLayer(new LayerNpcCloak(this));
      this.addLayer(new LayerCustomHead(this.npcmodel.bipedHead));
      LayerBipedArmor armor = new LayerBipedArmor(this);
      this.addLayer(armor);
      ObfuscationReflectionHelper.setPrivateValue(LayerArmorBase.class, armor, new ModelBipedAlt(0.5F), 1);
      ObfuscationReflectionHelper.setPrivateValue(LayerArmorBase.class, armor, new ModelBipedAlt(1.0F), 2);
   }

   public void doRender(T npc, double d, double d1, double d2, float f, float partialTicks) {
      this.partialTicks = partialTicks;
      this.entity = npc.modelData.getEntity(npc);
      if (this.entity != null) {
         Render render = this.renderManager.getEntityRenderObject(this.entity);
         if (render instanceof RenderLivingBase) {
            this.renderEntity = (RenderLivingBase)render;
         } else {
            this.renderEntity = null;
            this.entity = null;
         }
      } else {
         this.renderEntity = null;

         for(LayerRenderer layer : this.layerRenderers) {
            if (layer instanceof LayerPreRender) {
               ((LayerPreRender)layer).preRender(npc);
            }
         }
      }

      this.npcmodel.rightArmPose = this.getPose(npc, npc.getHeldItemMainhand());
      this.npcmodel.leftArmPose = this.getPose(npc, npc.getHeldItemOffhand());
      super.doRender(npc, d, d1, d2, f, partialTicks);
   }

   public ArmPose getPose(T npc, ItemStack item) {
      if (NoppesUtilServer.IsItemStackNull(item)) {
         return ArmPose.EMPTY;
      } else {
         if (npc.getItemInUseCount() > 0) {
            EnumAction enumaction = item.getItemUseAction();
            if (enumaction == EnumAction.BLOCK) {
               return ArmPose.BLOCK;
            }

            if (enumaction == EnumAction.BOW) {
               return ArmPose.BOW_AND_ARROW;
            }
         }

         return ArmPose.ITEM;
      }
   }

   protected void renderModel(T npc, float par2, float par3, float par4, float par5, float par6, float par7) {
      if (this.renderEntity != null) {
         boolean flag = !npc.isInvisible();
         boolean flag1 = !flag && !npc.isInvisibleToPlayer(Minecraft.getMinecraft().player);
         if (!flag && !flag1) {
            return;
         }

         if (flag1) {
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.alphaFunc(516, 0.003921569F);
         }

         ModelBase model = this.renderEntity.getMainModel();
         if (PixelmonHelper.isPixelmon(this.entity)) {
            ModelBase pixModel = (ModelBase)PixelmonHelper.getModel(this.entity);
            if (pixModel != null) {
               model = pixModel;
            }
         }

         model.swingProgress = 1.0F;
         model.setLivingAnimations(this.entity, par2, par3, this.partialTicks);
         model.setRotationAngles(par2, par3, par4, par5, par6, par7, this.entity);
         model.isChild = this.entity.isChild();
         NPCRendererHelper.RenderModel(this.entity, par2, par3, par4, par5, par6, par7, this.renderEntity, model, this.getEntityTexture(npc));
         if (!npc.display.getOverlayTexture().isEmpty()) {
            GlStateManager.depthFunc(515);
            if (npc.textureGlowLocation == null) {
               npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
            }

            float f1 = 1.0F;
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(1, 1);
            GlStateManager.disableLighting();
            if (npc.isInvisible()) {
               GlStateManager.depthMask(false);
            } else {
               GlStateManager.depthMask(true);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.001F, 1.001F, 1.001F);
            NPCRendererHelper.RenderModel(this.entity, par2, par3, par4, par5, par6, par7, this.renderEntity, model, npc.textureGlowLocation);
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, f1);
            GlStateManager.depthFunc(515);
            GlStateManager.disableBlend();
         }

         if (flag1) {
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
         }
      } else {
         super.renderModel(npc, par2, par3, par4, par5, par6, par7);
      }

   }

   protected void renderLayers(T livingEntity, float p_177093_2_, float p_177093_3_, float p_177093_4_, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_) {
      if (this.entity != null && this.renderEntity != null) {
         NPCRendererHelper.DrawLayers(this.entity, p_177093_2_, p_177093_3_, p_177093_4_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_, this.renderEntity);
      } else {
         super.renderLayers(livingEntity, p_177093_2_, p_177093_3_, p_177093_4_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);
      }

   }

   protected void preRenderCallback(T npc, float f) {
      if (this.renderEntity != null) {
         this.renderColor(npc);
         int size = npc.display.getSize();
         if (this.entity instanceof EntityNPCInterface) {
            ((EntityNPCInterface)this.entity).display.setSize(5);
         }

         NPCRendererHelper.preRenderCallback(this.entity, f, this.renderEntity);
         npc.display.setSize(size);
         GlStateManager.scale(0.2F * (float)npc.display.getSize(), 0.2F * (float)npc.display.getSize(), 0.2F * (float)npc.display.getSize());
      } else {
         super.preRenderCallback(npc, f);
      }

   }

   protected float handleRotationFloat(T par1EntityLivingBase, float par2) {
      return this.renderEntity != null ? NPCRendererHelper.handleRotationFloat(this.entity, par2, this.renderEntity) : super.handleRotationFloat(par1EntityLivingBase, par2);
   }

}
