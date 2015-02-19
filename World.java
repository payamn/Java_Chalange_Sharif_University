package client;


import client.model.Cell;
import client.model.Map;

import common.data.*;
import common.util.ServerConstants;

import java.util.ArrayList;

/**
 * Created by Razi on 2/10/2015.
 */
public class World {

    /*class MapSize {
        int height;
        int width;
    }*/

    private Model model;

    private String[] teams;
    private int myId;
    private String myName;

    private MapSize mapSize;
    //private int mapWidth;
    //private int mapHeight;
    private Map map;

    private int turn;

    private ArrayList<Cell> allCells;
    private ArrayList<Cell> myCells;
    private ArrayList<Cell> enemyCells;

    /*public World(ArrayList<String>teams, int myId, String myName, int mapWidth, int mapHeight, Map map, int turn)
    {
        this.teams = new String[teams.size()];
        this.teams = teams.toArray(this.teams);
        this.myId = myId;
        this.myName = myName;
        this. mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = map;
        this.turn = turn;
    }*/

    public World(Model model, ClientInitInfo initInfo, Map map)
    {
        this.model = model;
        this.teams = initInfo.getTeams();
        this.myId = initInfo.getYourInfo().getId();
        this.myName = initInfo.getYourInfo().getName();
        this.mapSize = initInfo.getMapSize();
        this.map = map;
        this.turn = initInfo.getTurn();
    }

    public String[] getTeams() {
        return teams;
    }

    public int getMyId() {
        return myId;
    }

    public String getMyName() {
        return myName;
    }

    /*public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }*/
    public MapSize getMapSize()
    {
        return mapSize;
    }

    public Map getMap() {
        return map;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void clearDynamics()
    {
        allCells = new ArrayList<>();
        myCells = new ArrayList<>();
        enemyCells = new ArrayList<>();
    }

    public void addCell(CellData cellData)
    {
        Cell cell = new Cell(model, cellData.getId(), cellData.getPosition(),cellData.getTeamId(),cellData.getEnergy());
        allCells.add(cell);
        //System.out.println(cellData.getTeamId());
        //System.out.println(myId);
        if(cellData.getTeamId() == myId) {
            myCells.add(cell);
        }
        else
        {
            enemyCells.add(cell);
        }
    }

    public ArrayList<Cell> getAllCells() {
        return allCells;
    }

    public ArrayList<Cell> getMyCells() {
        return myCells;
    }

    public ArrayList<Cell> getEnemyCells() {
        return enemyCells;
    }

    public void setStaticChange(StaticData s) {
        switch (s.getType())
        {
            case ServerConstants.BLOCK_TYPE_NONE:
            case ServerConstants.BLOCK_TYPE_NORMAL:
            case ServerConstants.BLOCK_TYPE_MITOSIS:
            case ServerConstants.BLOCK_TYPE_RESOURCE:
            case ServerConstants.BLOCK_TYPE_IMPASSABLE:
                BlockData blockData = new BlockData(s);
                map.setChange(blockData);
                break;
            default:
                //nothing yet !
                break;
        }
    }
}
