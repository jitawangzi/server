package cn.game.games.cache.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BaseOp implements ICacheOp{

	protected transient Logger log = LoggerFactory.getLogger(this.getClass());
	
}
