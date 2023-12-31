package noppes.npcs.controllers.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IRecipe;
import noppes.npcs.controllers.RecipeController;

public class RecipeCarpentry extends ShapedRecipes implements IRecipe {
   public int id = -1;
   public String name = "";
   public Availability availability = new Availability();
   public boolean isGlobal = false;
   public boolean ignoreDamage = false;
   public boolean ignoreNBT = false;
   public boolean savesRecipe = true;

   public RecipeCarpentry(int width, int height, NonNullList<Ingredient> recipe, ItemStack result) {
      super("customnpcs", width, height, recipe, result);
   }

   public RecipeCarpentry(String name) {
      super("customnpcs", 0, 0, NonNullList.create(), ItemStack.EMPTY);
      this.name = name;
   }

   public static RecipeCarpentry read(NBTTagCompound compound) {
      RecipeCarpentry recipe = new RecipeCarpentry(compound.getInteger("Width"), compound.getInteger("Height"), NBTTags.getIngredientList(compound.getTagList("Materials", 10)), new ItemStack(compound.getCompoundTag("Item")));
      recipe.name = compound.getString("Name");
      recipe.id = compound.getInteger("ID");
      recipe.availability.readFromNBT(compound.getCompoundTag("Availability"));
      recipe.ignoreDamage = compound.getBoolean("IgnoreDamage");
      recipe.ignoreNBT = compound.getBoolean("IgnoreNBT");
      recipe.isGlobal = compound.getBoolean("Global");
      return recipe;
   }

   public NBTTagCompound writeNBT() {
      NBTTagCompound compound = new NBTTagCompound();
      compound.setInteger("ID", this.id);
      compound.setInteger("Width", this.recipeWidth);
      compound.setInteger("Height", this.recipeHeight);
      if (this.getRecipeOutput() != null) {
         compound.setTag("Item", this.getRecipeOutput().writeToNBT(new NBTTagCompound()));
      }

      compound.setTag("Materials", NBTTags.nbtIngredientList(this.recipeItems));
      compound.setTag("Availability", this.availability.writeToNBT(new NBTTagCompound()));
      compound.setString("Name", this.name);
      compound.setBoolean("Global", this.isGlobal);
      compound.setBoolean("IgnoreDamage", this.ignoreDamage);
      compound.setBoolean("IgnoreNBT", this.ignoreNBT);
      return compound;
   }

   public boolean matches(InventoryCrafting par1InventoryCrafting, World world) {
      for(int i = 0; i <= 4 - this.recipeWidth; ++i) {
         for(int j = 0; j <= 4 - this.recipeHeight; ++j) {
            if (this.checkMatch(par1InventoryCrafting, i, j, true)) {
               return true;
            }

            if (this.checkMatch(par1InventoryCrafting, i, j, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkMatch(InventoryCrafting par1InventoryCrafting, int par2, int par3, boolean par4) {
      for(int i = 0; i < 4; ++i) {
         for(int j = 0; j < 4; ++j) {
            int var7 = i - par2;
            int var8 = j - par3;
            Ingredient ingredient = Ingredient.EMPTY;
            if (var7 >= 0 && var8 >= 0 && var7 < this.recipeWidth && var8 < this.recipeHeight) {
               if (par4) {
                  ingredient = (Ingredient)this.recipeItems.get(this.recipeWidth - var7 - 1 + var8 * this.recipeWidth);
               } else {
                  ingredient = (Ingredient)this.recipeItems.get(var7 + var8 * this.recipeWidth);
               }
            }

            ItemStack var10 = par1InventoryCrafting.getStackInRowAndColumn(i, j);
            if (!var10.isEmpty() || ingredient.getMatchingStacks().length == 0) {
               return false;
            }

            ItemStack var9 = ingredient.getMatchingStacks()[0];
            if ((!var10.isEmpty() || !var9.isEmpty()) && !NoppesUtilPlayer.compareItems(var9, var10, this.ignoreDamage, this.ignoreNBT)) {
               return false;
            }
         }
      }

      return true;
   }

   public ItemStack getCraftingResult(InventoryCrafting var1) {
      return this.getRecipeOutput().isEmpty() ? ItemStack.EMPTY : this.getRecipeOutput().copy();
   }

   public static RecipeCarpentry createRecipe(RecipeCarpentry recipe, ItemStack par1ItemStack, Object... par2ArrayOfObj) {
      String var3 = "";
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      if (par2ArrayOfObj[var4] instanceof String[]) {
         String[] var7 = (String[])par2ArrayOfObj[var4++];

         for(String var11 : var7) {
            ++var6;
            var5 = var11.length();
            var3 = var3 + var11;
         }
      } else {
         while(par2ArrayOfObj[var4] instanceof String) {
            String var13 = (String)par2ArrayOfObj[var4++];
            ++var6;
            var5 = var13.length();
            var3 = var3 + var13;
         }
      }

      HashMap var14;
      for(var14 = new HashMap(); var4 < par2ArrayOfObj.length; var4 += 2) {
         Character var16 = (Character)par2ArrayOfObj[var4];
         ItemStack var17 = ItemStack.EMPTY;
         if (par2ArrayOfObj[var4 + 1] instanceof Item) {
            var17 = new ItemStack((Item)par2ArrayOfObj[var4 + 1]);
         } else if (par2ArrayOfObj[var4 + 1] instanceof Block) {
            var17 = new ItemStack((Block)par2ArrayOfObj[var4 + 1], 1, -1);
         } else if (par2ArrayOfObj[var4 + 1] instanceof ItemStack) {
            var17 = (ItemStack)par2ArrayOfObj[var4 + 1];
         }

         var14.put(var16, var17);
      }

      NonNullList<Ingredient> ingredients = NonNullList.create();

      for(int var9 = 0; var9 < var5 * var6; ++var9) {
         char var18 = var3.charAt(var9);
         if (var14.containsKey(Character.valueOf(var18))) {
            ingredients.add(var9, Ingredient.fromStacks(new ItemStack[]{((ItemStack)var14.get(Character.valueOf(var18))).copy()}));
         } else {
            ingredients.add(var9, Ingredient.EMPTY);
         }
      }

      RecipeCarpentry newrecipe = new RecipeCarpentry(var5, var6, ingredients, par1ItemStack);
      newrecipe.copy(recipe);
      if (var5 == 4 || var6 == 4) {
         newrecipe.isGlobal = false;
      }

      return newrecipe;
   }

   public NonNullList<ItemStack> getRemainingItems(InventoryCrafting p_179532_1_) {
      NonNullList<ItemStack> list = NonNullList.withSize(p_179532_1_.getSizeInventory(), ItemStack.EMPTY);

      for(int i = 0; i < list.size(); ++i) {
         ItemStack itemstack = p_179532_1_.getStackInSlot(i);
         list.set(i, ForgeHooks.getContainerItem(itemstack));
      }

      return list;
   }

   public void copy(RecipeCarpentry recipe) {
      this.id = recipe.id;
      this.name = recipe.name;
      this.availability = recipe.availability;
      this.isGlobal = recipe.isGlobal;
      this.ignoreDamage = recipe.ignoreDamage;
      this.ignoreNBT = recipe.ignoreNBT;
   }

   public ItemStack getCraftingItem(int i) {
      if (this.recipeItems != null && i < this.recipeItems.size()) {
         Ingredient ingredients = (Ingredient)this.recipeItems.get(i);
         return ingredients.getMatchingStacks().length == 0 ? ItemStack.EMPTY : ingredients.getMatchingStacks()[0];
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean isValid() {
      if (this.recipeItems.size() != 0 && !this.getRecipeOutput().isEmpty()) {
         for(Ingredient ingredient : this.recipeItems) {
            if (ingredient.getMatchingStacks().length > 0) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public String getName() {
      return this.name;
   }

   public ItemStack getResult() {
      return this.getRecipeOutput();
   }

   public boolean isGlobal() {
      return this.isGlobal;
   }

   public void setIsGlobal(boolean bo) {
      this.isGlobal = bo;
   }

   public boolean getIgnoreNBT() {
      return this.ignoreNBT;
   }

   public void setIgnoreNBT(boolean bo) {
      this.ignoreNBT = bo;
   }

   public boolean getIgnoreDamage() {
      return this.ignoreDamage;
   }

   public void setIgnoreDamage(boolean bo) {
      this.ignoreDamage = bo;
   }

   public void save() {
      try {
         RecipeController.instance.saveRecipe(this);
      } catch (IOException var2) {
         ;
      }

   }

   public void delete() {
      RecipeController.instance.delete(this.id);
   }

   public int getWidth() {
      return this.recipeWidth;
   }

   public int getHeight() {
      return this.recipeHeight;
   }

   public ItemStack[] getRecipe() {
      List<ItemStack> list = new ArrayList();

      for(Ingredient ingredient : this.recipeItems) {
         if (ingredient.getMatchingStacks().length > 0) {
            list.add(ingredient.getMatchingStacks()[0]);
         }
      }

      return (ItemStack[])list.toArray(new ItemStack[list.size()]);
   }

   public void saves(boolean bo) {
      this.savesRecipe = bo;
   }

   public boolean saves() {
      return this.savesRecipe;
   }

   public int getId() {
      return this.id;
   }
}
