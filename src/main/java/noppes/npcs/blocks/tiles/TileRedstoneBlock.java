package noppes.npcs.blocks.tiles;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.BlockNpcRedstone;
import noppes.npcs.controllers.data.Availability;

public class TileRedstoneBlock extends TileNpcEntity implements ITickable {
   public int onRange = 12;
   public int offRange = 20;
   public int onRangeX = 12;
   public int onRangeY = 12;
   public int onRangeZ = 12;
   public int offRangeX = 20;
   public int offRangeY = 20;
   public int offRangeZ = 20;
   public boolean isDetailed = false;
   public Availability availability = new Availability();
   public boolean isActivated = false;
   private int ticks = 10;

   public void update() {
      if (!this.world.isRemote) {
         --this.ticks;
         if (this.ticks <= 0) {
            this.ticks = this.onRange > 10 ? 20 : 10;
            Block block = this.getBlockType();
            if (block != null && block instanceof BlockNpcRedstone) {
               if (CustomNpcs.FreezeNPCs) {
                  if (this.isActivated) {
                     this.setActive(block, false);
                  }

               } else {
                  if (!this.isActivated) {
                     int x = this.isDetailed ? this.onRangeX : this.onRange;
                     int y = this.isDetailed ? this.onRangeY : this.onRange;
                     int z = this.isDetailed ? this.onRangeZ : this.onRange;
                     List<EntityPlayer> list = this.getPlayerList(x, y, z);
                     if (list.isEmpty()) {
                        return;
                     }

                     for(EntityPlayer player : list) {
                        if (this.availability.isAvailable(player)) {
                           this.setActive(block, true);
                           return;
                        }
                     }
                  } else {
                     int x = this.isDetailed ? this.offRangeX : this.offRange;
                     int y = this.isDetailed ? this.offRangeY : this.offRange;
                     int z = this.isDetailed ? this.offRangeZ : this.offRange;

                     for(EntityPlayer player : this.getPlayerList(x, y, z)) {
                        if (this.availability.isAvailable(player)) {
                           return;
                        }
                     }

                     this.setActive(block, false);
                  }

               }
            }
         }
      }
   }

   public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
      return oldState.getBlock() != newState.getBlock();
   }

   private void setActive(Block block, boolean bo) {
      this.isActivated = bo;
      IBlockState state = block.getDefaultState().withProperty(BlockNpcRedstone.ACTIVE, Boolean.valueOf(this.isActivated));
      this.world.setBlockState(this.pos, state, 2);
      this.markDirty();
      this.world.notifyBlockUpdate(this.pos, state, state, 3);
      block.onBlockAdded(this.world, this.pos, state);
   }

   private List<EntityPlayer> getPlayerList(int x, int y, int z) {
      return this.world.getEntitiesWithinAABB(EntityPlayer.class, (new AxisAlignedBB((double)this.pos.getX(), (double)this.pos.getY(), (double)this.pos.getZ(), (double)(this.pos.getX() + 1), (double)(this.pos.getY() + 1), (double)(this.pos.getZ() + 1))).grow((double)x, (double)y, (double)z));
   }

   public void readFromNBT(NBTTagCompound compound) {
      super.readFromNBT(compound);
      this.onRange = compound.getInteger("BlockOnRange");
      this.offRange = compound.getInteger("BlockOffRange");
      this.isDetailed = compound.getBoolean("BlockIsDetailed");
      if (compound.hasKey("BlockOnRangeX")) {
         this.isDetailed = true;
         this.onRangeX = compound.getInteger("BlockOnRangeX");
         this.onRangeY = compound.getInteger("BlockOnRangeY");
         this.onRangeZ = compound.getInteger("BlockOnRangeZ");
         this.offRangeX = compound.getInteger("BlockOffRangeX");
         this.offRangeY = compound.getInteger("BlockOffRangeY");
         this.offRangeZ = compound.getInteger("BlockOffRangeZ");
      }

      if (compound.hasKey("BlockActivated")) {
         this.isActivated = compound.getBoolean("BlockActivated");
      }

      this.availability.readFromNBT(compound);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound compound) {
      compound.setInteger("BlockOnRange", this.onRange);
      compound.setInteger("BlockOffRange", this.offRange);
      compound.setBoolean("BlockActivated", this.isActivated);
      compound.setBoolean("BlockIsDetailed", this.isDetailed);
      if (this.isDetailed) {
         compound.setInteger("BlockOnRangeX", this.onRangeX);
         compound.setInteger("BlockOnRangeY", this.onRangeY);
         compound.setInteger("BlockOnRangeZ", this.onRangeZ);
         compound.setInteger("BlockOffRangeX", this.offRangeX);
         compound.setInteger("BlockOffRangeY", this.offRangeY);
         compound.setInteger("BlockOffRangeZ", this.offRangeZ);
      }

      this.availability.writeToNBT(compound);
      return super.writeToNBT(compound);
   }
}
