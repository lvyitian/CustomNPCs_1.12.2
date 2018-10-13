package noppes.npcs.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCStringSlot;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCDialogSelection extends GuiNPCInterface implements IScrollData {
   private GuiNPCStringSlot slot;
   private GuiScreen parent;
   private HashMap<String, Integer> data = new HashMap();
   private int dialog;
   private boolean selectCategory = true;
   public GuiSelectionListener listener;

   public GuiNPCDialogSelection(EntityNPCInterface npc, GuiScreen parent, int dialog) {
      super(npc);
      this.drawDefaultBackground = false;
      this.title = "Select Dialog Category";
      this.parent = parent;
      this.dialog = dialog;
      if (parent instanceof GuiSelectionListener) {
         this.listener = (GuiSelectionListener)parent;
      }

   }

   public void initPacket() {
      if (this.dialog >= 0) {
         Client.sendData(EnumPacketServer.DialogsGetFromDialog, this.dialog);
         this.selectCategory = false;
         this.title = "Select Dialog";
      } else {
         Client.sendData(EnumPacketServer.DialogCategoriesGet, this.dialog);
         this.title = "Select Dialog Category";
      }

   }

   public void initGui() {
      super.initGui();
      Vector<String> list = new Vector();
      this.addButton(new GuiNpcButton(2, this.width / 2 - 100, this.height - 41, 98, 20, "gui.back"));
      this.addButton(new GuiNpcButton(4, this.width / 2 + 2, this.height - 41, 98, 20, "mco.template.button.select"));
      this.slot = new GuiNPCStringSlot(list, this, false, 18);
      this.slot.registerScrollButtons(4, 5);
   }

   public void handleMouseInput() throws IOException {
      this.slot.handleMouseInput();
      super.handleMouseInput();
   }

   public void drawScreen(int i, int j, float f) {
      this.slot.drawScreen(i, j, f);
      super.drawScreen(i, j, f);
   }

   protected void actionPerformed(GuiButton guibutton) {
      int id = guibutton.id;
      if (id == 2) {
         if (this.selectCategory) {
            this.close();
            NoppesUtil.openGUI(this.player, this.parent);
         } else {
            this.title = "Select Dialog Category";
            this.selectCategory = true;
            Client.sendData(EnumPacketServer.DialogCategoriesGet, this.dialog);
         }
      }

      if (id == 4) {
         this.doubleClicked();
      }

   }

   public void doubleClicked() {
      if (this.slot.selected != null && !this.slot.selected.isEmpty()) {
         if (this.selectCategory) {
            this.selectCategory = false;
            this.title = "Select Dialog";
            Client.sendData(EnumPacketServer.DialogsGet, this.data.get(this.slot.selected));
         } else {
            this.dialog = ((Integer)this.data.get(this.slot.selected)).intValue();
            this.close();
            NoppesUtil.openGUI(this.player, this.parent);
         }

      }
   }

   public void save() {
      if (this.dialog >= 0 && this.listener != null) {
         this.listener.selected(this.dialog, this.slot.selected);
      }

   }

   public void setData(Vector<String> list, HashMap<String, Integer> data) {
      this.data = data;
      this.slot.setList(list);
      if (this.dialog >= 0) {
         for(String name : data.keySet()) {
            if (((Integer)data.get(name)).intValue() == this.dialog) {
               this.slot.selected = name;
            }
         }
      }

   }

   public void setSelected(String selected) {
   }
}
