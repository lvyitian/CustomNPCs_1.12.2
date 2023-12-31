package noppes.npcs.blocks;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.IPermission;

public class BlockScripted extends BlockInterface implements IPermission {
   public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0010000000474974513D, 0.0010000000474974513D, 0.0010000000474974513D, 0.9980000257492065D, 0.9980000257492065D, 0.9980000257492065D);
   public static final AxisAlignedBB AABB_EMPTY = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

   public BlockScripted() {
      super(Material.ROCK);
      this.setSoundType(SoundType.STONE);
   }

   public TileEntity createNewTileEntity(World worldIn, int meta) {
      return new TileScripted();
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      return AABB;
   }

   public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos) {
      TileScripted tile = (TileScripted)world.getTileEntity(pos);
      return tile != null && tile.isPassible ? AABB_EMPTY : AABB;
   }

   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      if (world.isRemote) {
         return true;
      } else {
         ItemStack currentItem = player.inventory.getCurrentItem();
         if (currentItem == null || currentItem.getItem() != CustomItems.wand && currentItem.getItem() != CustomItems.scripter) {
            TileScripted tile = (TileScripted)world.getTileEntity(pos);
            return !EventHooks.onScriptBlockInteract(tile, player, side.getIndex(), hitX, hitY, hitZ);
         } else {
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.ScriptBlock, (EntityNPCInterface)null, pos.getX(), pos.getY(), pos.getZ());
            return true;
         }
      }
   }

   public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
      if (entity instanceof EntityPlayer && !world.isRemote) {
         NoppesUtilServer.sendOpenGui((EntityPlayer)entity, EnumGuiType.ScriptBlock, (EntityNPCInterface)null, pos.getX(), pos.getY(), pos.getZ());
      }

   }

   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entityIn) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         EventHooks.onScriptBlockCollide(tile, entityIn);
      }
   }

   public void fillWithRain(World world, BlockPos pos) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         EventHooks.onScriptBlockRainFill(tile);
      }
   }

   public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         fallDistance = EventHooks.onScriptBlockFallenUpon(tile, entity, fallDistance);
         super.onFallenUpon(world, pos, entity, fallDistance);
      }
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         EventHooks.onScriptBlockClicked(tile, player);
      }
   }

   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         EventHooks.onScriptBlockBreak(tile);
      }

      super.breakBlock(world, pos, state);
   }

   public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         if (EventHooks.onScriptBlockHarvest(tile, player)) {
            return false;
         }
      }

      return super.removedByPlayer(state, world, pos, player, willHarvest);
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return null;
   }

   public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         if (EventHooks.onScriptBlockExploded(tile)) {
            return;
         }
      }

      super.onBlockExploded(world, pos, explosion);
   }

   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos pos2) {
      if (!world.isRemote) {
         TileScripted tile = (TileScripted)world.getTileEntity(pos);
         EventHooks.onScriptBlockNeighborChanged(tile);
         int power = 0;

         for(EnumFacing enumfacing : EnumFacing.values()) {
            int p = world.getRedstonePower(pos.offset(enumfacing), enumfacing);
            if (p > power) {
               power = p;
            }
         }

         if (tile.prevPower != power && tile.powering <= 0) {
            tile.newPower = power;
         }

      }
   }

   public boolean canProvidePower(IBlockState state) {
      return true;
   }

   public int getWeakPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
      return this.getStrongPower(state, worldIn, pos, side);
   }

   public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return ((TileScripted)world.getTileEntity(pos)).activePowering;
   }

   public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
      return ((TileScripted)world.getTileEntity(pos)).isLadder;
   }

   public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
      return true;
   }

   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      TileScripted tile = (TileScripted)world.getTileEntity(pos);
      return tile == null ? 0 : tile.lightValue;
   }

   public boolean isPassable(IBlockAccess world, BlockPos pos) {
      return ((TileScripted)world.getTileEntity(pos)).isPassible;
   }

   public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
      return super.canEntityDestroy(state, world, pos, entity);
   }

   public float getEnchantPowerBonus(World world, BlockPos pos) {
      return super.getEnchantPowerBonus(world, pos);
   }

   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      return ((TileScripted)world.getTileEntity(pos)).blockHardness;
   }

   public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
      return ((TileScripted)world.getTileEntity(pos)).blockResistance;
   }

   public boolean isAllowed(EnumPacketServer e) {
      return e == EnumPacketServer.SaveTileEntity || e == EnumPacketServer.ScriptBlockDataSave;
   }
}
