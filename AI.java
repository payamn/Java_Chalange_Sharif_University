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
	private static HashMap<Cell, Vector<Position>> cell_visited = new HashMap<Cell, Vector<Position>>();
	Random rnd = new Random();

	public void doTurn(World world) {

		for(Cell c : world.getMyCells()) {
			if(!cell_visited.containsKey(c)) {
				Vector<Position> v = new Vector<Position>();
				v.add(c.getPos());
				cell_visited.put(c, v);
			}
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
			Queue<info> Q = new LinkedList<info>();
			Q.add(new info(null, 0, c.getPos()));
			int lvl = 1;
			
			while (!Q.isEmpty()) {
				info inf = Q.poll();
				
				// set level
				if(Math.abs(inf.pos.x - c.getPos().x) != 0)
					lvl = Math.abs(inf.pos.x - c.getPos().x) + 1;
				else if(Math.abs(inf.pos.y - c.getPos().y) != 0)
					lvl = Math.abs(inf.pos.y - c.getPos().y) + 1;

				if(lvl == MAX_LEVEL)
					break;
				
				for (Direction d : Direction.values()) {
					int score = inf.score;

					// get block
					Block b = null;
					try {
						b = world.getMap().at(inf.pos.getNextPos(d));
					} catch (Exception e) {
						continue;
					}

					// bug fix -> impassible check
					if(b.getType().equals(Constants.BLOCK_TYPE_IMPASSABLE))
						continue; // goto next direction
					
					if (b.getType().equals(Constants.BLOCK_TYPE_MITOSIS)) {
						MitosisBlocks.add(inf.pos.getNextPos(d));

						if (c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS && lvl == 1) {
							score += 1200;
						}

						else if (c.getEnergy() >= Constants.CELL_MIN_ENERGY_FOR_MITOSIS) {
							score += 120;
						}

					} else if (b.getType()
							.equals(Constants.BLOCK_TYPE_RESOURCE)) {
						ResourceBlocks.add(inf.pos.getNextPos(d));

						if (c.getEnergy() < Constants.CELL_MIN_ENERGY_FOR_MITOSIS
								&& b.getResource() > 0 && lvl == 1) {
							score += 900;
						}
						else if (c.getEnergy() < Constants.CELL_MIN_ENERGY_FOR_MITOSIS
								&& b.getResource() > 0) {
							score += 90;
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
					} else if (!b.getType().equals(
							Constants.BLOCK_TYPE_IMPASSABLE)
							&& b.getHeight()
									- world.getMap().at(inf.pos).getHeight() < -2) {

						if (world.getMyCells().size() == 1) {
							score -= 5000000;
						} else {
							score -= 50;
						}
					}
					if (lvl == 1) {
						if (lastPos.get(c.getId()) != null
								&& lastPos.get(c.getId()).x == inf.pos
										.getNextPos(d).x
								&& lastPos.get(c.getId()).y == inf.pos
										.getNextPos(d).y) {
							score -= 1000;
						}
						
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
					if(cell_visited.get(c).contains(inf.pos.getNextPos(d))) {
						score -= 100;
					}
					else { 
						//System.out.println("YES");
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
				info i = Q.peek();
				Q.remove();
				// System.out.print("emitaz: " + i.score);

				if (i.score > max_score) {
					max_score = i.score;
					last_direction = i.d;
				}
			}

			//System.out.println(max_score);
			// move
			lastPos.put(c.getId(), c.getPos());
			cell_visited.get(c).add(c.getPos().getNextPos(last_direction));
			try {
				Block b = world.getMap().at(c.getPos().getNextPos(last_direction));
				c.move(last_direction);
				System.out.println("ID : " + c.getId() + " DIR : " + last_direction);
			} catch (Exception e) {
				System.out.println("eeeeeeeeeeeeeeeeeeeeee");
			}
			

		}
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
