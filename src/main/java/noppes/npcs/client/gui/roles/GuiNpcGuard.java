package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;

public class GuiNpcGuard extends GuiNPCInterface2 {
   private JobGuard role;
   private GuiCustomScroll scroll1;
   private GuiCustomScroll scroll2;

   public GuiNpcGuard(EntityNPCInterface npc) {
      super(npc);
      this.role = (JobGuard)npc.jobInterface;
   }

   public void initGui() {
      super.initGui();
      this.addButton(new GuiNpcButton(0, this.guiLeft + 10, this.guiTop + 4, 100, 20, "guard.animals"));
      this.addButton(new GuiNpcButton(1, this.guiLeft + 140, this.guiTop + 4, 100, 20, "guard.mobs"));
      this.addButton(new GuiNpcButton(2, this.guiLeft + 275, this.guiTop + 4, 100, 20, "guard.creepers"));
      if (this.scroll1 == null) {
         this.scroll1 = new GuiCustomScroll(this, 0);
         this.scroll1.setSize(175, 154);
      }

      this.scroll1.guiLeft = this.guiLeft + 4;
      this.scroll1.guiTop = this.guiTop + 58;
      this.addScroll(this.scroll1);
      this.addLabel(new GuiNpcLabel(11, "guard.availableTargets", this.guiLeft + 4, this.guiTop + 48));
      if (this.scroll2 == null) {
         this.scroll2 = new GuiCustomScroll(this, 1);
         this.scroll2.setSize(175, 154);
      }

      this.scroll2.guiLeft = this.guiLeft + 235;
      this.scroll2.guiTop = this.guiTop + 58;
      this.addScroll(this.scroll2);
      this.addLabel(new GuiNpcLabel(12, "guard.currentTargets", this.guiLeft + 235, this.guiTop + 48));
      List<String> all = new ArrayList();

      for(EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
         Class<? extends Entity> cl = ent.getEntityClass();
         String name = "entity." + ent.getName() + ".name";
         if (!this.role.targets.contains(name) && !EntityNPCInterface.class.isAssignableFrom(cl) && EntityLivingBase.class.isAssignableFrom(cl)) {
            all.add(name);
         }
      }

      this.scroll1.setList(all);
      this.scroll2.setList(this.role.targets);
      this.addButton(new GuiNpcButton(11, this.guiLeft + 180, this.guiTop + 80, 55, 20, ">"));
      this.addButton(new GuiNpcButton(12, this.guiLeft + 180, this.guiTop + 102, 55, 20, "<"));
      this.addButton(new GuiNpcButton(13, this.guiLeft + 180, this.guiTop + 130, 55, 20, ">>"));
      this.addButton(new GuiNpcButton(14, this.guiLeft + 180, this.guiTop + 152, 55, 20, "<<"));
   }

   protected void actionPerformed(GuiButton guibutton) {
      GuiNpcButton button = (GuiNpcButton)guibutton;
      if (button.id == 0) {
         for(EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
            Class<? extends Entity> cl = ent.getEntityClass();
            String name = "entity." + ent.getName() + ".name";
            if (EntityAnimal.class.isAssignableFrom(cl) && !this.role.targets.contains(name)) {
               this.role.targets.add(name);
            }
         }

         this.scroll1.selected = -1;
         this.scroll2.selected = -1;
         this.initGui();
      }

      if (button.id == 1) {
         for(EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
            Class<? extends Entity> cl = ent.getEntityClass();
            String name = "entity." + ent.getName() + ".name";
            if (EntityMob.class.isAssignableFrom(cl) && !EntityCreeper.class.isAssignableFrom(cl) && !this.role.targets.contains(name)) {
               this.role.targets.add(name);
            }
         }

         this.scroll1.selected = -1;
         this.scroll2.selected = -1;
         this.initGui();
      }

      if (button.id == 2) {
         for(EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
            Class<? extends Entity> cl = ent.getEntityClass();
            String name = "entity." + ent.getName() + ".name";
            if (EntityCreeper.class.isAssignableFrom(cl) && !this.role.targets.contains(name)) {
               this.role.targets.add(name);
            }
         }

         this.scroll1.selected = -1;
         this.scroll2.selected = -1;
         this.initGui();
      }

      if (button.id == 11 && this.scroll1.hasSelected()) {
         this.role.targets.add(this.scroll1.getSelected());
         this.scroll1.selected = -1;
         this.scroll2.selected = -1;
         this.initGui();
      }

      if (button.id == 12 && this.scroll2.hasSelected()) {
         this.role.targets.remove(this.scroll2.getSelected());
         this.scroll2.selected = -1;
         this.initGui();
      }

      if (button.id == 13) {
         this.role.targets.clear();
         List<String> all = new ArrayList();

         for(EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
            Class<? extends Entity> cl = ent.getEntityClass();
            String name = "entity." + ent.getName() + ".name";
            if (EntityLivingBase.class.isAssignableFrom(cl) && !EntityNPCInterface.class.isAssignableFrom(cl)) {
               all.add(name);
            }
         }

         this.role.targets = all;
         this.scroll1.selected = -1;
         this.scroll2.selected = -1;
         this.initGui();
      }

      if (button.id == 14) {
         this.role.targets.clear();
         this.scroll1.selected = -1;
         this.scroll2.selected = -1;
         this.initGui();
      }

   }

   public void save() {
      Client.sendData(EnumPacketServer.JobSave, this.role.writeToNBT(new NBTTagCompound()));
   }
}
