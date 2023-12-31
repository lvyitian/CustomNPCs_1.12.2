package noppes.npcs.api.wrapper;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlockScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.tiles.TileScripted;

public class BlockScriptedWrapper extends BlockWrapper implements IBlockScripted {
   private TileScripted tile;

   public BlockScriptedWrapper(World world, Block block, BlockPos pos) {
      super(world, block, pos);
      this.tile = (TileScripted)super.tile;
   }

   public void setModel(IItemStack item) {
      if (item == null) {
         this.tile.setItemModel((ItemStack)null, (Block)null);
      } else {
         this.tile.setItemModel(item.getMCItemStack(), Block.getBlockFromItem(item.getMCItemStack().getItem()));
      }

   }

   public void setModel(String name) {
      if (name == null) {
         this.tile.setItemModel((ItemStack)null, (Block)null);
      } else {
         ResourceLocation loc = new ResourceLocation(name);
         Block block = (Block)Block.REGISTRY.getObject(loc);
         this.tile.setItemModel(new ItemStack((Item)Item.REGISTRY.getObject(loc)), block);
      }

   }

   public IItemStack getModel() {
      return NpcAPI.Instance().getIItemStack(this.tile.itemModel);
   }

   public void setRedstonePower(int strength) {
      this.tile.setRedstonePower(strength);
   }

   public int getRedstonePower() {
      return this.tile.powering;
   }

   public void setIsLadder(boolean bo) {
      this.tile.isLadder = bo;
      this.tile.needsClientUpdate = true;
   }

   public boolean getIsLadder() {
      return this.tile.isLadder;
   }

   public void setIsPassible(boolean bo) {
      this.tile.isPassible = bo;
      this.tile.needsClientUpdate = true;
   }

   public boolean getIsPassible() {
      return this.tile.isPassible;
   }

   public void setLight(int value) {
      this.tile.setLightValue(value);
   }

   public int getLight() {
      return this.tile.lightValue;
   }

   public void setScale(float x, float y, float z) {
      this.tile.setScale(x, y, z);
   }

   public float getScaleX() {
      return this.tile.scaleX;
   }

   public float getScaleY() {
      return this.tile.scaleY;
   }

   public float getScaleZ() {
      return this.tile.scaleZ;
   }

   public void setRotation(int x, int y, int z) {
      this.tile.setRotation(x % 360, y % 360, z % 360);
   }

   public int getRotationX() {
      return this.tile.rotationX;
   }

   public int getRotationY() {
      return this.tile.rotationY;
   }

   public int getRotationZ() {
      return this.tile.rotationZ;
   }

   public float getHardness() {
      return this.tile.blockHardness;
   }

   public void setHardness(float hardness) {
      this.tile.blockHardness = hardness;
   }

   public float getResistance() {
      return this.tile.blockResistance;
   }

   public void setResistance(float resistance) {
      this.tile.blockResistance = resistance;
   }

   public String executeCommand(String command) {
      if (!this.tile.getWorld().getMinecraftServer().isCommandBlockEnabled()) {
         throw new CustomNPCsException("Command blocks need to be enabled to executeCommands", new Object[0]);
      } else {
         return NoppesUtilServer.runCommand(this.tile.getWorld(), this.tile.getPos(), "ScriptBlock: " + this.tile.getPos(), command, (EntityPlayer)null, (ICommandSender)null);
      }
   }

   public ITimers getTimers() {
      return this.tile.timers;
   }
}
