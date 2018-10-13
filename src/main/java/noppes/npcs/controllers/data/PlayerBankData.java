package noppes.npcs.controllers.data;

import java.util.HashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.BankController;

public class PlayerBankData {
   public HashMap<Integer, BankData> banks = new HashMap();

   public void loadNBTData(NBTTagCompound compound) {
      HashMap<Integer, BankData> banks = new HashMap();
      NBTTagList list = compound.getTagList("BankData", 10);
      if (list != null) {
         for(int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
            BankData data = new BankData();
            data.readNBT(nbttagcompound);
            banks.put(Integer.valueOf(data.bankId), data);
         }

         this.banks = banks;
      }
   }

   public void saveNBTData(NBTTagCompound playerData) {
      NBTTagList list = new NBTTagList();

      for(BankData data : this.banks.values()) {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         data.writeNBT(nbttagcompound);
         list.appendTag(nbttagcompound);
      }

      playerData.setTag("BankData", list);
   }

   public BankData getBank(int bankId) {
      return (BankData)this.banks.get(Integer.valueOf(bankId));
   }

   public BankData getBankOrDefault(int bankId) {
      BankData data = (BankData)this.banks.get(Integer.valueOf(bankId));
      if (data != null) {
         return data;
      } else {
         Bank bank = BankController.getInstance().getBank(bankId);
         return (BankData)this.banks.get(Integer.valueOf(bank.id));
      }
   }

   public boolean hasBank(int bank) {
      return this.banks.containsKey(Integer.valueOf(bank));
   }

   public void loadNew(int bank) {
      BankData data = new BankData();
      data.bankId = bank;
      this.banks.put(Integer.valueOf(bank), data);
   }
}
