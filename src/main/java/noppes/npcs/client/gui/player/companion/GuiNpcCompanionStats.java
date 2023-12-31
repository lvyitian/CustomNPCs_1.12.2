package noppes.npcs.client.gui.player.companion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiMenuTopIconButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

public class GuiNpcCompanionStats extends GuiNPCInterface implements IGuiData {
   private RoleCompanion role;
   private boolean isEating = false;

   public GuiNpcCompanionStats(EntityNPCInterface npc) {
      super(npc);
      this.role = (RoleCompanion)npc.roleInterface;
      this.closeOnEsc = true;
      this.setBackground("companion.png");
      this.xSize = 171;
      this.ySize = 166;
      NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
   }

   public void initGui() {
      super.initGui();
      int y = this.guiTop + 10;
      this.addLabel(new GuiNpcLabel(0, NoppesStringUtils.translate("gui.name", ": ", this.npc.display.getName()), this.guiLeft + 4, y));
      String var10004 = NoppesStringUtils.translate("companion.owner", ": ", this.role.ownerName);
      int var10005 = this.guiLeft + 4;
      y = y + 12;
      this.addLabel(new GuiNpcLabel(1, var10004, var10005, y));
      var10004 = NoppesStringUtils.translate("companion.age", ": ", this.role.ticksActive / 18000L + " (", this.role.stage.name, ")");
      var10005 = this.guiLeft + 4;
      y = y + 12;
      this.addLabel(new GuiNpcLabel(2, var10004, var10005, y));
      var10004 = NoppesStringUtils.translate("companion.strength", ": ", this.npc.stats.melee.getStrength());
      var10005 = this.guiLeft + 4;
      y = y + 12;
      this.addLabel(new GuiNpcLabel(3, var10004, var10005, y));
      var10004 = NoppesStringUtils.translate("companion.level", ": ", this.role.getTotalLevel());
      var10005 = this.guiLeft + 4;
      y = y + 12;
      this.addLabel(new GuiNpcLabel(4, var10004, var10005, y));
      var10004 = NoppesStringUtils.translate("job.name", ": ", "gui.none");
      var10005 = this.guiLeft + 4;
      y = y + 12;
      this.addLabel(new GuiNpcLabel(5, var10004, var10005, y));
      addTopMenu(this.role, this, 1);
   }

   public static void addTopMenu(RoleCompanion role, GuiScreen screen, int active) {
      if (screen instanceof GuiNPCInterface) {
         GuiNPCInterface gui = (GuiNPCInterface)screen;
         GuiMenuTopIconButton button;
         gui.addTopButton(button = new GuiMenuTopIconButton(1, gui.guiLeft + 4, gui.guiTop - 27, "menu.stats", new ItemStack(Items.BOOK)));
         GuiMenuTopIconButton var6;
         gui.addTopButton(var6 = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(Items.NETHER_STAR)));
         if (role.hasInv()) {
            gui.addTopButton(var6 = new GuiMenuTopIconButton(3, var6, "inv.inventory", new ItemStack(Blocks.CHEST)));
         }

         if (role.job != EnumCompanionJobs.NONE) {
            gui.addTopButton(new GuiMenuTopIconButton(4, var6, "job.name", new ItemStack(Items.CARROT)));
         }

         gui.getTopButton(active).active = true;
      }

      if (screen instanceof GuiContainerNPCInterface) {
         GuiContainerNPCInterface gui = (GuiContainerNPCInterface)screen;
         GuiMenuTopIconButton button;
         gui.addTopButton(button = new GuiMenuTopIconButton(1, gui.guiLeft + 4, gui.guiTop - 27, "menu.stats", new ItemStack(Items.BOOK)));
         GuiMenuTopIconButton var8;
         gui.addTopButton(var8 = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(Items.NETHER_STAR)));
         if (role.hasInv()) {
            gui.addTopButton(var8 = new GuiMenuTopIconButton(3, var8, "inv.inventory", new ItemStack(Blocks.CHEST)));
         }

         if (role.job != EnumCompanionJobs.NONE) {
            gui.addTopButton(new GuiMenuTopIconButton(4, var8, "job.name", new ItemStack(Items.CARROT)));
         }

         gui.getTopButton(active).active = true;
      }

   }

   public void actionPerformed(GuiButton guibutton) {
      super.actionPerformed(guibutton);
      int id = guibutton.id;
      if (id == 2) {
         CustomNpcs.proxy.openGui(this.npc, EnumGuiType.CompanionTalent);
      }

      if (id == 3) {
         NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionOpenInv);
      }

   }

   public void drawScreen(int i, int j, float f) {
      super.drawScreen(i, j, f);
      if (this.isEating && !this.role.isEating()) {
         NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
      }

      this.isEating = this.role.isEating();
      super.drawNpc(34, 150);
      int y = this.drawHealth(this.guiTop + 88);
   }

   private int drawHealth(int y) {
      this.mc.getTextureManager().bindTexture(ICONS);
      int max = this.role.getTotalArmorValue();
      if (this.role.talents.containsKey(EnumCompanionTalent.ARMOR) || max > 0) {
         for(int i = 0; i < 10; ++i) {
            int x = this.guiLeft + 66 + i * 10;
            if (i * 2 + 1 < max) {
               this.drawTexturedModalRect(x, y, 34, 9, 9, 9);
            }

            if (i * 2 + 1 == max) {
               this.drawTexturedModalRect(x, y, 25, 9, 9, 9);
            }

            if (i * 2 + 1 > max) {
               this.drawTexturedModalRect(x, y, 16, 9, 9, 9);
            }
         }

         y += 10;
      }

      max = MathHelper.ceil(this.npc.getMaxHealth());
      int k = (int)this.npc.getHealth();
      float scale = 1.0F;
      if (max > 40) {
         scale = (float)max / 40.0F;
         k = (int)((float)k / scale);
         max = 40;
      }

      for(int i = 0; i < max; ++i) {
         int x = this.guiLeft + 66 + i % 20 * 5;
         int offset = i / 20 * 10;
         this.drawTexturedModalRect(x, y + offset, 52 + i % 2 * 5, 9, i % 2 == 1 ? 4 : 5, 9);
         if (k > i) {
            this.drawTexturedModalRect(x, y + offset, 52 + i % 2 * 5, 0, i % 2 == 1 ? 4 : 5, 9);
         }
      }

      k = this.role.foodstats.getFoodLevel();
      y = y + 10;
      if (max > 20) {
         y += 10;
      }

      for(int i = 0; i < 20; ++i) {
         int x = this.guiLeft + 66 + i % 20 * 5;
         this.drawTexturedModalRect(x, y, 16 + i % 2 * 5, 27, i % 2 == 1 ? 4 : 5, 9);
         if (k > i) {
            this.drawTexturedModalRect(x, y, 52 + i % 2 * 5, 27, i % 2 == 1 ? 4 : 5, 9);
         }
      }

      return y;
   }

   public void save() {
   }

   public void setGuiData(NBTTagCompound compound) {
      this.role.readFromNBT(compound);
   }
}
