package client.model;

import client.Model;
import common.model.Direction;
import common.model.GameEvent;
import common.model.Position;

/**
 * Created by Razi on 2/12/2015.
 */
public class Cell {

    private Model mModel;
    private String mId;
    private Position mPos;
    final private int mTeamId;
    private int mEnergy;

    public Cell (Model model, String id, Position pos, int teamId, int energy) {
        mModel = model;
        mId = id;
        mPos = pos;
        mTeamId = teamId;
        mEnergy = energy;
    }

    public void move(Direction direction)
    {
        GameEvent event = new GameEvent(GameEvent.TYPE_MOVE);
        event.setObjectId(mId);
        event.setArg(direction.toString(), GameEvent.ARG_INDEX_MOVE_DIRECTION);

        mModel.addEvent(event);
    }

    public void gainResource()
    {
        GameEvent event = new GameEvent(GameEvent.TYPE_GAIN_RESOURCE);
        event.setObjectId(mId);

        mModel.addEvent(event);
    }

    public void mitosis()
    {
        GameEvent event = new GameEvent(GameEvent.TYPE_MITOSIS);
        event.setObjectId(mId);

        mModel.addEvent(event);
    }

    public String getId() {
        return mId;
    }

    public Position getPos() {
        return mPos;
    }

    public int getTeamId() {
        return mTeamId;
    }

    public int getEnergy() {
        return mEnergy;
    }
}
