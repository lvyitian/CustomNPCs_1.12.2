package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCanineTail extends ModelRenderer {
   ModelRenderer Base_1;
   ModelRenderer BaseB_1;
   ModelRenderer Mid_1;
   ModelRenderer Mid_2;
   ModelRenderer MidB_1;
   ModelRenderer End_1;

   public ModelCanineTail(ModelBiped base) {
      super(base);
      this.Base_1 = new ModelRenderer(base, 56, 16);
      this.Base_1.addBox(-1.0F, 0.0F, -3.0F, 2, 3, 2);
      this.Base_1.setRotationPoint(0.0F, 1.0F, -1.2F);
      this.setRotation(this.Base_1, -0.4490659F, 3.141593F, 0.0F);
      this.addChild(this.Base_1);
      this.BaseB_1 = new ModelRenderer(base, 56, 16);
      this.BaseB_1.addBox(-0.5F, 0.0F, -1.5F, 1, 3, 1);
      this.Base_1.addChild(this.BaseB_1);
      this.Mid_1 = new ModelRenderer(base, 56, 20);
      this.Mid_1.addBox(-1.0F, 3.0F, -2.8F, 2, 2, 2);
      this.setRotation(this.Mid_1, -0.16F, 0.0F, 0.0F);
      this.Base_1.addChild(this.Mid_1);
      this.Mid_2 = new ModelRenderer(base, 56, 22);
      this.Mid_2.addBox(-1.5F, 5.0F, -1.5F, 3, 6, 2);
      this.Mid_2.setRotationPoint(0.0F, 0.0F, -1.5F);
      this.setRotation(this.Mid_2, -0.0F, 0.0F, 0.0F);
      this.Mid_1.addChild(this.Mid_2);
      ModelRenderer Mid_2b = new ModelRenderer(base, 56, 23);
      Mid_2b.addBox(-1.5F, 5.0F, -1.5F, 3, 6, 1);
      this.setRotation(Mid_2b, -0.0F, 3.1415927F, 0.0F);
      this.Mid_2.addChild(Mid_2b);
      this.MidB_1 = new ModelRenderer(base, 56, 20);
      this.MidB_1.addBox(-0.5F, 3.0F, -1.0F, 1, 2, 1);
      this.Mid_1.addChild(this.MidB_1);
      this.End_1 = new ModelRenderer(base, 56, 29);
      this.End_1.addBox(-1.0F, 10.7F, -1.0F, 2, 1, 2);
      this.Mid_2.addChild(this.End_1);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
      this.Base_1.rotateAngleX = -0.5490659F - f1 * 0.7F;
      this.Base_1.rotateAngleY = 3.141593F + this.rotateAngleY * 0.1F;
      this.Mid_1.rotateAngleY = this.rotateAngleY * 0.2F;
      this.Mid_2.rotateAngleY = this.rotateAngleY * 0.2F;
   }
}
