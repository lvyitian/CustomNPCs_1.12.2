package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiEditText extends SubGuiInterface {
   public String text;
   public boolean cancelled = true;

   public SubGuiEditText(String text) {
      this.text = text;
      this.setBackground("extrasmallbg.png");
      this.closeOnEsc = true;
      this.xSize = 176;
      this.ySize = 71;
   }

   public void initGui() {
      super.initGui();
      this.addTextField(new GuiNpcTextField(0, this.parent, this.guiLeft + 4, this.guiTop + 14, 168, 20, this.text));
      this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 44, 80, 20, "gui.done"));
      this.addButton(new GuiNpcButton(1, this.guiLeft + 90, this.guiTop + 44, 80, 20, "gui.cancel"));
   }

   public void buttonEvent(GuiButton button) {
      if (button.id == 0) {
         this.cancelled = false;
         this.text = this.getTextField(0).getText();
      }

      this.close();
   }

   public void save() {
   }
}
