package cn.game.core.map;

import java.util.ArrayList;
import java.util.List;

import com.ubird.astar.core.AStarNode;

/**    
 * 检查地图障碍是否会形成环
 * @date 2022年8月20日 下午3:03:29
 * @author SYQ
 */
public class RingChecker {

	/** 实际的障碍数据比正常数据大一圈，并且最外圈都设置为障碍，方便检测边缘的环 */
	private int[][] barrierData;
	private AStarNode[][] checked;
	private boolean foundRing = false;
	private List<Grid> emptyGridsInBarrier = new ArrayList<>();

	// 障碍环路径
	private int[][] barrierDataShow;
	private int barrierDataIndex;

	/**
	 * 构造方法，传入障碍数据
	 * @param barrierData
	 */
	public RingChecker(int[][] barrierData) {

//		this.checked = new AStarNode[barrierData.length][barrierData[0].length];
		this.barrierData = barrierData;
		// 四周边缘也设置为障碍
		for (int i = 0; i < barrierData[0].length; i++) {
			this.barrierData[0][i] = 1;
		}
		for (int i = 0; i < barrierData[0].length; i++) {
			this.barrierData[barrierData.length - 1][i] = 1;
		}
		for (int i = 0; i < barrierData.length; i++) {
			this.barrierData[i][0] = 1;
		}
		for (int i = 0; i < barrierData.length; i++) {
			this.barrierData[i][barrierData[0].length - 1] = 1;
		}

		barrierDataShow = new int[barrierData.length][barrierData[0].length];
		barrierDataIndex = 1;

	}

	/** 
	 * 检查这个待生成障碍的空地，会不会和其他障碍形成环
	 * @param grid
	 * @return
	 */
	public boolean checkRing(Grid grid) {
		// 构建一次数据，多次执行检查前，需要先清理上次检查时的一些辅助变量
		foundRing = false;
		emptyGridsInBarrier = new ArrayList<>();
		this.checked = new AStarNode[barrierData.length][barrierData[0].length];

//		AStarNode start = new AStarNode(grid.getX(), grid.getY());
		// 转换大一圈后的地图坐标
		AStarNode start = new AStarNode(grid.getX() + 1, grid.getY() + 1);
		// 待检查的位置先设置为障碍，方便后续的检测。 
		this.barrierData[start.getY()][start.getX()] = 1;
		checkRing(start, start, checked, 0);
		// 清除临时设置的障碍数据
		this.barrierData[start.getY()][start.getX()] = 0;
		return foundRing;
	}

	private void checkRing(AStarNode start, AStarNode end, AStarNode[][] checked, int count) {
//		System.out.println();
		if (foundRing) {
			return;
		}
		if (checked[start.getY()][start.getX()] != null) {
			return;
		}
		count++;
//		AStarNode curNode = new AStarNode(barrierX, barrierY);
		checked[start.getY()][start.getX()] = start;

//		System.err.println("当前check： " + start.getX() + " " + start.getY());
		AStarNode[] neighbor = getNeighborBarrier8(start);
		if (neighbor == null || neighbor.length == 0) {
			return;
		}
		for (AStarNode aStarNode : neighbor) {
			if (aStarNode == null) {
				continue;
			}
			if (foundRing) {
				return;
			}
			if (isBarrier(aStarNode.getX(), aStarNode.getY())) {
				aStarNode.setFather(start);
				// 找到了自己，可能是一个环
				if (aStarNode.equals(end)) {
					if (count < 4) {
						continue;
					}
					boolean ring = isRingComplete(checked, start, end);
					if (ring) {
//						System.err.println("找到了完整的环");
//						buildPath(start);
//						printPathData();
						foundRing = true;
						findEmptyGridInBarrier(start);
						return;
					}
				}
				if (checked[aStarNode.getY()][aStarNode.getX()] != null) {
					continue;
				}
//				System.err.println("当前neighbor： " + aStarNode.getX() + " " + aStarNode.getY() + "  count : " + count);
				checkRing(aStarNode, end, checked, count);
			}
		}
	}
	private boolean isRingComplete(AStarNode[][] checked, AStarNode start, AStarNode end) {

		return isNaberPass(checked, start, end) == 0;
	}

	private int isNaberPass(AStarNode[][] checked, AStarNode start, AStarNode end) {
		// 起始点
		int num = 0;
		AStarNode[] neighbor = getNeighborBarrier8(start);
		for (AStarNode aStarNode : neighbor) {
			if (aStarNode == null || aStarNode == start) {
				continue;
			}
			if (aStarNode == end) {
				continue;
			}
			if (checked[aStarNode.getY()][aStarNode.getX()] != null) {
				num += this.isPass(aStarNode, end);
			}
		}
		// 起始点的8方向周围，如果不是结束点，检查起始点是否可以到达
		// 周围点，如果可以到达， 并且周围点访问过，
		// and 周围点在4方向上有结束点
		// num =  
		return num;

	}

	private int isPass(AStarNode start, AStarNode end) {
		int num = 0;
		AStarNode[] neighbor = getNeighborBarrier4(start);
		for (AStarNode aStarNode : neighbor) {
			if (aStarNode == null) {
				continue;
			}
			if (aStarNode.equals(end)) {
				num++;
			}
		}
		return num;
	}
	/** 
	 * 构建环路径
	 * @param node
	 */
	private void buildPath(AStarNode node) {
//		System.err.println(node);
		barrierDataShow[node.getY()][node.getX()] = barrierDataIndex++;
		while (node.getFather() != null) {
			node = node.getFather();
//			System.err.println(node);
			barrierDataShow[node.getY()][node.getX()] = barrierDataIndex++;
		}

	}
	private void printPathData() {

//		int index = 1;
		for (int y = 0; y < barrierDataShow.length; y++) {

			for (int x = 0; x < barrierDataShow[y].length; x++) {
				int value = barrierDataShow[y][x];
				if (value > 0) {
					if (value < 10) {
						System.out.print(value + "  ");
					} else if (value < 100) {
						System.out.print(value + " ");
					} else {
						System.out.print(value);
					}
				} else {
					System.out.print(0 + "  ");
				}
				System.out.print(" ");
			}
			System.out.println();
		}

	}
	private void findEmptyGridInBarrier(AStarNode node) {
		List<AStarNode> nodes = new ArrayList<>();
//		List<Grid> emptyGrids = new ArrayList<>();
		nodes.add(node);
		while (node.getFather() != null) {
			node = node.getFather();
			nodes.add(node);
		}

		// 最外圈不用检查
		for (int i = 1; i < barrierData.length - 1; i++) {
			loop: for (int j = 1; j < barrierData[0].length - 1; j++) {
				if (barrierData[i][j] == 0) {
					boolean hasLeft = false;
					loopLeft: for (int x = 1; x < j; x++) {
						for (AStarNode aStarNode : nodes) {
							if (aStarNode.getX() == x && aStarNode.getY() == i) {
								hasLeft = true;
								break loopLeft;
							}
						}
					}
					boolean hasRight = false;
					loopRight: for (int x = j; x < barrierData.length - 1; x++) {
						for (AStarNode aStarNode : nodes) {
							if (aStarNode.getX() == x && aStarNode.getY() == i) {
								hasRight = true;
								break loopRight;
							}
						}
					}
					boolean hasUp = false;
					loopUp: for (int y = 1; y < i; y++) {
						for (AStarNode aStarNode : nodes) {
							if (aStarNode.getX() == j && aStarNode.getY() == y) {
								hasUp = true;
								break loopUp;
							}
						}
					}
					boolean hasDown = false;
					loopDown: for (int y = i; y < barrierData.length - 1; y++) {
						for (AStarNode aStarNode : nodes) {
							if (aStarNode.getX() == j && aStarNode.getY() == y) {
								hasDown = true;
								break loopDown;
							}
						}
					}
					if (hasLeft && hasRight && hasUp && hasDown) {
						emptyGridsInBarrier.add(new Grid(j - 1, i - 1));
					}
				}
			}
		}

	}

	private AStarNode[] getNeighborBarrier8(AStarNode current) {
		AStarNode[] neighbors = new AStarNode[8];
		for (int i = 0; i < neighbors.length; i++) {
			int x = -1;
			int y = -1;
			if (i == 0) {
				x = current.getX() - 1;
				y = current.getY();
			} else if (i == 1) {
				x = current.getX() + 1;
				y = current.getY();
			} else if (i == 2) {
				x = current.getX();
				y = current.getY() - 1;
			} else if (i == 3) {
				x = current.getX();
				y = current.getY() + 1;
			} else if (i == 4) {
				x = current.getX() + 1;
				y = current.getY() + 1;
			} else if (i == 5) {
				x = current.getX() - 1;
				y = current.getY() - 1;
			} else if (i == 6) {
				x = current.getX() - 1;
				y = current.getY() + 1;
			} else if (i == 7) {
				x = current.getX() + 1;
				y = current.getY() - 1;
			}
			if (x < 0 || y < 0 || x >= barrierData[0].length || y >= barrierData.length || (x == current.getX() && y == current.getY()))
				continue;
			if (!isBarrier(x, y))
				continue;
			neighbors[i] = new AStarNode(x, y);
		}
		return neighbors;
	}
	private AStarNode[] getNeighborBarrier4(AStarNode current) {
		AStarNode[] neighbors = new AStarNode[4];
		for (int i = 0; i < neighbors.length; i++) {
			int x = -1;
			int y = -1;
			if (i == 0) {
				x = current.getX() - 1;
				y = current.getY();
			} else if (i == 1) {
				x = current.getX() + 1;
				y = current.getY();
			} else if (i == 2) {
				x = current.getX();
				y = current.getY() - 1;
			} else if (i == 3) {
				x = current.getX();
				y = current.getY() + 1;
			}

			if (x < 0 || y < 0 || x >= barrierData[0].length || y >= barrierData.length || (x == current.getX() && y == current.getY()))
				continue;
			if (!isBarrier(x, y))
				continue;
			neighbors[i] = new AStarNode(x, y);
		}
		return neighbors;
	}

	public boolean isBarrier(int x, int y) {
		return barrierData[y][x] == 1;
	}

	public List<Grid> getEmptyGridsInBarrier() {
		return emptyGridsInBarrier;
	}

}
