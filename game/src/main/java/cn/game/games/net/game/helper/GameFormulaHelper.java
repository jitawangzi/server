package cn.game.games.net.game.helper;

public class GameFormulaHelper {

	/** 
	 * 调用指定id的公式,一般只在buff中使用,不确定公式id的情况下，
	 * 不要直接调用，不好确定传入的参数。  
	 * @param playerId
	 * @param formula  公式id
	 * @param param	   公式需要的参数
	 * @return
	 */
	public static int calcFormula(long playerId, int formula, int... param) {
		
		int ret = 0;
		switch (formula) {
			case 101: {
//				ExploreOp exploreOp = player.getModule(ExploreOp.class);
//				ret = FormulaHelper.getChangeHpByPollution(formula, exploreOp.getPollution(), GlobalConst.explorePollutionLimit);
				break;
			} 
			case 102: {
//				ExploreOp exploreOp = player.getModule(ExploreOp.class);
//				ret = FormulaHelper.getChangeSanByPollution(formula, exploreOp.getPollution(), GlobalConst.explorePollutionLimit);
				break;
			}
			default:
				break;
		}
		return ret;
		
	}
}
