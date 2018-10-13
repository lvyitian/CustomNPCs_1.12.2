package noppes.npcs.client.gui.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import noppes.npcs.containers.ContainerEmpty;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public abstract class GuiContainerNPCInterface extends GuiContainer {
   public boolean drawDefaultBackground = false;
   public int guiLeft;
   public int guiTop;
   public EntityPlayerSP player;
   public EntityNPCInterface npc;
   private HashMap<Integer, GuiNpcButton> buttons = new HashMap();
   private HashMap<Integer, GuiMenuTopButton> topbuttons = new HashMap();
   private HashMap<Integer, GuiNpcTextField> textfields = new HashMap();
   private HashMap<Integer, GuiNpcLabel> labels = new HashMap();
   private HashMap<Integer, GuiCustomScroll> scrolls = new HashMap();
   private HashMap<Integer, GuiNpcSlider> sliders = new HashMap();
   public String title;
   public boolean closeOnEsc = false;
   private SubGuiInterface subgui;
   public int mouseX;
   public int mouseY;

   public GuiContainerNPCInterface(EntityNPCInterface npc, Container cont) {
      super(cont);
      this.player = Minecraft.getMinecraft().player;
      this.npc = npc;
      this.title = "Npc Mainmenu";
      this.mc = Minecraft.getMinecraft();
      this.itemRender = this.mc.getRenderItem();
      this.fontRenderer = this.mc.fontRenderer;
   }

   public void setWorldAndResolution(Minecraft mc, int width, int height) {
      super.setWorldAndResolution(mc, width, height);
      this.initPacket();
   }

   public void initPacket() {
   }

   public void initGui() {
      super.initGui();
      GuiNpcTextField.unfocus();
      this.buttonList.clear();
      this.buttons.clear();
      this.topbuttons.clear();
      this.scrolls.clear();
      this.sliders.clear();
      this.labels.clear();
      this.textfields.clear();
      Keyboard.enableRepeatEvents(true);
      if (this.subgui != null) {
         this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
         this.subgui.initGui();
      }

      this.buttonList.clear();
      this.guiLeft = (this.width - this.xSize) / 2;
      this.guiTop = (this.height - this.ySize) / 2;
   }

   public ResourceLocation getResource(String texture) {
      return new ResourceLocation("customnpcs", "textures/gui/" + texture);
   }

   public void updateScreen() {
      for(GuiNpcTextField tf : this.textfields.values()) {
         if (tf.enabled) {
            tf.updateCursorCounter();
         }
      }

      super.updateScreen();
   }

   protected void mouseClicked(int i, int j, int k) throws IOException {
      if (this.subgui != null) {
         this.subgui.mouseClicked(i, j, k);
      } else {
         for(GuiNpcTextField tf :this.textfields.values()) {
            if (tf.enabled) {
               tf.mouseClicked(i, j, k);
            }
         }

         if (k == 0) {
            for(GuiCustomScroll scroll : this.scrolls.values()) {
               scroll.mouseClicked(i, j, k);
            }
         }

         this.mouseEvent(i, j, k);
         super.mouseClicked(i, j, k);
      }

   }

   public void mouseEvent(int i, int j, int k) {
   }

   protected void keyTyped(char c, int i) {
      if (this.subgui != null) {
         this.subgui.keyTyped(c, i);
      } else {
         for(GuiNpcTextField tf : this.textfields.values()) {
            tf.textboxKeyTyped(c, i);
         }

         if (this.closeOnEsc && (i == 1 || i == this.mc.gameSettings.keyBindInventory.getKeyCode() && !GuiNpcTextField.isActive())) {
            this.close();
         }
      }

   }

   protected void actionPerformed(GuiButton guibutton) {
      if (this.subgui != null) {
         this.subgui.buttonEvent(guibutton);
      } else {
         this.buttonEvent(guibutton);
      }

   }

   public void buttonEvent(GuiButton guibutton) {
   }

   public void close() {
      GuiNpcTextField.unfocus();
      this.save();
      this.player.closeScreen();
      this.displayGuiScreen((GuiScreen)null);
      this.mc.setIngameFocus();
   }

   public void addButton(GuiNpcButton button) {
      this.buttons.put(Integer.valueOf(button.id), button);
      this.buttonList.add(button);
   }

   public void addTopButton(GuiMenuTopButton button) {
      this.topbuttons.put(Integer.valueOf(button.id), button);
      this.buttonList.add(button);
   }

   public GuiNpcButton getButton(int i) {
      return (GuiNpcButton)this.buttons.get(Integer.valueOf(i));
   }

   public void addTextField(GuiNpcTextField tf) {
      this.textfields.put(Integer.valueOf(tf.getId()), tf);
   }

   public GuiNpcTextField getTextField(int i) {
      return (GuiNpcTextField)this.textfields.get(Integer.valueOf(i));
   }

   public void addLabel(GuiNpcLabel label) {
      this.labels.put(Integer.valueOf(label.id), label);
   }

   public GuiNpcLabel getLabel(int i) {
      return (GuiNpcLabel)this.labels.get(Integer.valueOf(i));
   }

   public GuiMenuTopButton getTopButton(int i) {
      return (GuiMenuTopButton)this.topbuttons.get(Integer.valueOf(i));
   }

   public void addSlider(GuiNpcSlider slider) {
      this.sliders.put(Integer.valueOf(slider.id), slider);
      this.buttonList.add(slider);
   }

   public GuiNpcSlider getSlider(int i) {
      return (GuiNpcSlider)this.sliders.get(Integer.valueOf(i));
   }

   public void addScroll(GuiCustomScroll scroll) {
      scroll.setWorldAndResolution(this.mc, 350, 250);
      this.scrolls.put(Integer.valueOf(scroll.id), scroll);
   }

   public GuiCustomScroll getScroll(int id) {
      return (GuiCustomScroll)this.scrolls.get(Integer.valueOf(id));
   }

   protected void drawGuiContainerForegroundLayer(int par1, int par2) {
   }

   protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
      this.drawCenteredString(this.fontRenderer, I18n.translateToLocal(this.title), this.width / 2, this.guiTop - 8, 16777215);

      for(GuiNpcLabel label : this.labels.values()) {
         label.drawLabel(this, this.fontRenderer);
      }

      for(GuiNpcTextField tf : this.textfields.values()) {
         tf.drawTextBox(i, j);
      }

      for(GuiCustomScroll scroll : this.scrolls.values()) {
         scroll.drawScreen(i, j, f, this.hasSubGui() ? 0 : Mouse.getDWheel());
      }

   }

   public abstract void save();

   public void drawScreen(int i, int j, float f) {
      this.mouseX = i;
      this.mouseY = j;
      Container container = this.inventorySlots;
      if (this.subgui != null) {
         this.inventorySlots = new ContainerEmpty();
      }

      super.drawScreen(i, j, f);
      this.zLevel = 0.0F;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.subgui != null) {
         this.inventorySlots = container;
         RenderHelper.disableStandardItemLighting();
         this.subgui.drawScreen(i, j, f);
      }

   }

   public void drawDefaultBackground() {
      if (this.drawDefaultBackground && this.subgui == null) {
         super.drawDefaultBackground();
      }

   }

   public FontRenderer getFontRenderer() {
      return this.fontRenderer;
   }

   public void closeSubGui(SubGuiInterface gui) {
      this.subgui = null;
   }

   public boolean hasSubGui() {
      return this.subgui != null;
   }

   public SubGuiInterface getSubGui() {
      return this.hasSubGui() && this.subgui.hasSubGui() ? this.subgui.getSubGui() : this.subgui;
   }

   public void displayGuiScreen(GuiScreen gui) {
      this.mc.displayGuiScreen(gui);
   }

   public void setSubGui(SubGuiInterface gui) {
      this.subgui = gui;
      this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
      this.subgui.parent = this;
      this.initGui();
   }

   public void drawNpc(int x, int y) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableColorMaterial();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)(this.guiLeft + x), (float)(this.guiTop + y), 50.0F);
      float scale = 1.0F;
      if ((double)this.npc.height > 2.4D) {
         scale = 2.0F / this.npc.height;
      }

      GlStateManager.scale(-30.0F * scale, 30.0F * scale, 30.0F * scale);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      float f2 = this.npc.renderYawOffset;
      float f3 = this.npc.rotationYaw;
      float f4 = this.npc.rotationPitch;
      float f7 = this.npc.rotationYawHead;
      float f5 = (float)(this.guiLeft + x) - (float)this.mouseX;
      float f6 = (float)(this.guiTop + y - 50) - (float)this.mouseY;
      int orientation = 0;
      if (this.npc != null) {
         orientation = this.npc.ais.orientation;
         this.npc.ais.orientation = 0;
      }

      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-((float)Math.atan((double)(f6 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
      this.npc.renderYawOffset = (float)Math.atan((double)(f5 / 40.0F)) * 20.0F;
      this.npc.rotationYaw = (float)Math.atan((double)(f5 / 40.0F)) * 40.0F;
      this.npc.rotationPitch = -((float)Math.atan((double)(f6 / 40.0F))) * 20.0F;
      this.npc.rotationYawHead = this.npc.rotationYaw;
      this.mc.getRenderManager().playerViewY = 180.0F;
      this.mc.getRenderManager().renderEntity(this.npc, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
      this.npc.renderYawOffset = f2;
      this.npc.rotationYaw = f3;
      this.npc.rotationPitch = f4;
      this.npc.rotationYawHead = f7;
      if (this.npc != null) {
         this.npc.ais.orientation = orientation;
      }

      GlStateManager.popMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableRescaleNormal();
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.disableTexture2D();
      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
