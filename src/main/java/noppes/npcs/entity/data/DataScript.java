package noppes.npcs.entity.data;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;

public class DataScript implements IScriptHandler {
   private List<ScriptContainer> scripts = new ArrayList();
   private String scriptLanguage = "ECMAScript";
   private EntityNPCInterface npc;
   private boolean enabled = false;
   public long lastInited = -1L;

   public DataScript(EntityNPCInterface npc) {
      this.npc = npc;
   }

   public void readFromNBT(NBTTagCompound compound) {
      this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
      this.scriptLanguage = compound.getString("ScriptLanguage");
      this.enabled = compound.getBoolean("ScriptEnabled");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound compound) {
      compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
      compound.setString("ScriptLanguage", this.scriptLanguage);
      compound.setBoolean("ScriptEnabled", this.enabled);
      return compound;
   }

   public void runScript(EnumScriptType type, Event event) {
      if (this.isEnabled()) {
         if (ScriptController.Instance.lastLoaded > this.lastInited) {
            this.lastInited = ScriptController.Instance.lastLoaded;
            if (type != EnumScriptType.INIT) {
               EventHooks.onNPCInit(this.npc);
            }
         }

         for(ScriptContainer script : this.scripts) {
            script.run(type, event);
         }

      }
   }

   public boolean isEnabled() {
      return this.enabled && ScriptController.HasStart && !this.npc.world.isRemote;
   }

   public boolean isClient() {
      return this.npc.isRemote();
   }

   public boolean getEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean bo) {
      this.enabled = bo;
   }

   public String getLanguage() {
      return this.scriptLanguage;
   }

   public void setLanguage(String lang) {
      this.scriptLanguage = lang;
   }

   public List<ScriptContainer> getScripts() {
      return this.scripts;
   }

   public String noticeString() {
      BlockPos pos = this.npc.getPosition();
      return MoreObjects.toStringHelper(this.npc).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ()).toString();
   }

   public Map<Long, String> getConsoleText() {
      Map<Long, String> map = new TreeMap();
      int tab = 0;

      for(ScriptContainer script : this.getScripts()) {
         ++tab;

         for(Entry<Long, String> entry : script.console.entrySet()) {
            map.put(entry.getKey(), " tab " + tab + ":\n" + (String)entry.getValue());
         }
      }

      return map;
   }

   public void clearConsole() {
      for(ScriptContainer script : this.getScripts()) {
         script.console.clear();
      }

   }
}
