//
// Copyright (c) 2012, J2 Innovations
// Licensed under the Academic Free License version 3.0
//
// History:
//   29 Mar 2013  Mike Jarmy  Creation
//
package nhaystack.server;

import java.util.*;
import javax.baja.collection.*;
import javax.baja.control.*;
import javax.baja.control.enums.*;
import javax.baja.history.*;
import javax.baja.log.*;
import javax.baja.naming.*;
import javax.baja.status.*;
import javax.baja.sys.*;
import javax.baja.util.*;

import haystack.*;
import haystack.server.*;
import nhaystack.*;
import nhaystack.collection.*;
import nhaystack.server.storehouse.*;
import nhaystack.site.*;

/**
  * Cache stores cached information that makes it faster to look things up.
  */
public class Cache
{
    Cache(NHServer server)
    {
        this.server = server;
    }

    /**
      * Rebuild the cache.
      */
    public synchronized void rebuild()
    {
        long ticks = Clock.ticks();
        if (initialized)
        {
            LOG.message("Begin cache rebuild.");
            rebuildComponentCache();
            rebuildHistoryCache();
            LOG.message("End cache rebuild " + 
                (Clock.ticks()-ticks) + "ms.");
        }
        else
        {
            LOG.message("Begin cache build.");
            rebuildComponentCache();
            rebuildHistoryCache();
            initialized = true;
            LOG.message("End cache build " + 
                (Clock.ticks()-ticks) + "ms.");
        }
    }

    /**
      * Get the history config that goes with the remote point, or return null.
      */
    public synchronized BHistoryConfig getHistoryConfig(RemotePoint remotePoint)
    {
        if (!initialized) throw new IllegalStateException("Cache is not initialized.");

        return (BHistoryConfig) remoteToConfig.get(remotePoint);
    }

    /**
      * Get the control point that goes with the remote point, or return null.
      */
    public synchronized BControlPoint getControlPoint(RemotePoint remotePoint)
    {
        if (!initialized) throw new IllegalStateException("Cache is not initialized.");

        return (BControlPoint) remoteToPoint.get(remotePoint);
    }

////////////////////////////////////////////////////////////////
// private
////////////////////////////////////////////////////////////////

    private void rebuildComponentCache()
    {
        remoteToPoint = new HashMap();
        ConfigStorehouse storehouse = server.getConfigStorehouse();

//        Array sitesArr = new Array(HDict.class);
//        Array equipsArr = new Array(HDict.class);

        Iterator iterator = new ComponentTreeIterator(
            (BComponent) BOrd.make("slot:/").resolve(server.getService(), null).get());

        while (iterator.hasNext())
        {
            BComponent comp = (BComponent) iterator.next();

            // remote point
            if (comp instanceof BControlPoint)
            {
                BControlPoint point = (BControlPoint) comp;
                if (point.getProxyExt().getType().is(RemotePoint.NIAGARA_PROXY_EXT)) 
                {
                    RemotePoint remote = RemotePoint.fromControlPoint(point);
                    if (remote != null) remoteToPoint.put(remote, point);
                }
            }

//            // check tags
//            HDict tags = storehouse.createComponentTags(comp);
//            if (tags.has("site")) sitesArr.add(tags);
//            if (tags.has("equip")) equipsArr.add(tags);
        }
    }

    private void rebuildHistoryCache()
    {
        remoteToConfig = new HashMap();
        HistoryStorehouse storehouse = server.getHistoryStorehouse();

        BIHistory[] histories = server.getService().getHistoryDb().getHistories(); 
        for (int i = 0; i < histories.length; i++)
        {
            BIHistory h = histories[i];
            BHistoryId hid = h.getId();

            // ignore local histories
            if (hid.getDeviceName().equals(Sys.getStation().getStationName()))
                continue;

            BHistoryConfig cfg = h.getConfig();
            RemotePoint remotePoint = RemotePoint.fromHistoryConfig(cfg);
            if (remotePoint != null)
                remoteToConfig.put(remotePoint, cfg);
        }
    }

////////////////////////////////////////////////////////////////
// attribs
////////////////////////////////////////////////////////////////

    private static final Log LOG = Log.getLog("nhaystack");

    private final NHServer server;
    private boolean initialized = false;

    private Map remoteToConfig = null; // RemotePoint -> BHistoryConfig
    private Map remoteToPoint  = null; // RemotePoint -> BControlPoint
}

