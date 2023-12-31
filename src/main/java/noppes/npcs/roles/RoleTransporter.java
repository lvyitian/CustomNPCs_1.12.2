package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.entity.data.role.IRoleTransporter;
import noppes.npcs.api.entity.data.role.IRoleTransporter$ITransportLocation;
import noppes.npcs.api.event.RoleEvent$TransporterUnlockedEvent;
import noppes.npcs.api.event.RoleEvent$TransporterUseEvent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerTransportData;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleTransporter extends RoleInterface implements IRoleTransporter {
   public int transportId = -1;
   public String name;
   private int ticks = 10;

   public RoleTransporter(EntityNPCInterface npc) {
      super(npc);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("TransporterId", this.transportId);
      return nbttagcompound;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      this.transportId = nbttagcompound.getInteger("TransporterId");
      TransportLocation loc = this.getLocation();
      if (loc != null) {
         this.name = loc.name;
      }

   }

   public boolean aiShouldExecute() {
      --this.ticks;
      if (this.ticks > 0) {
         return false;
      } else {
         this.ticks = 10;
         if (!this.hasTransport()) {
            return false;
         } else {
            TransportLocation loc = this.getLocation();
            if (loc.type != 0) {
               return false;
            } else {
               for(EntityPlayer player : this.npc.world.getEntitiesWithinAABB(EntityPlayer.class, this.npc.getEntityBoundingBox().grow(6.0D, 6.0D, 6.0D))) {
                  if (this.npc.canSee(player)) {
                     this.unlock(player, loc);
                  }
               }

               return false;
            }
         }
      }
   }

   public void interact(EntityPlayer player) {
      if (this.hasTransport()) {
         TransportLocation loc = this.getLocation();
         if (loc.type == 2) {
            this.unlock(player, loc);
         }

         NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTransporter, this.npc);
      }

   }

   public void transport(EntityPlayerMP player, String location) {
      TransportLocation loc = TransportController.getInstance().getTransport(location);
      PlayerTransportData playerdata = PlayerData.get(player).transportData;
      if (loc != null && (loc.isDefault() || playerdata.transports.contains(Integer.valueOf(loc.id)))) {
         RoleEvent$TransporterUseEvent event = new RoleEvent$TransporterUseEvent(player, this.npc.wrappedNPC, loc);
         if (!EventHooks.onNPCRole(this.npc, event)) {
            NoppesUtilPlayer.teleportPlayer(player, (double)loc.pos.getX(), (double)loc.pos.getY(), (double)loc.pos.getZ(), loc.dimension);
         }
      }
   }

   private void unlock(EntityPlayer player, TransportLocation loc) {
      PlayerTransportData data = PlayerData.get(player).transportData;
      if (!data.transports.contains(Integer.valueOf(this.transportId))) {
         RoleEvent$TransporterUnlockedEvent event = new RoleEvent$TransporterUnlockedEvent(player, this.npc.wrappedNPC);
         if (!EventHooks.onNPCRole(this.npc, event)) {
            data.transports.add(Integer.valueOf(this.transportId));
            player.sendMessage(new TextComponentTranslation("transporter.unlock", new Object[]{loc.name}));
         }
      }
   }

   public TransportLocation getLocation() {
      return this.npc.isRemote() ? null : TransportController.getInstance().getTransport(this.transportId);
   }

   public boolean hasTransport() {
      TransportLocation loc = this.getLocation();
      return loc != null && loc.id == this.transportId;
   }

   public void setTransport(TransportLocation location) {
      this.transportId = location.id;
      this.name = location.name;
   }

}
