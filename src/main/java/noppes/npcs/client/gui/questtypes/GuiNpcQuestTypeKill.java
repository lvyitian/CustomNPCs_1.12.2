package noppes.npcs.client.gui.questtypes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestKill;

public class GuiNpcQuestTypeKill extends SubGuiInterface implements ITextfieldListener, ICustomScrollListener {
   private GuiScreen parent;
   private GuiCustomScroll scroll;
   private QuestKill quest;
   private GuiNpcTextField lastSelected;

   public GuiNpcQuestTypeKill(EntityNPCInterface npc, Quest q, GuiScreen parent) {
      this.npc = npc;
      this.parent = parent;
      this.title = "Quest Kill Setup";
      this.quest = (QuestKill)q.questInterface;
      this.setBackground("menubg.png");
      this.xSize = 356;
      this.ySize = 216;
      this.closeOnEsc = true;
   }

   public void initGui() {
      super.initGui();
      int i = 0;
      this.addLabel(new GuiNpcLabel(0, "You can fill in npc or player names too", this.guiLeft + 4, this.guiTop + 50));

      for(String name : this.quest.targets.keySet()) {
         this.addTextField(new GuiNpcTextField(i, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 70 + i * 22, 180, 20, name));
         this.addTextField(new GuiNpcTextField(i + 3, this, this.fontRenderer, this.guiLeft + 186, this.guiTop + 70 + i * 22, 24, 20, this.quest.targets.get(name) + ""));
         this.getTextField(i + 3).numbersOnly = true;
         this.getTextField(i + 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
         ++i;
      }

      while(i < 3) {
         this.addTextField(new GuiNpcTextField(i, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 70 + i * 22, 180, 20, ""));
         this.addTextField(new GuiNpcTextField(i + 3, this, this.fontRenderer, this.guiLeft + 186, this.guiTop + 70 + i * 22, 24, 20, "1"));
         this.getTextField(i + 3).numbersOnly = true;
         this.getTextField(i + 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
         ++i;
      }

      ArrayList<String> list = new ArrayList();

      for(EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
         Class<? extends Entity> c = ent.getEntityClass();
         String name = ent.getName();

         try {
            if (EntityLivingBase.class.isAssignableFrom(c) && !EntityNPCInterface.class.isAssignableFrom(c) && c.getConstructor(World.class) != null && !Modifier.isAbstract(c.getModifiers())) {
               list.add(name.toString());
            }
         } catch (SecurityException var8) {
            var8.printStackTrace();
         } catch (NoSuchMethodException var9) {
            ;
         }
      }

      if (this.scroll == null) {
         this.scroll = new GuiCustomScroll(this, 0);
      }

      this.scroll.setList(list);
      this.scroll.setSize(130, 198);
      this.scroll.guiLeft = this.guiLeft + 220;
      this.scroll.guiTop = this.guiTop + 14;
      this.addScroll(this.scroll);
      this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 140, 98, 20, "gui.back"));
      this.scroll.visible = this.lastSelected != null;
   }

   protected void actionPerformed(GuiButton guibutton) {
      super.actionPerformed(guibutton);
      if (guibutton.id == 0) {
         this.close();
      }

   }

   public void mouseClicked(int i, int j, int k) {
      super.mouseClicked(i, j, k);
      this.scroll.visible = this.lastSelected != null;
   }

   public void save() {
   }

   public void unFocused(GuiNpcTextField guiNpcTextField) {
      if (guiNpcTextField.getId() < 3) {
         this.lastSelected = guiNpcTextField;
      }

      this.saveTargets();
   }

   private void saveTargets() {
      HashMap<String, Integer> map = new HashMap();

      for(int i = 0; i < 3; ++i) {
         String name = this.getTextField(i).getText();
         if (!name.isEmpty()) {
            map.put(name, Integer.valueOf(this.getTextField(i + 3).getInteger()));
         }
      }

      this.quest.targets = map;
   }

   public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
      if (this.lastSelected != null) {
         this.lastSelected.setText(guiCustomScroll.getSelected());
         this.saveTargets();
      }
   }
}
