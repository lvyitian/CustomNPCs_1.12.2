package noppes.npcs.api.handler;

import java.util.List;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;

public interface IQuestHandler {
   List<IQuestCategory> categories();

   IQuest get(int var1);
}
