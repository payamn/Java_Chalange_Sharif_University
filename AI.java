package client;

import client.model.Block;
import client.model.Cell;
import common.model.Direction;
import common.model.Position;
import common.util.Constants;
import common.util.ServerConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.Vector;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import com.sun.org.apache.bcel.internal.generic.NEW;

/**
 * AI class. You should fill body of the method {@link #doTurn}. Do not change
 * name or modifiers of the methods or fields. You can add as many methods or
 * fields as you want! You could use model to get information about current
 * state of the game.
 */
public class AI {

	private static final int MAX_LEVEL = 5;
	private static Vector<Position> MitosisBlocks = new Vector<Position>();
	private static Vector<Position> ResourceBlocks = new Vector<Position>();
	private static Vector<Position> notFound = new Vector<Position>();
	private static HashMap<String, Position> lastPos = new HashMap<String, Position>();
	// private static HashMap<Cell, Vector<Position>> cell_visited = new
	// HashMap<Cell, Vector<Position>>();
	private static HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>> visitedMap = new HashMap<Integer, HashMap<Integer, HashMap<String, Integer>>>();
	Random rnd = new Random();

	private int isVisitedBy(String id, int x, int y) { // 2 means found but not
														// with this id, 1 means
														// found and this cell
														// visit it before
		if (visitedMap.containsKey(x)) {
			HashMap<Integer, HashMap<String, Integer>> a = visitedMap.get(x);
			if (a.containsKey(y)) {
				HashMap<String, Integer> a1 = a.get(y);
				if (a1.containsKey(id)) {
					return a1.get(id);
				} else {
					return -1;
				}
			}
		}
		return 0;
	}

	private void addVisited(String id, int x, int y) {
		if (visitedMap.containsKey(x)) {
			HashMap<Integer, HashMap<String, Integer>> a = visitedMap.get(x);
			if (a.containsKey(y)) {
				HashMap<String, Integer> a1 = a.get(y);
				if (a1.containsKey(id)) {
					a1.put(id, a1.get(id) + 1);
				} else
					a1.put(id, 1);
			} else {
				HashMap<String, Integer> a1 = new HashMap<String, Integer>();
				a1.put(id, 1);
				a.put(y, a1);
			}
		} else {
			HashMap<Integer, HashMap<String, Integer>> a = new HashMap<Integer, HashMap<String, Integer>>();
			HashMap<String, Integer> a1 = new HashMap<String, Integer>();
			a1.put(id, 1);
			a.put(x, a1);
			visitedMap.put(x, a);
		}
	}

	public void doTurn(World world) {

		for (Cell c : world.getMyCells()) {
			addVisited(c.getId(), c.getPos().x, c.getPos().y);
		}

		for (Cell c : world.getMyCells()) {
			// System.out.println(c.getId());
			if (world.getMap().at(c.getPos()).getType()
					.equals(Constants.BLOCK_TYPE_MITOSIS)
					&& c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
				c.mitosis();
				continue;
			} else if (world.getMap().at(c.getPos()).getType()
					.equals(Constants.BLOCK_TYPE_RESOURCE)
					&& c.getEnergy() < Constants.CELL_MIN_ENERGY_FOR_MITOSIS
					&& world.getMap().at(c.getPos()).getResource() != 0) {
				c.gainResource();
				continue;
			}

			// BFS part
			Vector<Position> vis = new Vector<Position>();
			Queue<info> Q = new LinkedList<info>();
			Q.add(new info(null, 0, c.getPos()));
			if (!visited(c.getPos(), vis))
				vis.add(c.getPos());
			int lvl = 1;

			while (!Q.isEmpty()) {
				info inf = Q.poll();

				// set level
				if (Math.abs(inf.pos.x - c.getPos().x) != 0)
					lvl = Math.abs(inf.pos.x - c.getPos().x) + 1;
				else if (Math.abs(inf.pos.y - c.getPos().y) != 0)
					lvl = Math.abs(inf.pos.y - c.getPos().y) + 1;

				if (lvl == MAX_LEVEL)
					break;

				for (Direction d : Direction.values()) {
					int score = inf.score;

					if (!isPossible(c, d, world))
						continue;

					// get block
					Block b = null;
					try {
						b = world.getMap().at(inf.pos.getNextPos(d));
					} catch (Exception e) {
						continue;
					}

					// bug fix -> impassible check
					if (b.getType().equals(Constants.BLOCK_TYPE_IMPASSABLE))
						continue; // goto next direction

					if (b.getType().equals(Constants.BLOCK_TYPE_MITOSIS)) {
						if (!visited(inf.pos.getNextPos(d), MitosisBlocks))
							MitosisBlocks.add(inf.pos.getNextPos(d));

						if (c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS
								&& lvl == 1) {
							score += 120000;
						}

						else if (c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
							score += 1200;
						}

					} else if (b.getType()
							.equals(Constants.BLOCK_TYPE_RESOURCE)) {
						if (!visited(inf.pos.getNextPos(d), ResourceBlocks))
							ResourceBlocks.add(inf.pos.getNextPos(d));

						if (c.getEnergy() < Constants.CELL_MIN_ENERGY_FOR_MITOSIS
								&& b.getResource() > 0 && lvl == 1) {
							score += 9000;
						} else if (c.getEnergy() < Constants.CELL_MIN_ENERGY_FOR_MITOSIS
								&& b.getResource() > 0) {
							score += 200;
						}

					} else if (b.getType().equals(Constants.BLOCK_TYPE_NONE)) {
						notFound.add(inf.pos.getNextPos(d));
						score += 5000;

					} else if (b.getType().equals(Constants.BLOCK_TYPE_NORMAL)) {
						score += 5;
					} else if (!b.getType().equals(
							Constants.BLOCK_TYPE_IMPASSABLE)
							&& b.getHeight()
									- world.getMap().at(inf.pos).getHeight() > 2) {
						continue;
					}

					// check for height
					if (!b.getType().equals(Constants.BLOCK_TYPE_IMPASSABLE)
							&& b.getHeight()
									- world.getMap().at(inf.pos).getHeight() < -2) {

						// if (world.getMyCells().size() == 1) {
						// score -= 5000000; // never go there!
						// } else {
						score -= 5000;
						// }
					}
					if (lvl == 1) {
						if (isDanger(c.getPos().getNextPos(d), world)) // ignore
																		// the
																		// move
							continue;
						/*
						 * if (lastPos.get(c.getId()) != null &&
						 * lastPos.get(c.getId()).x == inf.pos .getNextPos(d).x
						 * && lastPos.get(c.getId()).y == inf.pos
						 * .getNextPos(d).y) { score -= 4000;
						 * //System.out.println("roo xodesh"); }
						 */
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
					int visited = isVisitedBy(c.getId(),
							inf.pos.getNextPos(d).x, inf.pos.getNextPos(d).y);
					if (visited > 0) {
						// System.out.println(visited);
						score -= 100 * visited;
					} else if (visited == -1) {
						score -= 40;
					} else {
						// System.out.println("YES");
						score += 10000;
					}

					// push
					info i = new info();
					if (lvl == 1) {
						i.d = d;
						i.score = score;
						i.pos = inf.pos.getNextPos(d);
					} else {
						i.d = inf.d;
						i.score = score;
						i.pos = inf.pos.getNextPos(d);
					}

					Q.add(i);
				}

			}

			// find max
			int max_score = Integer.MIN_VALUE;

			Direction last_direction = Direction.values()[rnd.nextInt(6)];

			while (!Q.isEmpty()) {
				info i = Q.poll();

				if (visited(c.getPos().getNextPos(i.d), vis)) {
					// System.out.println("ID : " + c.getId() + " POS : " +
					// c.getPos().x + " " + c.getPos().y);
					continue; // ignore move X((((
				}

				if (i.score > max_score) {
					max_score = i.score;
					last_direction = i.d;
				}
			}

			// System.out.println(max_score);
			// move
			lastPos.put(c.getId(), c.getPos());
			addVisited(c.getId(), c.getPos().getNextPos(last_direction).x, c
					.getPos().getNextPos(last_direction).y);
			vis.add(c.getPos());
			try {
				Block b = world.getMap().at(
						c.getPos().getNextPos(last_direction));
				c.move(last_direction);
				System.out.println("ID : " + c.getId() + " DIR : "
						+ last_direction);
			} catch (Exception e) {
				System.out.println("eeeeeeeeeeeeeeeeeeeeee");
			}

		}
	}

	public static boolean visited(Position chk, Vector<Position> pos) {

		for (Position p : pos) {
			if (p.x == chk.x && p.y == chk.y)
				return true;
		}
		return false;
	}

	public static boolean isDanger(Position p, World w) {
		// max height!
		int count = 0;
		
		for (Direction d : Direction.values()) {
			Position pos = p.getNextPos(d);
			try {
				if (w.getMap().at(pos).getHeight()
						- w.getMap().at(p).getHeight() > 2) {
					++ count;
				}
			} catch (Exception e) {

			}
		}
		if(count < 5)
			return false;
		return true;
	}

	public static boolean isPossible(Cell c, Direction d, World w) {
		// is it possible to go there? :|
		try {
			if (w.getMap().at(c.getPos().getNextPos(d)).getHeight()
					- w.getMap().at(c.getPos()).getHeight() > 2)
				return false;
		} catch (Exception e) {

		}
		return true;
	}
}

class info {
	public Direction d;
	public int score;
	public Position pos;
	public short path_size;

	public info(Direction d, int score, Position p) {
		this.d = d;
		this.score = score;
		pos = p;
	}

	public info() {
	}

}
