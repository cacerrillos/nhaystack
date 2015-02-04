//
// Copyright (c) 2012, J2 Innovations
// Licensed under the Academic Free License version 3.0
//
// History:
//   22 Mar 2013  Mike Jarmy  Creation
//
package nhaystack.util;

import java.util.*;

import javax.baja.history.*;
import javax.baja.history.db.*;
import javax.baja.naming.*;
import javax.baja.security.*;
import javax.baja.status.*;
import javax.baja.sys.*;
import javax.baja.timezone.*;

import org.projecthaystack.*;
import nhaystack.res.*;

/**
  * TypeUtil maps between Haystack types and Baja types.
  */
public abstract class TypeUtil
{
    public static BSimple toBajaSimple(HVal val)
    {
        if (val instanceof HStr)
        {
            return BString.make(((HStr) val).val); 
        }
        else if (val instanceof HNum)
        {
            HNum num = (HNum) val;

            if (num.unit == null)
            {
                return BDouble.make(num.val); 
            }
            else
            {
                Unit unit = Resources.getSymbolUnit(num.unit);
                if (unit.quantity.equals("time"))
                    return makeRelTime(num, unit);
                else
                    return BDouble.make(num.val); 
            }
        }
        else if (val instanceof HBool)
        {
            return BBoolean.make(((HBool) val).val); 
        }
        
        throw new IllegalStateException("Cannot convert " + 
            val.getClass() + ": " + val);
    }

    private static BRelTime makeRelTime(HNum num, Unit unit)
    {
        if      (unit.name.equals("nanosecond"))        return BRelTime.make((long) (num.val / 1000 / 1000));
        else if (unit.name.equals("microsecond"))       return BRelTime.make((long) (num.val / 1000));
        else if (unit.name.equals("millisecond"))       return BRelTime.make((long) num.val);
        else if (unit.name.equals("hundredths_second")) return BRelTime.make((long) (num.val * 10));
        else if (unit.name.equals("tenths_second"))     return BRelTime.make((long) (num.val * 100));
        else if (unit.name.equals("second"))            return BRelTime.makeSeconds ((int) num.val);
        else if (unit.name.equals("minute"))            return BRelTime.makeMinutes ((int) num.val);
        else if (unit.name.equals("hour"))              return BRelTime.makeHours   ((int) num.val);
        else if (unit.name.equals("day"))               return BRelTime.makeHours   ((int) (num.val * 24));

        else throw new IllegalStateException(
            "Cannot convert " + num + " to RelTime");
    }

    public static HVal fromBajaSimple(BSimple simple)
    {
        if (simple instanceof BString)
        {
            return HStr.make(simple.toString());
        }
        else if (simple instanceof BNumber)
        {
            return HNum.make(((BNumber) simple).getDouble());
        }
        else if (simple instanceof BBoolean)
        {
            return HBool.make(((BBoolean) simple).getBoolean());
        }
        else if (simple instanceof BEnum)
        {
            return HStr.make(SlotUtil.fromNiagara(((BEnum) simple).getTag()));
        }
        else
            throw new IllegalStateException("Cannot convert " + simple.getClass());
    }

    /**
      * Convert the args into a parameter that can be used to invoke the Action.
      */
    public static BValue actionArgsToBaja(HDict args, Action action)
    {
        // NOTE we can't use args.size(), because if args is an HRow,
        // the size() can be non-zero even if args.iterator().hasNext() is false.

        BValue def = action.getParameterDefault();

        // null
        if (def == null)
        {
            return null;
        }
        // simple
        else if (def instanceof BSimple)
        {
            Map.Entry e = (Map.Entry) args.iterator().next();
            HVal val = (HVal) e.getValue();
            BSimple simple = toBajaSimple(val);

            if (!simple.getType().is(action.getParameterType()))
                throw new IllegalStateException(
                    "type mismatch: " + simple.getType() + 
                    " is not " + action.getParameterType());

            return simple;
        }
        // complex
        else
        {
            BComplex cpx = (BComplex) def;

            // Set each slot in the BComplex to a dict entry.
            // Note that we do not currently support nesting structs within structs.
            Iterator it = args.iterator();
            while (it.hasNext())
            {
                Map.Entry e = (Map.Entry) it.next();
                cpx.set(
                    (String) e.getKey(), 
                    toBajaSimple((HVal) e.getValue()));
            }

            return cpx;
        }
    }

    public static BTimeZone toBajaTimeZone(HTimeZone tz)
    {
        return BTimeZone.getTimeZone(tz.java.getID());
    }

    public static BStatus toBajaStatus(HStr curStatus)
    {
        if (curStatus.val.equals("ok"))       return BStatus.ok;
        if (curStatus.val.equals("fault"))    return BStatus.fault;
        if (curStatus.val.equals("down"))     return BStatus.down;
        if (curStatus.val.equals("disabled")) return BStatus.disabled;
        if (curStatus.val.equals("unknown"))  return BStatus.nullStatus;

        throw new IllegalStateException("Cannot convert " + curStatus.val + " to BStatus");
    }

////////////////////////////////////////////////////////////////
// permissions
////////////////////////////////////////////////////////////////

    /**
      * Check if the permissions for the component allow us to read
      */
    public static boolean canRead(BComponent comp, Context cx)
    {          
        BPermissions perm = permissions(comp, cx);
        return 
            perm.has(BPermissions.OPERATOR_READ) ||
            perm.has(BPermissions.ADMIN_READ);
    }

    /**
      * Check if the permissions for the component allow us to write
      */
    public static boolean canWrite(BComponent comp, Context cx)
    {          
        BPermissions perm = permissions(comp, cx);
        return 
            perm.has(BPermissions.OPERATOR_WRITE) ||
            perm.has(BPermissions.ADMIN_WRITE);
    }

    /**
      * Check if the permissions for the component allow us to invoke
      */
    public static boolean canInvoke(BComponent comp, Context cx)
    {          
        BPermissions perm = permissions(comp, cx);
        return 
            perm.has(BPermissions.OPERATOR_INVOKE) ||
            perm.has(BPermissions.ADMIN_INVOKE);
    }

    private static BPermissions permissions(BComponent comp, Context cx)
    {
        // For history config, you have to look up the BIHistory
        // and get the permissions for that.
        if (comp instanceof BHistoryConfig)
        {
            BHistoryConfig cfg = (BHistoryConfig) comp;
            BHistoryDatabase historyDb = (BHistoryDatabase) 
                BOrd.make("history:").get(); 
            BIHistory his = historyDb.getHistory(cfg.getId());
            if (his == null) return BPermissions.DEFAULT;
            return his.getPermissions(cx);
        }
        else
        {
            return comp.getPermissions(cx);
        }
    }
}

