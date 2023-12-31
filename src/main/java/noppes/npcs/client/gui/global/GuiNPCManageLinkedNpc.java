package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageLinkedNpc extends GuiNPCInterface2 implements IScrollData, ISubGuiListener {
   private GuiCustomScroll scroll;
   private List<String> data = new ArrayList();
   public static GuiScreen Instance;

   public GuiNPCManageLinkedNpc(EntityNPCInterface npc) {
      super(npc);
      Instance = this;
      Client.sendData(EnumPacketServer.LinkedGetAll);
   }

   public void initGui() {
      super.initGui();
      this.addButton(new GuiNpcButton(1, this.guiLeft + 358, this.guiTop + 38, 58, 20, "gui.add"));
      this.addButton(new GuiNpcButton(2, this.guiLeft + 358, this.guiTop + 61, 58, 20, "gui.remove"));
      if (this.scroll == null) {
         this.scroll = new GuiCustomScroll(this, 0);
         this.scroll.setSize(143, 208);
      }

      this.scroll.guiLeft = this.guiLeft + 214;
      this.scroll.guiTop = this.guiTop + 4;
      this.scroll.setList(this.data);
      this.addScroll(this.scroll);
   }

   public void buttonEvent(GuiButton button) {
      if (button.id == 1) {
         this.save();
         this.setSubGui(new SubGuiEditText("New"));
      }

      if (button.id == 2 && this.scroll.hasSelected()) {
         Client.sendData(EnumPacketServer.LinkedRemove, this.scroll.getSelected());
      }

   }

   public void subGuiClosed(SubGuiInterface subgui) {
      if (!((SubGuiEditText)subgui).cancelled) {
         Client.sendData(EnumPacketServer.LinkedAdd, ((SubGuiEditText)subgui).text);
      }

   }

   public void setData(Vector<String> list, HashMap<String, Integer> data) {
      this.data = new ArrayList(list);
      this.initGui();
   }

   public void setSelected(String selected) {
   }

   public void save() {
   }
}
