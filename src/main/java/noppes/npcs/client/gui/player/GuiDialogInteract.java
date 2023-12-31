package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;

public class GuiDialogInteract extends GuiNPCInterface implements IGuiClose {
   private Dialog dialog;
   private int selected = 0;
   private List<TextBlockClient> lines = new ArrayList();
   private List<Integer> options = new ArrayList();
   private int rowStart = 0;
   private int rowTotal = 0;
   private int dialogHeight = 180;
   private ResourceLocation wheel;
   private ResourceLocation[] wheelparts;
   private ResourceLocation indicator;
   private boolean isGrabbed = false;
   private int selectedX = 0;
   private int selectedY = 0;

   public GuiDialogInteract(EntityNPCInterface npc, Dialog dialog) {
      super(npc);
      this.dialog = dialog;
      this.appendDialog(dialog);
      this.ySize = 238;
      this.wheel = this.getResource("wheel.png");
      this.indicator = this.getResource("indicator.png");
      this.wheelparts = new ResourceLocation[]{this.getResource("wheel1.png"), this.getResource("wheel2.png"), this.getResource("wheel3.png"), this.getResource("wheel4.png"), this.getResource("wheel5.png"), this.getResource("wheel6.png")};
   }

   public void initGui() {
      super.initGui();
      this.isGrabbed = false;
      this.grabMouse(this.dialog.showWheel);
      this.guiTop = this.height - this.ySize;
      this.calculateRowHeight();
   }

   public void grabMouse(boolean grab) {
      if (grab && !this.isGrabbed) {
         Minecraft.getMinecraft().mouseHelper.grabMouseCursor();
         this.isGrabbed = true;
      } else if (!grab && this.isGrabbed) {
         Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
         this.isGrabbed = false;
      }

   }

   public void drawScreen(int i, int j, float f) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (!this.dialog.hideNPC) {
         int l = -70;
         int i1 = this.ySize;
         this.drawNpc(this.npc, l, i1, 1.4F, 0);
      }

      super.drawScreen(i, j, f);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.enableAlpha();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, 0.5F, 100.065F);
      int count = 0;

      for(TextBlockClient block : this.lines) {
         int size = ClientProxy.Font.width(block.getName() + ": ");
         this.drawString(block.getName() + ": ", -4 - size, block.color, count);

         for(ITextComponent line : block.lines) {
            this.drawString(line.getFormattedText(), 0, block.color, count);
            ++count;
         }

         ++count;
      }

      if (!this.options.isEmpty()) {
         if (!this.dialog.showWheel) {
            this.drawLinedOptions(j);
         } else {
            this.drawWheel();
         }
      }

      GlStateManager.popMatrix();
   }

   private void drawWheel() {
      int yoffset = this.guiTop + this.dialogHeight + 14;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.renderEngine.bindTexture(this.wheel);
      this.drawTexturedModalRect(this.width / 2 - 31, yoffset, 0, 0, 63, 40);
      this.selectedX += Mouse.getDX();
      this.selectedY += Mouse.getDY();
      int limit = 80;
      if (this.selectedX > limit) {
         this.selectedX = limit;
      }

      if (this.selectedX < -limit) {
         this.selectedX = -limit;
      }

      if (this.selectedY > limit) {
         this.selectedY = limit;
      }

      if (this.selectedY < -limit) {
         this.selectedY = -limit;
      }

      this.selected = 1;
      if (this.selectedY < -20) {
         ++this.selected;
      }

      if (this.selectedY > 54) {
         --this.selected;
      }

      if (this.selectedX < 0) {
         this.selected += 3;
      }

      this.mc.renderEngine.bindTexture(this.wheelparts[this.selected]);
      this.drawTexturedModalRect(this.width / 2 - 31, yoffset, 0, 0, 85, 55);
      Iterator var3 = this.dialog.options.keySet().iterator();

      while(var3.hasNext()) {
         int slot = ((Integer)var3.next()).intValue();
         DialogOption option = (DialogOption)this.dialog.options.get(Integer.valueOf(slot));
         if (option != null && option.optionType != 2 && (!option.hasDialog() || option.getDialog().availability.isAvailable(this.player))) {
            int color = option.optionColor;
            if (slot == this.selected) {
               color = 8622040;
            }

            int height = ClientProxy.Font.height(option.title);
            if (slot == 0) {
               this.drawString(this.fontRenderer, option.title, this.width / 2 + 13, yoffset - height, color);
            }

            if (slot == 1) {
               this.drawString(this.fontRenderer, option.title, this.width / 2 + 33, yoffset - height / 2 + 14, color);
            }

            if (slot == 2) {
               this.drawString(this.fontRenderer, option.title, this.width / 2 + 27, yoffset + 27, color);
            }

            if (slot == 3) {
               this.drawString(this.fontRenderer, option.title, this.width / 2 - 13 - ClientProxy.Font.width(option.title), yoffset - height, color);
            }

            if (slot == 4) {
               this.drawString(this.fontRenderer, option.title, this.width / 2 - 33 - ClientProxy.Font.width(option.title), yoffset - height / 2 + 14, color);
            }

            if (slot == 5) {
               this.drawString(this.fontRenderer, option.title, this.width / 2 - 27 - ClientProxy.Font.width(option.title), yoffset + 27, color);
            }
         }
      }

      this.mc.renderEngine.bindTexture(this.indicator);
      this.drawTexturedModalRect(this.width / 2 + this.selectedX / 4 - 2, yoffset + 16 - this.selectedY / 6, 0, 0, 8, 8);
   }

   private void drawLinedOptions(int j) {
      this.drawHorizontalLine(this.guiLeft - 60, this.guiLeft + this.xSize + 120, this.guiTop + this.dialogHeight - ClientProxy.Font.height((String)null) / 3, -1);
      int offset = this.dialogHeight;
      if (j >= this.guiTop + offset) {
         int selected = (j - (this.guiTop + offset)) / ClientProxy.Font.height((String)null);
         if (selected < this.options.size()) {
            this.selected = selected;
         }
      }

      if (this.selected >= this.options.size()) {
         this.selected = 0;
      }

      if (this.selected < 0) {
         this.selected = 0;
      }

      for(int k = 0; k < this.options.size(); ++k) {
         int id = ((Integer)this.options.get(k)).intValue();
         DialogOption option = (DialogOption)this.dialog.options.get(Integer.valueOf(id));
         int y = this.guiTop + offset + k * ClientProxy.Font.height((String)null);
         if (this.selected == k) {
            this.drawString(this.fontRenderer, ">", this.guiLeft - 60, y, 14737632);
         }

         this.drawString(this.fontRenderer, NoppesStringUtils.formatText(option.title, this.player, this.npc), this.guiLeft - 30, y, option.optionColor);
      }

   }

   private void drawString(String text, int left, int color, int count) {
      int height = count - this.rowStart;
      this.drawString(this.fontRenderer, text, this.guiLeft + left, this.guiTop + height * ClientProxy.Font.height((String)null), color);
   }

   public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
      ClientProxy.Font.drawString(text, x, y, color);
   }

   private int getSelected() {
      if (this.selected <= 0) {
         return 0;
      } else {
         return this.selected < this.options.size() ? this.selected : this.options.size() - 1;
      }
   }

   public void keyTyped(char c, int i) {
      if (i == this.mc.gameSettings.keyBindForward.getKeyCode() || i == 200) {
         --this.selected;
      }

      if (i == this.mc.gameSettings.keyBindBack.getKeyCode() || i == 208) {
         ++this.selected;
      }

      if (i == 28) {
         this.handleDialogSelection();
      }

      if (this.closeOnEsc && (i == 1 || this.isInventoryKey(i))) {
         NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, this.dialog.id, Integer.valueOf(-1));
         this.closed();
         this.close();
      }

      super.keyTyped(c, i);
   }

   public void mouseClicked(int i, int j, int k) {
      if (this.selected == -1 && this.options.isEmpty() || this.selected >= 0) {
         this.handleDialogSelection();
      }

   }

   private void handleDialogSelection() {
      int optionId = -1;
      if (this.dialog.showWheel) {
         optionId = this.selected;
      } else if (!this.options.isEmpty()) {
         optionId = ((Integer)this.options.get(this.selected)).intValue();
      }

      NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, this.dialog.id, optionId);
      if (this.dialog != null && this.dialog.hasOtherOptions() && !this.options.isEmpty()) {
         DialogOption option = (DialogOption)this.dialog.options.get(Integer.valueOf(optionId));
         if (option != null && option.optionType == 1) {
            this.lines.add(new TextBlockClient(this.player.getDisplayNameString(), option.title, 280, option.optionColor, new Object[]{this.player, this.npc}));
            this.calculateRowHeight();
            NoppesUtil.clickSound();
         } else {
            this.closed();
            this.close();
         }
      } else {
         this.closed();
         this.close();
      }
   }

   private void closed() {
      this.grabMouse(false);
      NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion);
   }

   public void appendDialog(Dialog dialog) {
      this.closeOnEsc = !dialog.disableEsc;
      this.dialog = dialog;
      this.options = new ArrayList();
      if (dialog.sound != null && !dialog.sound.isEmpty()) {
         MusicController.Instance.stopMusic();
         MusicController.Instance.playSound(SoundCategory.VOICE, dialog.sound, (float)this.npc.posX, (float)this.npc.posY, (float)this.npc.posZ);
      }

      this.lines.add(new TextBlockClient(this.npc, dialog.text, 280, 14737632, new Object[]{this.player, this.npc}));
      Iterator var2 = dialog.options.keySet().iterator();

      while(var2.hasNext()) {
         int slot = ((Integer)var2.next()).intValue();
         DialogOption option = (DialogOption)dialog.options.get(Integer.valueOf(slot));
         if (option != null && option.isAvailable(this.player)) {
            this.options.add(Integer.valueOf(slot));
         }
      }

      this.calculateRowHeight();
      this.grabMouse(dialog.showWheel);
   }

   private void calculateRowHeight() {
      if (this.dialog.showWheel) {
         this.dialogHeight = this.ySize - 58;
      } else {
         this.dialogHeight = this.ySize - 3 * ClientProxy.Font.height((String)null) - 4;
         if (this.dialog.options.size() > 3) {
            this.dialogHeight -= (this.dialog.options.size() - 3) * ClientProxy.Font.height((String)null);
         }
      }

      this.rowTotal = 0;

      for(TextBlockClient block : this.lines) {
         this.rowTotal += block.lines.size() + 1;
      }

      int max = this.dialogHeight / ClientProxy.Font.height((String)null);
      this.rowStart = this.rowTotal - max;
      if (this.rowStart < 0) {
         this.rowStart = 0;
      }

   }

   public void setClose(int i, NBTTagCompound data) {
      this.grabMouse(false);
   }

   public void save() {
   }
}
