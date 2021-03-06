//
// Copyright (c) 2012, J2 Innovations
// Licensed under the Academic Free License version 3.0
//
// History:
//   27 Jan 2013  Mike Jarmy Creation
//
package nhaystack.res;

import java.io.*;
import java.util.*;

import javax.baja.sys.*;
import javax.baja.units.*;
import javax.baja.util.*;

/**
  * Resources makes available all the various files downloaded 
  * from project-haystack.org
  */
public class Resources
{
    private static final String TAGS  = "/nhaystack/res/tags.csv";
    private static final String TZ    = "/nhaystack/res/tz.txt";
    private static final String UNITS = "/nhaystack/res/units.txt";

    private static final String ELEC_METER     = "/nhaystack/res/equip-points/elecMeter.txt";
    private static final String VAV            = "/nhaystack/res/equip-points/vav.txt";
    private static final String HEAT_EXCHANGER = "/nhaystack/res/equip-points/heatExchanger.txt";
    private static final String COOLING_TOWER  = "/nhaystack/res/equip-points/coolingTower.txt";
    private static final String CHILLER_PLANT  = "/nhaystack/res/equip-points/chillerPlant.txt";
    private static final String CHILLER        = "/nhaystack/res/equip-points/chiller.txt";
    private static final String AHU            = "/nhaystack/res/equip-points/ahu.txt";

    private static Map kindTags; // <String,Set<String>>

    private static String[] timeZones;

    private static Map unitsByQuantity; // <String,Array<Unit>>
    private static Map unitsBySymbol;   // <String,Unit>
    private static Map unitsByLowerCaseName;   // <String,Unit>
    private static String[] quantities;

    private static String[] markerSets = new String[] {
        "elecMeter", "vav", "heatExchanger", "coolingTower",
        "chillerPlant", "chiller", "ahu" };
    private static Map markerSetTags; // <String,Array<String>>

////////////////////////////////////////////////////////////////
//  load
////////////////////////////////////////////////////////////////

    static
    {
        try
        {
            loadTags();
            loadTimeZones();
            loadUnits();
            loadMarkerSets();
        }
        catch (Exception e)
        {
            throw new BajaRuntimeException(e);
        }
    }

    private static void loadTags() throws Exception
    {
        InputStream in = Resources.class.getResourceAsStream(TAGS);
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));

        kindTags = new HashMap();

        String str = bin.readLine(); // throw away header
        str = bin.readLine();
        while (str != null)
        {
            String[] tokens = TextUtil.split(str, ',');
            String name = tokens[0];
            String kind = tokens[1];

            Set set = (Set) kindTags.get(kind);
            if (set == null)
                kindTags.put(kind, set = new TreeSet());
            set.add(name);

            str = bin.readLine();
        }

        bin.close();
    }

    private static void loadTimeZones() throws Exception
    {
        InputStream in = Resources.class.getResourceAsStream(TZ);
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));

        Array arr = new Array(String.class);

        String str = bin.readLine(); 
        while (str != null)
        {
            arr.add(str.trim());
            str = bin.readLine();
        }

        timeZones = (String[]) arr.trim();
        bin.close();
    }

    private static void loadUnits() throws Exception
    {
        InputStream in = Resources.class.getResourceAsStream(UNITS);
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));

        unitsByQuantity = new HashMap();
        unitsBySymbol = new HashMap();
        unitsByLowerCaseName = new HashMap();

        Array arr = new Array(String.class);
        String curQuant = null;

        String str = bin.readLine(); 
        while (str != null)
        {
            if (!str.equals("")) 
            {
                if (str.startsWith("-- "))
                {
                    curQuant = str.substring(3, str.length() - 3);
                    arr.add(curQuant);
                }
                else
                {
                    String[] tokens = TextUtil.split(str, ',');

                    if (tokens.length == 1)
                        loadUnit(new Unit(curQuant, tokens[0], tokens[0]));
                    else
                        for (int i = 0; i < tokens.length-1; i++)
                            loadUnit(new Unit(curQuant, tokens[0], tokens[i+1]));
                }
            }

            str = bin.readLine();
        }

        quantities = (String[]) arr.trim();
        bin.close();
    }

    private static void loadUnit(Unit unit)
    {
        Array arr = (Array) unitsByQuantity.get(unit.quantity);
        if (arr == null)
            unitsByQuantity.put(unit.quantity, arr = new Array(Unit.class));
        arr.add(unit);

        if (unitsBySymbol.containsKey(unit.symbol))
            throw new BajaRuntimeException("Duplicate symbol: " + unit.symbol);
        unitsBySymbol.put(unit.symbol, unit);

        String name = unit.name.toLowerCase();
        if (!unitsByLowerCaseName.containsKey(name))
            unitsByLowerCaseName.put(name, unit);
    }

    private static void loadMarkerSets() throws Exception
    {
        markerSetTags = new HashMap();

        markerSetTags.put("elecMeter",     loadMarkerSet(ELEC_METER));
        markerSetTags.put("vav",           loadMarkerSet(VAV));
        markerSetTags.put("heatExchanger", loadMarkerSet(HEAT_EXCHANGER));
        markerSetTags.put("coolingTower",  loadMarkerSet(COOLING_TOWER));
        markerSetTags.put("chillerPlant",  loadMarkerSet(CHILLER_PLANT));
        markerSetTags.put("chiller",       loadMarkerSet(CHILLER));
        markerSetTags.put("ahu",           loadMarkerSet(AHU));
    }

    private static Array loadMarkerSet(String resourcePath) throws Exception
    {
        InputStream in = Resources.class.getResourceAsStream(resourcePath);
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));

        Array arr = new Array(String.class);

        String str = bin.readLine(); 
        while (str != null)
        {
            if (!str.equals("") && !str.startsWith("**"))
            {
                while (str.indexOf("  ") != -1)
                    str = TextUtil.replace(str, "  ", " ");
                arr.add(str);
            }

            str = bin.readLine();
        }

        bin.close();
        return arr;
    }

////////////////////////////////////////////////////////////////
// access
////////////////////////////////////////////////////////////////

    /**
      * Get the different 'kinds' of tags: Str, Marker, Bool, etc
      */
    public static String[] getKindTags(String kind)
    {
        if (!kindTags.containsKey(kind)) return new String[0];

        Array arr = new Array(String.class, (Set) kindTags.get(kind));
        return (String[]) arr.trim();
    }

    /**
      * Get all the timezones.
      */
    public static String[] getTimeZones()
    {
        return timeZones;
    }

    /**
      * Get all the different quantities of units.
      */
    public static String[] getUnitQuantities()
    {
        return quantities;
    }

    /**
      * Get all the Units having a given quantity.
      */
    public static Unit[] getUnits(String quantity)
    {
        Array arr = (Array) unitsByQuantity.get(quantity);
        return (Unit[]) arr.trim();
    }

    /**
      * Get all the Units having a given quantity and name.
      */
    public static Unit[] getUnits(String quantity, String name)
    {
        Array a1 = (Array) unitsByQuantity.get(quantity);
        Array a2 = new Array(Unit.class);

        for (int i = 0; i < a1.size(); i++)
        {
            Unit unit = (Unit) a1.get(i);
            if (unit.name.equals(name))
                a2.add(unit);
        }

        return (Unit[]) a2.trim();
    }

    /**
      * Get the unit having a given symbol.
      */
    public static Unit getSymbolUnit(String symbol)
    {
        Unit unit = (Unit) unitsBySymbol.get(symbol);
        if (unit == null)
            throw new BajaRuntimeException(
                "Cannot find Unit for symbol '" + symbol + "'.");
        return unit;
    }

    /**
      * Get the names of all the Marker Sets.
      */
    public static String[] getMarkerSets()
    {
        return markerSets;
    }

    /**
      * Get the space-delimited list of the tags
      * associated with the given Marker Set name.
      */
    public static String[] getMarkerSetTags(String group)
    {
        Array arr = (Array) markerSetTags.get(group);
        if (arr == null) throw new BajaRuntimeException(
            "Cannot find marker group '" + group + "'.");
        return (String[]) arr.trim();
    }

    /**
      * Get the Unit that corresponds to the given Niagara BUnit,
      * or return null if no corresponding Unit can be found.
      */
    public static Unit fromBajaUnit(BUnit bunit)
    {
        Unit unit = (Unit) unitsByLowerCaseName.get(
            TextUtil.replace(bunit.getUnitName(), " ", "_").toLowerCase());

        if (unit != null) return unit;

        if (bunit.getUnitName().equals("dollar")) return getSymbolUnit("$");
        if (bunit.getUnitName().equals("pounds")) return getSymbolUnit("GBP");
        if (bunit.getUnitName().equals("rupee"))  return getSymbolUnit("INR");
        if (bunit.getUnitName().equals("won"))    return getSymbolUnit("KRW");
        if (bunit.getUnitName().equals("yen"))    return getSymbolUnit("JPY");

        return null;
    }

    /**
      * Get the BUnit that corresponds to the given Haystack Unit,
      * or return null if no corresponding BUnit can be found.
      */
    public static BUnit toBajaUnit(Unit unit)
    {
        if (unit.name.equals("us_dollar"))     return BUnit.getUnit("dollar");
        if (unit.name.equals("british_pound")) return BUnit.getUnit("pounds");
        if (unit.name.equals("indian_rupee"))  return BUnit.getUnit("rupee");
        if (unit.name.equals("chinese_yuan"))  return BUnit.getUnit("won");
        if (unit.name.equals("japanese_yen"))  return BUnit.getUnit("yen");

        return BUnit.getUnit(TextUtil.replace(unit.name, "_", " ").toLowerCase());
    }

////////////////////////////////////////////////////////////////
// main
////////////////////////////////////////////////////////////////

    private static void missingUnitsToNiagara() throws Exception
    {
        // print all the haystack unit names which cannot automatically
        // be converted into Baja unit names.

        System.out.println("--------------------------------------------------------");
        System.out.println("Units which cannot be converted from Haystack to Niagara");
        System.out.println();

        String[] quantities = getUnitQuantities();
        for (int i = 0; i < quantities.length; i++)
        {
            String quantity = quantities[i];
            boolean headerPrinted = false;

            Unit[] units = getUnits(quantity);
            for (int j = 0; j < units.length; j++)
            {
                Unit unit = units[j];
                try
                {
                    BUnit bunit = toBajaUnit(unit);
                }
                catch (UnitException e)
                {
                    if (!headerPrinted)
                    {
                        System.out.println(quantity);
                        headerPrinted = true;
                    }
                    System.out.println("    " + unit.name);
                }
            }
        }
    }

    private static void missingUnitsFromNiagara() throws Exception
    {
        // print all the niagara unit names which cannot automatically
        // be converted into haystack unit names.

        System.out.println("--------------------------------------------------------");
        System.out.println("Units which cannot be converted from Niagara to Haystack");
        System.out.println();

        UnitDatabase ud = UnitDatabase.getDefault();
        UnitDatabase.Quantity[] quantities = ud.getQuantities();

        for (int i = 0; i < quantities.length; i++)
        {
            UnitDatabase.Quantity q = quantities[i];
            boolean headerPrinted = false;

            BUnit[] units = q.getUnits();
            for (int j = 0; j < units.length; j++)
            {
                BUnit u = units[j];

                if (fromBajaUnit(u) == null)
                {
                    if (!headerPrinted)
                    {
                        System.out.println(q.getName());
                        headerPrinted = true;
                    }

                    System.out.println("    " + u.getUnitName());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        missingUnitsFromNiagara();
        missingUnitsToNiagara();
    }
}
