//
// Copyright (c) 2012, J2 Innovations
// Licensed under the Academic Free License version 3.0
//
// History:
//   18 Apr 2014  Mike Jarmy  Creation

package nhaystack.driver;

import java.io.*;
import javax.baja.sys.*;
import org.projecthaystack.*;
import org.projecthaystack.io.*;

/**
 * BHTags wraps a Haystack HDict.  BHTags is similar to BHDict,
 * except that it is intended to always be read-only. 
 * <p/>
 * BHTags is used to keep track of the tags that were imported when points or 
 * histories were added to a BNHaystackServer.
 */
public final class BHTags
    extends BSimple
{
    /**
      * Make a BHTags instance from an HDict.
      */
    public static BHTags make(HDict dict) 
    { 
        return new BHTags(dict);  
    }

    /**
      * Make a BHTags instance from a ZINC-encoded string.
      */
    public static BHTags make(String s) 
    { 
        HZincReader zr = new HZincReader(s);
        return new BHTags(zr.readDict());
    }

    private BHTags(HDict dict) 
    { 
        this.dict = dict;
    }

////////////////////////////////////////////////////////////////
// Object
////////////////////////////////////////////////////////////////

    public int hashCode() 
    { 
        return dict.hashCode(); 
    }

    public boolean equals(Object obj)
    {
        if (this == obj) return true;

        if (!(obj instanceof BHTags)) return false;
        BHTags that = (BHTags) obj;
        return (dict.equals(that.dict));
    }

////////////////////////////////////////////////////////////////
// BSimple
////////////////////////////////////////////////////////////////

    public String toString(Context context)
    {
        return dict.toZinc();
    }

    /**
      * Encode to ZINC format
      */
    public void encode(DataOutput encoder) throws IOException
    { 
        encoder.writeUTF(dict.toZinc()); 
    }

    /**
      * Decode from ZINC format
      */
    public BObject decode(DataInput decoder) throws IOException
    { 
        HZincReader zr = new HZincReader(decoder.readUTF());
        return new BHTags(zr.readDict());
    }  

    /**
      * Encode to ZINC format
      */
    public String encodeToString() throws IOException
    { 
        return dict.toZinc(); 
    }

    /**
      * Decode from ZINC format
      */
    public BObject decodeFromString(String s) throws IOException
    { 
        HZincReader zr = new HZincReader(s);
        return new BHTags(zr.readDict());
    }

////////////////////////////////////////////////////////////////
// Access
////////////////////////////////////////////////////////////////

    /**
      * Return the underlying HDict.
      */
    public HDict getDict() { return dict; }

////////////////////////////////////////////////////////////////
// Attributes
//////////////////////////////////////////////////////////////// 

    public BIcon getIcon() { return ICON; }
    private static final BIcon ICON = BIcon.make("module://nhaystack/nhaystack/icons/tag.png");

    /** * The default is HDict.EMPTY. */
    public static final BHTags DEFAULT = new BHTags(HDict.EMPTY);

    public Type getType() { return TYPE; }
    public static final Type TYPE = Sys.loadType(BHTags.class);

    private final HDict dict;
}
