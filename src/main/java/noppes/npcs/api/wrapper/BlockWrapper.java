package noppes.npcs.api.wrapper;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidBase;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.BlockScriptedDoor;
import noppes.npcs.blocks.tiles.TileNpcEntity;
import noppes.npcs.util.LRUHashMap;

public class BlockWrapper implements IBlock {
   private static final Map<String, BlockWrapper> blockCache = new LRUHashMap<String, BlockWrapper>(400);
   protected final IWorld world;
   protected final Block block;
   protected final BlockPos pos;
   protected final BlockPosWrapper bPos;
   protected TileEntity tile;
   protected TileNpcEntity storage;
   private final IData tempdata = new BlockWrapper$1(this);
   private final IData storeddata = new BlockWrapper$2(this);

   protected BlockWrapper(World world, Block block, BlockPos pos) {
      this.world = NpcAPI.Instance().getIWorld((WorldServer)world);
      this.block = block;
      this.pos = pos;
      this.bPos = new BlockPosWrapper(pos);
      this.setTile(world.getTileEntity(pos));
   }

   public int getX() {
      return this.pos.getX();
   }

   public int getY() {
      return this.pos.getY();
   }

   public int getZ() {
      return this.pos.getZ();
   }

   public IPos getPos() {
      return this.bPos;
   }

   public int getMetadata() {
      return this.block.getMetaFromState(this.world.getMCWorld().getBlockState(this.pos));
   }

   public void setMetadata(int i) {
      this.world.getMCWorld().setBlockState(this.pos, this.block.getStateFromMeta(i), 3);
   }

   public void remove() {
      this.world.getMCWorld().setBlockToAir(this.pos);
   }

   public boolean isRemoved() {
      IBlockState state = this.world.getMCWorld().getBlockState(this.pos);
      if (state == null) {
         return true;
      } else {
         return state.getBlock() != this.block;
      }
   }

   public boolean isAir() {
      return this.block.isAir(this.world.getMCWorld().getBlockState(this.pos), this.world.getMCWorld(), this.pos);
   }

   public BlockWrapper setBlock(String name) {
      Block block = (Block)Block.REGISTRY.getObject(new ResourceLocation(name));
      if (block == null) {
         return this;
      } else {
         this.world.getMCWorld().setBlockState(this.pos, block.getDefaultState());
         return new BlockWrapper(this.world.getMCWorld(), block, this.pos);
      }
   }

   public BlockWrapper setBlock(IBlock block) {
      this.world.getMCWorld().setBlockState(this.pos, block.getMCBlock().getDefaultState());
      return new BlockWrapper(this.world.getMCWorld(), block.getMCBlock(), this.pos);
   }

   public boolean isContainer() {
      if (this.tile != null && this.tile instanceof IInventory) {
         return ((IInventory)this.tile).getSizeInventory() > 0;
      } else {
         return false;
      }
   }

   public IContainer getContainer() {
      if (!this.isContainer()) {
         throw new CustomNPCsException("This block is not a container", new Object[0]);
      } else {
         return NpcAPI.Instance().getIContainer((IInventory)this.tile);
      }
   }

   public IData getTempdata() {
      return this.tempdata;
   }

   public IData getStoreddata() {
      return this.storeddata;
   }

   public String getName() {
      return Block.REGISTRY.getNameForObject(this.block) + "";
   }

   public String getDisplayName() {
      return this.tile == null ? this.getName() : this.tile.getDisplayName().getUnformattedText();
   }

   public IWorld getWorld() {
      return this.world;
   }

   public Block getMCBlock() {
      return this.block;
   }

   /** @deprecated */
   @Deprecated
   public static IBlock createNew(World world, BlockPos pos, IBlockState state) {
      Block block = state.getBlock();
      String key = state.toString() + pos.toString();
      BlockWrapper b = (BlockWrapper)blockCache.get(key);
      if (b != null) {
         b.setTile(world.getTileEntity(pos));
         return b;
      } else {
         if (block instanceof BlockScripted) {
            b = new BlockScriptedWrapper(world, block, pos);
         } else if (block instanceof BlockScriptedDoor) {
            b = new BlockScriptedDoorWrapper(world, block, pos);
         } else if (block instanceof BlockFluidBase) {
            b = new BlockFluidContainerWrapper(world, block, pos);
         } else {
            b = new BlockWrapper(world, block, pos);
         }

         blockCache.put(key, b);
         return b;
      }
   }

   public static void clearCache() {
      blockCache.clear();
   }

   public boolean hasTileEntity() {
      return this.tile != null;
   }

   private void setTile(TileEntity tile) {
      this.tile = tile;
      if (tile instanceof TileNpcEntity) {
         this.storage = (TileNpcEntity)tile;
      }

   }

   public INbt getTileEntityNBT() {
      NBTTagCompound compound = new NBTTagCompound();
      this.tile.writeToNBT(compound);
      return NpcAPI.Instance().getINbt(compound);
   }

   public void setTileEntityNBT(INbt nbt) {
      this.tile.readFromNBT(nbt.getMCNBT());
   }

   public TileEntity getMCTileEntity() {
      return this.tile;
   }

   public void blockEvent(int type, int data) {
      this.world.getMCWorld().addBlockEvent(this.pos, this.getMCBlock(), type, data);
   }

}
