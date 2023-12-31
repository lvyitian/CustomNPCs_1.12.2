package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelRodentTail extends ModelRenderer {
   ModelRenderer Shape1;
   ModelRenderer Shape2;

   public ModelRodentTail(ModelBiped base) {
      super(base);
      this.Shape1 = new ModelRenderer(base, 0, 0);
      this.Shape1.addBox(-0.5333334F, -0.4666667F, -1.0F, 1, 1, 6);
      this.Shape1.setRotationPoint(0.0F, 0.0F, 2.0F);
      this.setRotation(this.Shape1, -0.9294653F, 0.0F, 0.0F);
      this.addChild(this.Shape1);
      this.Shape2 = new ModelRenderer(base, 1, 1);
      this.Shape2.addBox(-0.5F, -0.1666667F, 1.0F, 1, 1, 5);
      this.Shape2.setRotationPoint(0.0F, 3.0F, 4.0F);
      this.setRotation(this.Shape2, -0.4833219F, 0.0F, 0.0F);
      this.addChild(this.Shape2);
   }

   private void setRotation(ModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }
}
