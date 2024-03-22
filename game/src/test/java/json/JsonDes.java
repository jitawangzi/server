package json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.game.games.net.game.module.quest.require.PlayerLevelCondition;

public class JsonDes {

	public static void main(String[] args) throws JsonProcessingException {
		PlayerLevelCondition e = new PlayerLevelCondition();
		e.setFinishCount(333);
//		String saveString = e.toSaveString();
		// {"achieve":false,"finishCount":333,"index":0}
//		System.out.println(saveString);
//		PlayerLevelCondition parseObject = (PlayerLevelCondition) JSON.parseObject(saveString, AbstractQuestCondition.class);
//		System.out.println(parseObject.getFinishCount());
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(e);
		System.out.println(jsonStr);

	}
}
