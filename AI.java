package client;

import client.model.Block;
import client.model.Cell;
import common.model.Direction;
import common.model.Position;
import common.util.Constants;
import common.util.ServerConstants;

import java.beans.DesignMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm.WordListener;

/**
 * AI class. You should fill body of the method {@link #doTurn}. Do not change
 * name or modifiers of the methods or fields. You can add as many methods or
 * fields as you want! You could use model to get information about current
 * state of the game.
 */
public class AI {

	private static boolean avoidChale = true;
	private static final int MAX_LEVEL = 5;
	private static HashMap<Integer, Short> enamyCells = null;
	private static HashSet<Integer> MitosisBlocks = new HashSet<Integer>();
	private static Vector<Position> ResourceBlocks = new Vector<Position>();
	private static Vector<Position> notFound = new Vector<Position>();
	private static HashMap<String, HashMap<String, Boolean>> isConnected = new HashMap<String, HashMap<String, Boolean>>();
	private static HashMap<Integer, Integer> trapResources = new HashMap<Integer, Integer>();
	// private static HashMap<Cell, Vector<Position>> cell_visited = new
	// HashMap<Cell, Vector<Position>>();
	// private static HashMap<Integer, HashMap<Integer, HashMap<String,
	// Integer>>> visitedMap = new HashMap<Integer, HashMap<Integer,
	// HashMap<String, Integer>>>();
	private static visitedInfo[][] visitedMapArray = null;

	Random rnd = new Random();

	private void attack(Cell mine, Direction d, Position attack_pos) {
		int id = attack_pos.x + attack_pos.y * 1000;
		enamyCells.put(id, (short) (enamyCells.get(id) - mine.getAttack()));
		mine.attack(d);
		System.out.println("atacked");
	}

	private enamyFound bestAttackDirecation(Position p) {
		enamyFound myEnam = new enamyFound();
		myEnam.enamyEnergy = Short.MAX_VALUE;
		for (Direction d : Direction.values()) {
			if (enamyCells.containsKey(p.getNextPos(d).x + p.getNextPos(d).y
					* 1000)) {
				if (enamyCells
						.get(p.getNextPos(d).x + p.getNextPos(d).y * 1000) < myEnam.enamyEnergy
						&& enamyCells.get(p.getNextPos(d).x + p.getNextPos(d).y
								* 1000) > 0) {
					myEnam.FoundedEnamy += 1;
					myEnam.enamyEnergy = enamyCells.get(p.getNextPos(d).x
							+ p.getNextPos(d).y * 1000);
					myEnam.attackDirection = d;
				}
			}

		}
		return myEnam;

	}

	private int isVisitedBy(String id, int x, int y) { // 2 means found but not
														// with this id, 1 means
		// System.out.println("x: "+x+" y: "+y); // found and this cell
		// visit it before
		if (visitedMapArray[x][y].visitedBy.containsKey(id))
			return visitedMapArray[x][y].visitedBy.get(id);
		if (visitedMapArray[x][y].visitedBy.keySet().size() > 0) {
			return -1;
		}
		return 0;
		// if (visitedMap.containsKey(x)) {
		// HashMap<Integer, HashMap<String, Integer>> a = visitedMap.get(x);
		// if (a.containsKey(y)) {
		// HashMap<String, Integer> a1 = a.get(y);
		// if (a1.containsKey(id)) {
		//
		// return a1.get(id);
		// } else {
		// return -1;
		// }
		// }
		// }
		// return 0;
	}

	private Boolean connected(String id, int x, int y) {
		// System.out.println("-0-0-0-0-0- in conected");
		if (visitedMapArray[x][y].visitedBy.keySet().size() > 0) {
			for (String bb : visitedMapArray[x][y].visitedBy.keySet())
				if (isConnected.containsKey(bb)) {
					HashMap<String, Boolean> ss = isConnected.get(bb);
					// System.out.println(bb + "found one");
					if (ss.containsKey(id))
						return true;

				} else if (bb.equals(id)) {
					// System.out.println("khodama hastam" + x + y);
					return true; // ???????????????????? chera dorsot kar
									// nemikone :(

				}
		}
		// }
		// if (visitedMap.containsKey(x)) {
		// HashMap<Integer, HashMap<String, Integer>> a = visitedMap.get(x);
		// if (a.containsKey(y)) {
		// HashMap<String, Integer> a1 = a.get(y);
		// for (String bb : a1.keySet()) {
		// // System.out.println(bb);
		// if (isConnected.containsKey(bb)) {
		// HashMap<String, Boolean> ss = isConnected.get(bb);
		// // System.out.println(bb + "found one");
		// if (ss.containsKey(id))
		// return true;
		//
		// } else if (bb.equals(id)) {
		// // System.out.println("khodama hastam" + x + y);
		// return true; // ???????????????????? chera dorsot kar
		// // nemikone :(
		//
		// }
		// }
		// }
		// }

		return false;
	}

	private void addVisited(String id, int x, int y, int status, int turn) {
		if (status == 1) {
			visitedMapArray[x][y].visitedBy.put(id, 1);
		} else {
			if (visitedMapArray[x][y].visitedBy.containsKey(id)) {
				visitedMapArray[x][y].visitedBy.put(id, turn);
			} else {
				visitedMapArray[x][y].visitedBy.put(id, turn);
			}

		}
	}

	// System.out.println("added x: " + x + " added y: " + y);
	// if (visitedMap.containsKey(x)) {
	// HashMap<Integer, HashMap<String, Integer>> a = visitedMap.get(x);
	// if (a.containsKey(y)) {
	// HashMap<String, Integer> a1 = a.get(y);
	// if (a1.containsKey(id)) {
	// // if (a1.get(id) == 20)
	// // avoidChale = false;
	//
	// a1.put(id, a1.get(id) + status);
	// a.put(y, a1);
	// visitedMap.put(x, a);
	// } else {
	// for (String komaki : a1.keySet()) {
	// HashMap<String, Boolean> added1 = new HashMap<String, Boolean>();
	// added1.put(komaki, true);
	// HashMap<String, Boolean> added2 = new HashMap<String, Boolean>();
	// added2.put(id, true);
	// isConnected.put(komaki, added2);
	// isConnected.put(id, added1);
	//
	// }
	// a1.put(id, status);
	// a.put(y, a1);
	// visitedMap.put(x, a);
	// }
	// } else {
	// HashMap<String, Integer> a1 = new HashMap<String, Integer>();
	// a1.put(id, status);
	// a.put(y, a1);
	// visitedMap.put(x, a);
	// }
	// } else {
	// HashMap<Integer, HashMap<String, Integer>> a = new HashMap<Integer,
	// HashMap<String, Integer>>();
	// HashMap<String, Integer> a1 = new HashMap<String, Integer>();
	// a1.put(id, status);
	// a.put(y, a1);
	// visitedMap.put(x, a);
	// // System.out.println("mamuli");
	//
	// }

	public void doTurn(World world) {
		if (visitedMapArray == null) {
			Class c = new visitedInfo().getClass();
			visitedInfo[] a0 = (visitedInfo[]) java.lang.reflect.Array
					.newInstance(c, 0);
			visitedMapArray = (visitedInfo[][]) java.lang.reflect.Array
					.newInstance(a0.getClass(),
							world.getMapSize().getWidth() + 1);
			for (int i = 0; i < world.getMapSize().getWidth(); i++) {
				visitedMapArray[i] = (visitedInfo[]) java.lang.reflect.Array
						.newInstance(c, world.getMapSize().getHeight() + 1);
			}

			for (int i = 0; i < world.getMapSize().getWidth(); i++) {
				for (int j = 0; j < world.getMapSize().getHeight(); j++) {
					visitedMapArray[i][j] = new visitedInfo();
				}
			}

		}
		enamyCells = new HashMap<Integer, Short>();
		for (Cell c : world.getEnemyCells()) {
			enamyCells.put(c.getPos().x + c.getPos().y * 1000,
					(short) c.getEnergy());
		}
		// System.out.println(world.getTurn());
		if (world.getTurn() >= 340) {
			avoidChale = false;
		}
		for (Cell c : world.getMyCells()) {
			addVisited(c.getId(), c.getPos().x, c.getPos().y, 2,
					world.getTurn());
		}

		for (Cell c : world.getMyCells()) {
			// System.out.println(c.getId());
			// if (world.getMap().at(c.getPos()).getType()
			// .equals(Constants.BLOCK_TYPE_MITOSIS)
			// && c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
			// c.mitosis();
			// continue;
			// } else if (world.getMap().at(c.getPos()).getType()
			// .equals(Constants.BLOCK_TYPE_RESOURCE)
			// && world.getMap().at(c.getPos()).getResource() > 0) {
			// if (c.getEnergy() < Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
			// c.gainResource();
			// continue;
			// } else if (c.getEnergy() < Constants.CELL_MAX_ENERGY) {
			// if (isVisitedBy(c.getId(), c.getPos().x, c.getPos().y) > 3
			// || world.getTurn() > 400)
			// c.gainResource();
			// continue;
			// }
			//
			// }

			// BFS part
			// Vector<Position> vis = new Vector<Position>();

			// if (!visited(c.getPos(), vis))
			// vis.add(c.getPos());
			int lvl = 1;
			// int count = 0;
			Queue<info> Q = new LinkedList<info>();
			Q.add(new info(null, 0, c.getPos(), lvl));
			// HashMap<Integer, Boolean> visFixed = new HashMap<Integer,
			// Boolean>();
			while (!Q.isEmpty()) {
				// count ++;
				// if (count >= 2000)
				// break;
				info inf = Q.poll();
				lvl = inf.lvl;
				// set level
				// if (Math.abs(inf.pos.x - c.getPos().x) != 0)
				// lvl = Math.abs(inf.pos.x - c.getPos().x) + 1;
				// else if (Math.abs(inf.pos.y - c.getPos().y) != 0)
				// lvl = Math.abs(inf.pos.y - c.getPos().y) + 1;
				// get block

				if (lvl == MAX_LEVEL)
					break;

				for (Direction d : Direction.values()) {
					int score = inf.score;
					Block b = null;
					try {
						b = world.getMap().at(inf.pos.getNextPos(d));
					} catch (Exception e) {
						continue;
					}
					info checkFather = new info();
					checkFather = inf;
					boolean flagContinue = false;
					while (checkFather != null) {

						if (checkFather.pos.x == inf.pos.getNextPos(d).x
								&& checkFather.pos.y == inf.pos.getNextPos(d).y) {
							flagContinue = true;
							break;
						}
						checkFather = checkFather.father;
					}
					if (flagContinue)
						continue;

					// System.out.println(inf.pos.getNextPos(d).y+" x: "+inf.pos.getNextPos(d).x);
					if (!isPossible(inf.pos, d, world, c.getJump(), world)
							&& !b.getType().equals(Constants.BLOCK_TYPE_NONE))
						continue;

					info nextInf = new info();
					nextInf.canResource = inf.canResource;
					// bug fix -> impassible check
					if (b.getType().equals(Constants.BLOCK_TYPE_IMPASSABLE))
						continue; // goto next direction

					if (b.getType().equals(Constants.BLOCK_TYPE_MITOSIS)
							&& !inf.mitosis) {

						// if (!visited(inf.pos.getNextPos(d), MitosisBlocks))
						// MitosisBlocks.add(inf.pos.getNextPos(d));

						if (c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
							score += 120000 / lvl;
						}

					} else if (b.getType()
							.equals(Constants.BLOCK_TYPE_RESOURCE)
							&& b.getResource() > 0) {
						// if (!visited(inf.pos.getNextPos(d), ResourceBlocks))
						// ResourceBlocks.add(inf.pos.getNextPos(d));

						if (inf.canResource + c.getEnergy() <= Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
							// score += 9000;
							int thisResource = Constants.CELL_MIN_ENERGY_FOR_MITOSIS
									- (inf.canResource + c.getEnergy());
							if (b.getResource() < thisResource) {
								thisResource = b.getResource();
							}
							nextInf.canResource += thisResource;
							if (world.getTurn() > 340)
								score += (120000 * thisResource) / lvl;
							else
								score += (12000 * thisResource) / lvl;
							// c.move(d);
							// return ;
						} else if (inf.canResource + c.getEnergy() < Constants.CELL_MAX_ENERGY) {
							int thisResource = Constants.CELL_MAX_ENERGY
									- (inf.canResource + c.getEnergy());
							if (b.getResource() < thisResource) {
								thisResource = b.getResource();
							}
							if (world.getTurn() > 340)
								score += (1200 * thisResource) / lvl;

							nextInf.canResource += thisResource;

						}

					} else if (b.getType().equals(Constants.BLOCK_TYPE_NONE)) {
						// notFound.add(inf.pos.getNextPos(d));
						score += 12000 / lvl;
						nextInf.isNoneBlock = true;

					} else if (b.getType().equals(Constants.BLOCK_TYPE_NORMAL)) {
						score -= 5;
					}
					// check for height
					//System.out.println(c.getJump());
					if (!b.getType().equals(Constants.BLOCK_TYPE_NONE)
							&& getHeightBlock(b.getPos(), world)
									- getHeightBlock(world.getMap().at(inf.pos)
											.getPos(), world) < -1
									* c.getJump()   ||
									(trapResources.containsKey(b.getPos().x+b.getPos().y*1000)&&trapResources.get(b.getPos().x+b.getPos().y*1000)<=c.getJump())) {
						if (!visitedMapArray[inf.pos.x][inf.pos.y]
								.getAble2move(d)
								&& connected(c.getId(), b.getPos().x,
										b.getPos().y) == false) {
						nextInf.inChale = true;
							score -= 200;
							// }
						} else {
							visitedMapArray[inf.pos.x][inf.pos.y]
									.setAble2move(d);
						}
					}

					if (lvl == 1) {
						// if (isDanger(c.getPos().getNextPos(d), world)) //
						// ignore
						// // the
						// // move
						// continue;
						boolean skip = false;

						for (Cell c1 : world.getMyCells()) {
							if (c1.getPos().x == inf.pos.getNextPos(d).x
									&& c1.getPos().y == inf.pos.getNextPos(d).y) {
								skip = true;
								break;
							}
						}
						if (skip)
							continue;
					}

					// visited
					int myvisite = isVisitedBy(c.getId(),
							inf.pos.getNextPos(d).x, inf.pos.getNextPos(d).y);
					if (myvisite > 0) {
						// System.out.println(visited);
						score -= 100 * myvisite;
					} else if (myvisite == -1) {
						score -= 90;
					}

					// push

					nextInf.lvl = lvl + 1;
					if (nextInf.isNoneBlock == false)
						nextInf.isNoneBlock = inf.isNoneBlock;
					if (nextInf.inChale == false)
						nextInf.inChale = inf.inChale;
					nextInf.father = inf;
					nextInf.score = score;
					nextInf.pos = b.getPos();
					if (lvl == 1) {
						nextInf.d = d;
					} else {
						nextInf.d = inf.d;
					}
					if (myvisite <= 0 && nextInf.inChale == false
							&& !nextInf.isNoneBlock) {
						addVisited(c.getId(), b.getPos().x, b.getPos().y, 1,
								world.getTurn());

					}

					Q.add(nextInf);
					// System.out.println("added: x: "+i.pos.x+" y: "+i.pos.y);

				}

			}

			// find max
			int max_score = Integer.MIN_VALUE;

			Direction last_direction = Direction.values()[rnd.nextInt(6)];

			// injaro baiad badan vardarim
			if (Q.isEmpty())
				System.err.println("chera q khalie");
			while (!Q.isEmpty()) {
				info i = Q.poll();

				// if (visited(c.getPos().getNextPos(i.d), vis)) {
				// System.out.println("ID : " + c.getId() + " POS : " +
				// c.getPos().x + " " + c.getPos().y);
				// continue; // ignore move X((((
				// }
				if (avoidChale == true) {

					if (i.score > max_score && !i.inChale) {
						max_score = i.score;
						last_direction = i.d;
					}
				} else if (i.score > max_score) {
					max_score = i.score;
					last_direction = i.d;
				}

			}

			// System.out.println(c.getId());
			if (world.getMap().at(c.getPos()).getType()
					.equals(Constants.BLOCK_TYPE_MITOSIS)
					&& c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
				c.mitosis();
				continue;
			} else if (world.getMap().at(c.getPos()).getType()
					.equals(Constants.BLOCK_TYPE_RESOURCE)
					&& world.getMap().at(c.getPos()).getResource() > 0) {
				System.out
						.println(canMoveAfterGainRes(c, last_direction, world));
				if (c.getEnergy() < Constants.CELL_MIN_ENERGY_FOR_MITOSIS
						&& canMoveAfterGainRes(c, last_direction, world)) {
					c.gainResource();
					continue;
				} else if (c.getEnergy() < Constants.CELL_MAX_ENERGY) {
					if (world.getTurn() > 400){
						c.gainResource();
						continue;
					}
				}
				else if (canMoveAfterGainRes(c, last_direction, world)==false){
					if (!trapResources.containsKey(c.getPos().x+c.getPos().y*1000))
						trapResources.put(c.getPos().x+c.getPos().y*1000, c.getJump());
					else
						trapResources.put(c.getPos().x+c.getPos().y*1000, Math.max(c.getJump(), trapResources.get(c.getPos().x+c.getPos().y*1000)));
				}

			}
			enamyFound enamyFoundNear = bestAttackDirecation(c.getPos());
			if (enamyFoundNear.FoundedEnamy > 0) {
				System.out.println("attacking "+enamyFoundNear.FoundedEnamy);
				attack(c, enamyFoundNear.attackDirection, c.getPos()
						.getNextPos(enamyFoundNear.attackDirection));
				continue;
			}

			try {
				c.move(last_direction);
			//	System.out.println("ID : " + c.getId() + " DIR : "
			//			+ last_direction);
			} catch (Exception e) {
				continue;
				// System.out.println("eeeeeeeeeeeeeeeeeeeeee");
			}

		}
	}

	public static int getHeightBlock(Position p, World world) {
		Block b = world.getMap().at(p);
		if (b.getType() == Constants.BLOCK_TYPE_RESOURCE)
			return Math.min(9, b.getHeight() + b.getResource() / 50);
		else
			return b.getHeight();
	}

	public static int getHeightResourceAfterGain(Position p, World world,
			int gainResource) {
		Block b = world.getMap().at(p);
		int res = Math.min(gainResource, b.getResource());

		return Math.min(9, b.getHeight() + (b.getResource() - res) / 50);
	}

	public static boolean canMoveAfterGainRes(Cell c, Direction d, World world) {
		Block khodam = world.getMap().at(c.getPos());
		Block destin = world.getMap().at(c.getPos().getNextPos(d));
		int resourceHeightAfftergain = getHeightResourceAfterGain(
				khodam.getPos(), world, c.getGainRate());
		if (resourceHeightAfftergain - getHeightBlock(destin.getPos(), world) > -1
				* c.getJump()) {
			return true;
		}
		return false;
	}

	public static boolean visited(Position chk, Vector<Position> pos) {

		for (Position p : pos) {
			if (p.x == chk.x && p.y == chk.y)
				return true;
		}
		return false;
	}

	// public static boolean isDanger(Position p, World w) {
	// // max height!
	// int count = 0;
	//
	// for (Direction d : Direction.values()) {
	// Position pos = p.getNextPos(d);
	// try {
	// if (w.getMap().at(pos).getHeight()
	// - w.getMap().at(p).getHeight() > 2) {
	// ++count;
	// }
	// } catch (Exception e) {
	//
	// }
	// }
	// if (count < 5)
	// return false;
	// System.err.println("too dangeram <3");
	// return true;
	// }

	public static boolean isPossible(Position last, Direction d, World w,
			int jump, World world) {
		// is it possible to go there? :|
		try {
			if (getHeightBlock(w.getMap().at(last.getNextPos(d)).getPos(),
					world)
					- getHeightBlock(w.getMap().at(last).getPos(), world) > jump)
				return false;
		} catch (Exception e) {

		}
		return true;
	}
}

class info {
	public boolean inChale;
	public info father;
	public Direction d;
	public int score;
	public Position pos;
	public short path_size;
	public boolean mitosis;
	public int canResource;
	public int lvl;
	public boolean isNoneBlock;

	public info(Direction d, int score, Position p, int level) {
		this.d = d;
		canResource = 0;
		isNoneBlock = false;
		this.score = score;
		pos = p;
		mitosis = false;
		lvl = level;
		father = null;
		inChale = false;
	}

	public info() {
	}

}

class enamyFound {
	public short FoundedEnamy;
	public Direction attackDirection;
	public short enamyEnergy = 0;

	public enamyFound() {
		FoundedEnamy = 0;
	}
}

class visitedInfo {
	public HashMap<String, Integer> visitedBy;
	public boolean[] able2move = new boolean[6];

	public visitedInfo() {
		visitedBy = new HashMap<String, Integer>();
		for (int i = 0; i < 6; i++) {
			able2move[i] = false;
		}
	}

	public void setAble2move(Direction d) {
		able2move[d.ordinal()] = true;
	}

	public boolean getAble2move(Direction d) {
		return able2move[d.ordinal()];
	}
}