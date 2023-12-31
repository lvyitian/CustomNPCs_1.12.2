package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcMobSpawnerSelector extends SubGuiInterface implements IGuiData {
   private GuiCustomScroll scroll;
   private List<String> list;
   private static String search = "";
   public int activeTab = 1;
   public boolean isServer = false;

   public GuiNpcMobSpawnerSelector() {
      this.xSize = 256;
      this.closeOnEsc = true;
      this.setBackground("menubg.png");
   }

   public void initGui() {
      super.initGui();
      if (this.scroll == null) {
         this.scroll = new GuiCustomScroll(this, 0);
         this.scroll.setSize(165, 188);
      } else {
         this.scroll.clear();
      }

      this.scroll.guiLeft = this.guiLeft + 4;
      this.scroll.guiTop = this.guiTop + 26;
      this.addScroll(this.scroll);
      this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 4, 165, 20, search));
      this.addButton(new GuiNpcButton(0, this.guiLeft + 171, this.guiTop + 80, 80, 20, "gui.done"));
      this.addButton(new GuiNpcButton(1, this.guiLeft + 171, this.guiTop + 103, 80, 20, "gui.cancel"));
      this.addSideButton(new GuiMenuSideButton(21, this.guiLeft - 69, this.guiTop + 2, 70, 22, "Tab 1"));
      this.addSideButton(new GuiMenuSideButton(22, this.guiLeft - 69, this.guiTop + 23, 70, 22, "Tab 2"));
      this.addSideButton(new GuiMenuSideButton(23, this.guiLeft - 69, this.guiTop + 44, 70, 22, "Tab 3"));
      this.addSideButton(new GuiMenuSideButton(24, this.guiLeft - 69, this.guiTop + 65, 70, 22, "Tab 4"));
      this.addSideButton(new GuiMenuSideButton(25, this.guiLeft - 69, this.guiTop + 86, 70, 22, "Tab 5"));
      this.addSideButton(new GuiMenuSideButton(26, this.guiLeft - 69, this.guiTop + 107, 70, 22, "Tab 6"));
      this.addSideButton(new GuiMenuSideButton(27, this.guiLeft - 69, this.guiTop + 128, 70, 22, "Tab 7"));
      this.addSideButton(new GuiMenuSideButton(28, this.guiLeft - 69, this.guiTop + 149, 70, 22, "Tab 8"));
      this.addSideButton(new GuiMenuSideButton(29, this.guiLeft - 69, this.guiTop + 170, 70, 22, "Tab 9"));
      this.getSideButton(20 + this.activeTab).active = true;
      this.showClones();
   }

   public String getSelected() {
      return this.scroll.getSelected();
   }

   private void showClones() {
      if (this.isServer) {
         Client.sendData(EnumPacketServer.CloneList, this.activeTab);
      } else {
         new ArrayList();
         this.list = new ArrayList(ClientCloneController.Instance.getClones(this.activeTab));
         this.scroll.setList(this.getSearchList());
      }
   }

   public void keyTyped(char c, int i) {
      super.keyTyped(c, i);
      if (!search.equals(this.getTextField(1).getText())) {
         search = this.getTextField(1).getText().toLowerCase();
         this.scroll.setList(this.getSearchList());
      }
   }

   private List<String> getSearchList() {
      if (search.isEmpty()) {
         return new ArrayList(this.list);
      } else {
         List<String> list = new ArrayList();

         for(String name : this.list) {
            if (name.toLowerCase().contains(search)) {
               list.add(name);
            }
         }

         return list;
      }
   }

   public NBTTagCompound getCompound() {
      String sel = this.scroll.getSelected();
      return sel == null ? null : ClientCloneController.Instance.getCloneData(this.player, sel, this.activeTab);
   }

   public void buttonEvent(GuiButton guibutton) {
      int id = guibutton.id;
      if (id == 0) {
         this.close();
      }

      if (id == 1) {
         this.scroll.clear();
         this.close();
      }

      if (id > 20) {
         this.activeTab = id - 20;
         this.initGui();
      }

   }

   protected NBTTagList newDoubleNBTList(double... par1ArrayOfDouble) {
      NBTTagList nbttaglist = new NBTTagList();

      for(double d1 : par1ArrayOfDouble) {
         nbttaglist.appendTag(new NBTTagDouble(d1));
      }

      return nbttaglist;
   }

   public void save() {
   }

   public void setGuiData(NBTTagCompound compound) {
      NBTTagList nbtlist = compound.getTagList("List", 8);
      List<String> list = new ArrayList();

      for(int i = 0; i < nbtlist.tagCount(); ++i) {
         list.add(nbtlist.getStringTagAt(i));
      }

      this.list = list;
      this.scroll.setList(this.getSearchList());
   }
}
