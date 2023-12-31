package noppes.npcs.client.gui;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcMobSpawnerMounter extends GuiNPCInterface implements IGuiData {
   private GuiCustomScroll scroll;
   private int posX;
   private int posY;
   private int posZ;
   private List<String> list;
   private static int showingClones = 0;
   private static String search = "";
   private int activeTab = 1;

   public GuiNpcMobSpawnerMounter(int i, int j, int k) {
      this.xSize = 256;
      this.posX = i;
      this.posY = j;
      this.posZ = k;
      this.closeOnEsc = true;
      this.setBackground("menubg.png");
   }

   public void initGui() {
      super.initGui();
      this.guiTop += 10;
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
      GuiMenuTopButton button;
      this.addTopButton(button = new GuiMenuTopButton(3, this.guiLeft + 4, this.guiTop - 17, "spawner.clones"));
      button.active = showingClones == 0;
      GuiMenuTopButton var2;
      this.addTopButton(var2 = new GuiMenuTopButton(4, button, "spawner.entities"));
      var2.active = showingClones == 1;
      this.addTopButton(button = new GuiMenuTopButton(5, var2, "gui.server"));
      button.active = showingClones == 2;
      this.addButton(new GuiNpcButton(1, this.guiLeft + 170, this.guiTop + 6, 82, 20, "spawner.mount"));
      this.addButton(new GuiNpcButton(2, this.guiLeft + 170, this.guiTop + 50, 82, 20, "spawner.mountplayer"));
      if (showingClones != 0 && showingClones != 2) {
         this.showEntities();
      } else {
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

   }

   private void showEntities() {
      ArrayList<String> list = new ArrayList();

      for(EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
         Class<? extends Entity> c = ent.getEntityClass();
         String name = ent.getName();

         try {
            if (EntityLiving.class.isAssignableFrom(c) && c.getConstructor(World.class) != null && !Modifier.isAbstract(c.getModifiers())) {
               list.add(name.toString());
            }
         } catch (SecurityException var7) {
            var7.printStackTrace();
         } catch (NoSuchMethodException var8) {
            ;
         }
      }

      this.list = list;
      this.scroll.setList(this.getSearchList());
   }

   private void showClones() {
      if (showingClones == 2) {
         Client.sendData(EnumPacketServer.CloneList, this.activeTab);
      } else {
         new ArrayList();
         this.list = ClientCloneController.Instance.getClones(this.activeTab);
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

   private NBTTagCompound getCompound() {
      String sel = this.scroll.getSelected();
      if (sel == null) {
         return null;
      } else if (showingClones == 0) {
         return ClientCloneController.Instance.getCloneData(this.player, sel, this.activeTab);
      } else {
         Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(sel), Minecraft.getMinecraft().world);
         if (entity == null) {
            return null;
         } else {
            NBTTagCompound compound = new NBTTagCompound();
            entity.writeToNBTOptional(compound);
            return compound;
         }
      }
   }

   protected void actionPerformed(GuiButton guibutton) {
      int id = guibutton.id;
      if (id == 0) {
         this.close();
      }

      if (id == 1) {
         NBTTagCompound compound = this.getCompound();
         if (compound != null) {
            compound.setTag("Pos", this.newDoubleNBTList((double)this.posX + 0.5D, (double)(this.posY + 1), (double)this.posZ + 0.5D));
            Client.sendData(EnumPacketServer.SpawnRider, compound);
            this.close();
         }
      }

      if (id == 2) {
         Client.sendData(EnumPacketServer.PlayerRider);
         this.close();
      }

      if (id == 3) {
         showingClones = 0;
         this.initGui();
      }

      if (id == 4) {
         showingClones = 1;
         this.initGui();
      }

      if (id == 5) {
         showingClones = 2;
         this.initGui();
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
