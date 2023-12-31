package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMailData;

public class GuiMailbox extends GuiNPCInterface implements IGuiData, ICustomScrollListener, GuiYesNoCallback {
   private GuiCustomScroll scroll;
   private PlayerMailData data;
   private PlayerMail selected;

   public GuiMailbox() {
      this.xSize = 256;
      this.setBackground("menubg.png");
      NoppesUtilPlayer.sendData(EnumPlayerPacket.MailGet);
   }

   public void initGui() {
      super.initGui();
      if (this.scroll == null) {
         this.scroll = new GuiCustomScroll(this, 0);
         this.scroll.setSize(165, 186);
      }

      this.scroll.guiLeft = this.guiLeft + 4;
      this.scroll.guiTop = this.guiTop + 4;
      this.addScroll(this.scroll);
      String title = I18n.translateToLocal("mailbox.name");
      int x = (this.xSize - this.fontRenderer.getStringWidth(title)) / 2;
      this.addLabel(new GuiNpcLabel(0, title, this.guiLeft + x, this.guiTop - 8));
      if (this.selected != null) {
         this.addLabel(new GuiNpcLabel(3, I18n.translateToLocal("mailbox.sender") + ":", this.guiLeft + 170, this.guiTop + 6));
         this.addLabel(new GuiNpcLabel(1, this.selected.sender, this.guiLeft + 174, this.guiTop + 18));
         this.addLabel(new GuiNpcLabel(2, I18n.translateToLocalFormatted("mailbox.timesend", new Object[]{this.getTimePast()}), this.guiLeft + 174, this.guiTop + 30));
      }

      this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 192, 82, 20, "mailbox.read"));
      this.addButton(new GuiNpcButton(1, this.guiLeft + 88, this.guiTop + 192, 82, 20, "selectWorld.deleteButton"));
      this.getButton(1).setEnabled(this.selected != null);
   }

   private String getTimePast() {
      if (this.selected.timePast > 86400000L) {
         int days = (int)(this.selected.timePast / 86400000L);
         return days == 1 ? days + " " + I18n.translateToLocal("mailbox.day") : days + " " + I18n.translateToLocal("mailbox.days");
      } else if (this.selected.timePast > 3600000L) {
         int hours = (int)(this.selected.timePast / 3600000L);
         return hours == 1 ? hours + " " + I18n.translateToLocal("mailbox.hour") : hours + " " + I18n.translateToLocal("mailbox.hours");
      } else {
         int minutes = (int)(this.selected.timePast / 60000L);
         return minutes == 1 ? minutes + " " + I18n.translateToLocal("mailbox.minutes") : minutes + " " + I18n.translateToLocal("mailbox.minutes");
      }
   }

   public void confirmClicked(boolean flag, int i) {
      if (flag && this.selected != null) {
         NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, this.selected.time, this.selected.sender);
         this.selected = null;
      }

      NoppesUtil.openGUI(this.player, this);
   }

   protected void actionPerformed(GuiButton guibutton) {
      int id = guibutton.id;
      if (this.scroll.selected >= 0) {
         if (id == 0) {
            GuiMailmanWrite.parent = this;
            GuiMailmanWrite.mail = this.selected;
            NoppesUtilPlayer.sendData(EnumPlayerPacket.MailboxOpenMail, this.selected.time, this.selected.sender);
            this.selected = null;
            this.scroll.selected = -1;
         }

         if (id == 1) {
            GuiYesNo guiyesno = new GuiYesNo(this, "", I18n.translateToLocal("gui.deleteMessage"), 0);
            this.displayGuiScreen(guiyesno);
         }

      }
   }

   public void mouseClicked(int i, int j, int k) {
      super.mouseClicked(i, j, k);
      this.scroll.mouseClicked(i, j, k);
   }

   public void keyTyped(char c, int i) {
      if (i == 1 || this.isInventoryKey(i)) {
         this.close();
      }

   }

   public void save() {
   }

   public void setGuiData(NBTTagCompound compound) {
      PlayerMailData data = new PlayerMailData();
      data.loadNBTData(compound);
      List<String> list = new ArrayList();
      Collections.sort(data.playermail, new GuiMailbox$1(this));

      for(PlayerMail mail : data.playermail) {
         list.add(mail.subject);
      }

      this.data = data;
      this.scroll.clear();
      this.selected = null;
      this.scroll.setUnsortedList(list);
   }

   public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
      this.selected = (PlayerMail)this.data.playermail.get(guiCustomScroll.selected);
      this.initGui();
      if (this.selected != null && !this.selected.beenRead) {
         this.selected.beenRead = true;
         NoppesUtilPlayer.sendData(EnumPlayerPacket.MailRead, this.selected.time, this.selected.sender);
      }

   }
}
