package noppes.npcs.constants;

public enum EnumParts {
   EARS("ears"),
   HORNS("horns"),
   HAIR("hair"),
   MOHAWK("mohawk"),
   SNOUT("snout"),
   BEARD("beard"),
   TAIL("tail"),
   CLAWS("claws"),
   LEGS("legs"),
   FIN("fin"),
   SKIRT("skirt"),
   WINGS("wings"),
   HEAD("head"),
   BODY("body"),
   BREASTS("breasts"),
   PARTICLES("particles"),
   ARM_LEFT("armleft"),
   ARM_RIGHT("armright"),
   LEG_LEFT("legleft"),
   LEG_RIGHT("legright"),
   EYES("eyes");

   public String name;
   public int patterns = 1;

   private EnumParts(String name) {
      this.name = name;
   }

   public static EnumParts FromName(String name) {
      for(EnumParts e : values()) {
         if (e.name.equals(name)) {
            return e;
         }
      }

      return null;
   }
}
