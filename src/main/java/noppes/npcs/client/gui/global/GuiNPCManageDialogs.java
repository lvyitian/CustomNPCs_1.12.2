package noppes.npcs.client.gui.global;

import java.util.HashMap;
import java.util.Vector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNpcSoundSelection;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageDialogs extends GuiNPCInterface2 implements IScrollData, ISubGuiListener, GuiSelectionListener, ICustomScrollListener, ITextfieldListener, IGuiData {
   private GuiCustomScroll scroll;
   private HashMap<String, Integer> data = new HashMap();
   private Dialog dialog = new Dialog((DialogCategory)null);
   private DialogCategory category = new DialogCategory();
   private boolean categorySelection = true;
   private GuiNpcSoundSelection gui;

   public GuiNPCManageDialogs(EntityNPCInterface npc) {
      super(npc);
      Client.sendData(EnumPacketServer.DialogCategoriesGet);
   }

   public void initGui() {
      super.initGui();
      this.addButton(new GuiNpcButton(0, this.guiLeft + 358, this.guiTop + 8, 58, 20, this.categorySelection ? "dialog.dialogs" : "gui.categories"));
      this.addButton(new GuiNpcButton(1, this.guiLeft + 358, this.guiTop + 38, 58, 20, "gui.add"));
      this.addButton(new GuiNpcButton(2, this.guiLeft + 358, this.guiTop + 61, 58, 20, "gui.remove"));
      if (this.scroll == null) {
         this.scroll = new GuiCustomScroll(this, 0);
         this.scroll.setSize(143, 208);
      }

      this.scroll.guiLeft = this.guiLeft + 214;
      this.scroll.guiTop = this.guiTop + 4;
      this.addScroll(this.scroll);
      if (this.categorySelection && this.category.id >= 0) {
         this.categoryGuiInit();
      }

      if (!this.categorySelection && this.dialog.id >= 0) {
         this.dialogGuiInit();
      }

   }

   private void dialogGuiInit() {
      this.addLabel(new GuiNpcLabel(1, "gui.title", this.guiLeft + 4, this.guiTop + 8));
      this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 36, this.guiTop + 3, 140, 20, this.dialog.title));
      this.addLabel(new GuiNpcLabel(0, "ID", this.guiLeft + 178, this.guiTop + 4));
      this.addLabel(new GuiNpcLabel(2, this.dialog.id + "", this.guiLeft + 178, this.guiTop + 14));
      this.addLabel(new GuiNpcLabel(3, "dialog.dialogtext", this.guiLeft + 4, this.guiTop + 30));
      this.addButton(new GuiNpcButton(3, this.guiLeft + 120, this.guiTop + 25, 50, 20, "selectServer.edit"));
      this.addLabel(new GuiNpcLabel(4, "availability.options", this.guiLeft + 4, this.guiTop + 51));
      this.addButton(new GuiNpcButton(4, this.guiLeft + 120, this.guiTop + 46, 50, 20, "selectServer.edit"));
      this.addLabel(new GuiNpcLabel(5, "faction.options", this.guiLeft + 4, this.guiTop + 72));
      this.addButton(new GuiNpcButton(5, this.guiLeft + 120, this.guiTop + 67, 50, 20, "selectServer.edit"));
      this.addLabel(new GuiNpcLabel(6, "dialog.options", this.guiLeft + 4, this.guiTop + 93));
      this.addButton(new GuiNpcButton(6, this.guiLeft + 120, this.guiTop + 89, 50, 20, "selectServer.edit"));
      this.addButton(new GuiNpcButton(7, this.guiLeft + 4, this.guiTop + 114, 144, 20, "availability.selectquest"));
      this.addButton(new GuiNpcButton(8, this.guiLeft + 150, this.guiTop + 114, 20, 20, "X"));
      this.addLabel(new GuiNpcLabel(9, "gui.selectSound", this.guiLeft + 4, this.guiTop + 138));
      this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 148, 144, 20, this.dialog.sound));
      this.addButton(new GuiNpcButton(9, this.guiLeft + 150, this.guiTop + 148, 60, 20, "mco.template.button.select"));
      this.addButton(new GuiNpcButton(10, this.guiLeft + 4, this.guiTop + 172, 120, 20, "gui.extraoptions"));
   }

   private void categoryGuiInit() {
      this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 8, this.guiTop + 8, 160, 16, this.category.title));
   }

   public void elementClicked() {
      this.getTextField(2).setText(this.gui.getSelected());
      this.unFocused(this.getTextField(2));
   }

   public void buttonEvent(GuiButton guibutton) {
      int id = guibutton.id;
      if (id == 0) {
         this.save();
         if (this.categorySelection) {
            if (this.category.id < 0) {
               return;
            }

            this.dialog = new Dialog(this.category);
            Client.sendData(EnumPacketServer.DialogsGet, this.category.id);
         } else if (!this.categorySelection) {
            this.dialog = new Dialog((DialogCategory)null);
            this.category = new DialogCategory();
            Client.sendData(EnumPacketServer.DialogCategoriesGet);
         }

         this.categorySelection = !this.categorySelection;
         this.getButton(0).setEnabled(false);
         this.scroll.clear();
         this.data.clear();
      }

      if (id == 1) {
         this.save();

         String name;
         for(name = I18n.translateToLocal("gui.new"); this.data.containsKey(name); name = name + "_") {
            ;
         }

         if (this.categorySelection) {
            DialogCategory category = new DialogCategory();
            category.title = name;
            Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
         } else {
            Dialog dialog = new Dialog(this.category);
            dialog.title = name;
            Client.sendData(EnumPacketServer.DialogSave, this.category.id, dialog.writeToNBT(new NBTTagCompound()));
         }
      }

      if (id == 2 && this.data.containsKey(this.scroll.getSelected())) {
         if (this.categorySelection) {
            Client.sendData(EnumPacketServer.DialogCategoryRemove, this.category.id);
            this.category = new DialogCategory();
         } else {
            Client.sendData(EnumPacketServer.DialogRemove, this.dialog.id);
            this.dialog = new Dialog(this.category);
         }

         this.scroll.clear();
      }

      if (id == 3 && this.dialog.id >= 0) {
         this.setSubGui(new SubGuiNpcTextArea(this.dialog.text));
      }

      if (id == 4 && this.dialog.id >= 0) {
         this.setSubGui(new SubGuiNpcAvailability(this.dialog.availability));
      }

      if (id == 5 && this.dialog.id >= 0) {
         this.setSubGui(new SubGuiNpcFactionOptions(this.dialog.factionOptions));
      }

      if (id == 6 && this.dialog.id >= 0) {
         this.setSubGui(new SubGuiNpcDialogOptions(this.dialog));
      }

      if (id == 7 && this.dialog.id >= 0) {
         NoppesUtil.openGUI(this.player, new GuiNPCQuestSelection(this.npc, this, this.dialog.quest));
      }

      if (id == 8 && this.dialog.id >= 0) {
         this.dialog.quest = -1;
         this.initGui();
      }

      if (id == 9 && this.dialog.id >= 0) {
         NoppesUtil.openGUI(this.player, this.gui = new GuiNpcSoundSelection(this, this.getTextField(2).getText()));
      }

      if (id == 10) {
         this.setSubGui(new SubGuiNpcDialogExtra(this.dialog, this));
      }

   }

   public void unFocused(GuiNpcTextField guiNpcTextField) {
      if (guiNpcTextField.getId() == 0) {
         if (this.category.id < 0) {
            guiNpcTextField.setText("");
         } else {
            String name = guiNpcTextField.getText();
            if (!name.isEmpty() && !this.data.containsKey(name)) {
               if (this.categorySelection && this.category.id >= 0) {
                  String old = this.category.title;
                  this.data.remove(this.category.title);
                  this.category.title = name;
                  this.data.put(this.category.title, Integer.valueOf(this.category.id));
                  this.scroll.replace(old, this.category.title);
               }
            } else {
               guiNpcTextField.setText(this.category.title);
            }
         }
      }

      if (guiNpcTextField.getId() == 1) {
         if (this.dialog.id < 0) {
            guiNpcTextField.setText("");
         } else {
            String name = guiNpcTextField.getText();
            if (!name.isEmpty() && !this.data.containsKey(name)) {
               if (!this.categorySelection && this.dialog.id >= 0) {
                  String old = this.dialog.title;
                  this.data.remove(old);
                  this.dialog.title = name;
                  this.data.put(this.dialog.title, Integer.valueOf(this.dialog.id));
                  this.scroll.replace(old, this.dialog.title);
               }
            } else {
               guiNpcTextField.setText(this.dialog.title);
            }
         }
      }

      if (guiNpcTextField.getId() == 2) {
         this.dialog.sound = guiNpcTextField.getText();
      }

   }

   public void setGuiData(NBTTagCompound compound) {
      if (this.categorySelection) {
         this.category.readNBT(compound);
         this.setSelected(this.category.title);
         this.initGui();
      } else {
         this.dialog.readNBT(compound);
         this.setSelected(this.dialog.title);
         this.initGui();
         if (compound.hasKey("DialogQuestName")) {
            this.getButton(7).setDisplayText(compound.getString("DialogQuestName"));
         }
      }

   }

   public void subGuiClosed(SubGuiInterface subgui) {
      if (subgui instanceof SubGuiNpcTextArea) {
         SubGuiNpcTextArea gui = (SubGuiNpcTextArea)subgui;
         this.dialog.text = gui.text;
      }

      if (subgui instanceof SubGuiNpcDialogOption) {
         this.setSubGui(new SubGuiNpcDialogOptions(this.dialog));
      }

      if (subgui instanceof SubGuiNpcCommand) {
         this.dialog.command = ((SubGuiNpcCommand)subgui).command;
      }

   }

   public void setData(Vector<String> list, HashMap<String, Integer> data) {
      this.getButton(0).setEnabled(true);
      String name = this.scroll.getSelected();
      this.data = data;
      this.scroll.setList(list);
      if (name != null) {
         this.scroll.setSelected(name);
      }

      this.initGui();
   }

   public void setSelected(String selected) {
   }

   public void selected(int ob, String name) {
      this.dialog.quest = ob;
      Client.sendData(EnumPacketServer.DialogSave, this.category.id, this.dialog.writeToNBT(new NBTTagCompound()));
      Client.sendData(EnumPacketServer.DialogGet, this.dialog.id);
   }

   public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
      if (guiCustomScroll.id == 0) {
         this.save();
         String selected = this.scroll.getSelected();
         if (this.categorySelection) {
            this.category = new DialogCategory();
            Client.sendData(EnumPacketServer.DialogCategoryGet, this.data.get(selected));
         } else {
            this.dialog = new Dialog(this.category);
            Client.sendData(EnumPacketServer.DialogGet, this.data.get(selected));
         }
      }

   }

   public void save() {
      GuiNpcTextField.unfocus();
      if (!this.categorySelection && this.dialog.id >= 0) {
         Client.sendData(EnumPacketServer.DialogSave, this.category.id, this.dialog.writeToNBT(new NBTTagCompound()));
      } else if (this.categorySelection && this.category.id >= 0) {
         Client.sendData(EnumPacketServer.DialogCategorySave, this.category.writeNBT(new NBTTagCompound()));
      }

   }
}
