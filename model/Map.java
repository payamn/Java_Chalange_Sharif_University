package client.model;

import common.model.Position;
import common.data.BlockData;
import common.data.MapSize;
import common.data.StaticData;
import common.util.ServerConstants;

/**
 * Created by Razi on 2/13/2015.
 */
public class Map {

    private int width, height;
    private Block[][] blocks;


    public Map(MapSize mapSize, StaticData[] mapData) {
        blocks = new Block[mapSize.getHeight()][mapSize.getWidth()];
        for(StaticData s : mapData)
        {
            if(isBlockType(s.getType())) {
                BlockData blockData = new BlockData(s);
                setChange(blockData);
            }
        }
    }

    public boolean setChange(BlockData blockData)
    {
        if(!isBlockType(blockData.getType()))
        {
            return false;
        }
        Position pos = blockData.getPosition();
        if(at(pos) == null)
        {
            blocks[pos.getY()][pos.getX()] = new Block(blockData);
        }
        else
        {
            at(pos).setChange(blockData);
        }
        return true;
    }

    public boolean isBlockType(String type)
    {
        switch (type)
        {
            case ServerConstants.BLOCK_TYPE_NONE:
            case ServerConstants.BLOCK_TYPE_NORMAL:
            case ServerConstants.BLOCK_TYPE_MITOSIS:
            case ServerConstants.BLOCK_TYPE_RESOURCE:
            case ServerConstants.BLOCK_TYPE_IMPASSABLE:
                return true;
            default:
                //nothing yet !
                return false;
        }
    }

    public Block at(Position pos)
    {
        return blocks[pos.getY()][pos.getX()];
    }

    public Block at(int x, int y)
    {
        return blocks[y][x];
    }
}
