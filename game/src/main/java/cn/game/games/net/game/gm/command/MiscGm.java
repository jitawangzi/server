package cn.game.games.net.game.gm.command;

import org.springframework.stereotype.Component;

import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmGroup;

/**
 * 杂项GM指令分组（已清理硬编码本地文件读写与阻塞休眠的废弃测试命令）
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "misc", description = "杂项调试指令")
public class MiscGm extends AbstractGm {
}