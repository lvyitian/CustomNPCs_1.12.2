package noppes.npcs.client.gui.global;

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

public class GuiNPCQuestSelection extends GuiNPCInterface implements IScrollData {
   private GuiNPCStringSlot slot;
   private GuiScreen parent;
   private HashMap<String, Integer> data;
   private boolean selectCategory = true;
   public GuiSelectionListener listener;
   private int quest;

   public GuiNPCQuestSelection(EntityNPCInterface npc, GuiScreen parent, int quest) {
      super(npc);
      this.drawDefaultBackground = false;
      this.title = "";
      this.parent = parent;
      this.data = new HashMap();
      this.quest = quest;
      if (parent instanceof GuiSelectionListener) {
         this.listener = (GuiSelectionListener)parent;
      }

   }

   public void initPacket() {
      if (this.quest >= 0) {
         Client.sendData(EnumPacketServer.QuestsGetFromQuest, this.quest);
         this.selectCategory = false;
         this.title = "";
      } else {
         Client.sendData(EnumPacketServer.QuestCategoriesGet, this.quest);
      }

   }

   public void initGui() {
      super.initGui();
      Vector<String> list = new Vector();
      this.slot = new GuiNPCStringSlot(list, this, false, 18);
      this.slot.registerScrollButtons(4, 5);
      this.addButton(new GuiNpcButton(2, this.width / 2 - 100, this.height - 41, 98, 20, "gui.back"));
      this.addButton(new GuiNpcButton(4, this.width / 2 + 2, this.height - 41, 98, 20, "mco.template.button.select"));
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
            this.title = "";
            this.selectCategory = true;
            Client.sendData(EnumPacketServer.QuestCategoriesGet, this.quest);
         }
      }

      if (id == 4) {
         if (this.slot.selected == null || this.slot.selected.isEmpty()) {
            return;
         }

         this.doubleClicked();
      }

   }

   public String getSelected() {
      return this.slot.selected;
   }

   public void doubleClicked() {
      if (this.slot.selected != null && !this.slot.selected.isEmpty()) {
         if (this.selectCategory) {
            this.selectCategory = false;
            this.title = "";
            Client.sendData(EnumPacketServer.QuestsGet, this.data.get(this.slot.selected));
         } else {
            this.quest = ((Integer)this.data.get(this.slot.selected)).intValue();
            this.close();
            NoppesUtil.openGUI(this.player, this.parent);
         }

      }
   }

   public void save() {
      if (this.quest >= 0 && this.listener != null) {
         this.listener.selected(this.quest, this.slot.selected);
      }

   }

   public void setData(Vector<String> list, HashMap<String, Integer> data) {
      this.data = data;
      this.slot.setList(list);
      if (this.quest >= 0) {
         for(String name : data.keySet()) {
            if (((Integer)data.get(name)).intValue() == this.quest) {
               this.slot.selected = name;
            }
         }
      }

   }

   public void setSelected(String selected) {
   }
}
