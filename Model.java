package client;

import client.model.Map;
import com.google.gson.Gson;
import common.data.*;
import common.model.Event;
import common.network.data.Message;
import common.network.data.ReceivedMessage;
import common.util.ServerConstants;

import java.util.ArrayList;

/**
 * Model contains data which describes current state of the game.
 */
public class Model {

    private long turnTimeout = 400;
    private long turnStartTime;
    private ArrayList<Event> events;
    private World world;

    public void handleInitMessage(ReceivedMessage msg) {
        // game developers' todo store data

        Gson gson = new Gson();
        ClientInitInfo initInfo = gson.fromJson(msg.args.get(0), ClientInitInfo.class);

        StaticData[] mapData =  gson.fromJson(msg.args.get(1), StaticData[].class);
        Map map = new Map(initInfo.getMapSize(), mapData);

        //TODO STATIC DIFF

        world = new World(this, initInfo, map);
    }

    public void handleTurnMessage(ReceivedMessage msg) {
        turnStartTime = System.currentTimeMillis();
        // game developers' todo store data

        Gson gson = new Gson();

        world.setTurn(gson.fromJson(msg.args.get(0), Integer.class));
        ClientTurnData clientTurnData = gson.fromJson(msg.args.get(1), ClientTurnData.class);

        //set statics
        for(StaticData s : clientTurnData.getStatics())
        {
            world.setStaticChange(s);
        }

        //set dynamics
        world.clearDynamics();
        for(DynamicData d : clientTurnData.getDynamics())
        {
            if(d.getType().equals(ServerConstants.GAME_OBJECT_TYPE_CELL))
            {
                CellData cd = new CellData(d);
                world.addCell(cd);
            }
            else{
                //nothing yet!
            }
        }

        //set transients    TODO

        events = new ArrayList<>();
    }

    public long getTurnTimeout() {
        return turnTimeout;
    }

    public long getTurnTimePassed() {
        return System.currentTimeMillis() - turnStartTime;
    }

    public long getTurnRemainingTime() {
        return turnTimeout - getTurnTimePassed();
    }

    public Message getClientTurn() {
        // game developers' todo collect client's events as a single message
        Event[] tEvents = new Event[events.size()];
        tEvents = events.toArray(tEvents);
        Object [] args = new Object[1];
        return new Message("event", tEvents);
        /*
        Object [] args = new Object[1];
        args[0] = gameEvents;
        return new Message("event", args);
         */
    }

    public void addEvent(Event event)
    {
        events.add(event);
    }

    public World getWorld()
    {
        return world;
    }

}
