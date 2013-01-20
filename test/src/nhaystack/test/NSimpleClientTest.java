//
// Copyright (c) 2012, Brian Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   26 Sep 2012  Brian Frank  Creation
//
package nhaystack.test;

import haystack.*;
import haystack.io.*;
import haystack.client.*;
import haystack.test.*;

/**
 * NSimpleClientTest -- this test requires an instance of Niagara
 * running localhost port 80 with the nhaystack_simple station
 * and user "admin", pwd "".
 */
public class NSimpleClientTest extends NTest
{

    final String URI = "http://localhost/haystack/";
    HClient client;

//////////////////////////////////////////////////////////////////////////
// Main
//////////////////////////////////////////////////////////////////////////

    public void test() throws Exception
    {
        verifyAuth();
        verifyAbout();
        verifyOps();
        verifyFormats();
        verifyRead();
//        verifyEval();
//        verifyWatches();
//        verifyHisRead();
//        verifyHisWrite();
    }

//////////////////////////////////////////////////////////////////////////
// Auth
//////////////////////////////////////////////////////////////////////////

    void verifyAuth() throws Exception
    {
        // get bad credentials
        try { HClient.open(URI, "baduser", "badpass").about(); fail(); } catch (CallNetworkException e) { verifyException(e); }
        try { HClient.open(URI, "admin",   "badpass").about(); fail(); } catch (CallNetworkException e) { verifyException(e); }

        // create proper client
        this.client = HClient.open(URI, "admin", "");
    }

//////////////////////////////////////////////////////////////////////////
// About
//////////////////////////////////////////////////////////////////////////

    void verifyAbout() throws Exception
    {
        HDict r = client.about();
        verifyEq(r.getStr("haystackVersion"), "2.0");
        verifyEq(r.getStr("productName"), "nhaystack");
        verifyEq(r.getStr("tz"), HTimeZone.DEFAULT.name);
    }

//////////////////////////////////////////////////////////////////////////
// Ops
//////////////////////////////////////////////////////////////////////////

    void verifyOps() throws Exception
    {
        HGrid g = client.ops();

        // verify required columns
        verify(g.col("name")  != null);
        verify(g.col("summary") != null);

        // verify required ops
        verifyGridContains(g, "name", "about");
        verifyGridContains(g, "name", "ops");
        verifyGridContains(g, "name", "formats");
        verifyGridContains(g, "name", "read");
    }

//////////////////////////////////////////////////////////////////////////
// Formats
//////////////////////////////////////////////////////////////////////////

    void verifyFormats() throws Exception
    {
        HGrid g = client.formats();

        // verify required columns
        verify(g.col("mime")  != null);
        verify(g.col("read") != null);
        verify(g.col("write") != null);

        // verify required ops
        verifyGridContains(g, "mime", "text/plain");
        verifyGridContains(g, "mime", "text/zinc");
    }

//////////////////////////////////////////////////////////////////////////
// Reads
//////////////////////////////////////////////////////////////////////////

    void verifyRead() throws Exception
    {
        HGrid grid = client.readAll("id");
        verifyEq(grid.numRows(), showLinkedHistories ? 5 : 4);
        verifyEq(grid.row(0).get("id"), HRef.make("nhaystack_simple:c.OTk~"));
        verifyEq(grid.row(1).get("id"), HRef.make("nhaystack_simple:c.MTA0"));
        verifyEq(grid.row(2).get("id"), HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvQXVkaXRIaXN0b3J5"));
        verifyEq(grid.row(3).get("id"), HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvTG9nSGlzdG9yeQ~~"));

        if (showLinkedHistories)
            verifyEq(grid.row(4).get("id"), HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvU2luZVdhdmUx"));

        HDict dict = client.readById(HRef.make("nhaystack_simple:c.OTk~"));
        verifyEq(dict.get("axType"), HStr.make("kitControl:SineWave"));
        verify(dict.has("foo"));
        verify(dict.has("bar"));
        verifyEq(dict.get("kind"), HStr.make("Number"));
        verify(dict.has("his"));
        verifyEq(dict.get("curStatus"), HStr.make("ok"));
        verifyEq(dict.get("hisInterpolate"), HStr.make("cov"));
        verifyEq(dict.get("axSlotPath"), HStr.make("slot:/Foo/SineWave1"));
        verifyEq(dict.get("units"), HStr.make("�F"));
        verify(dict.has("point"));
        verifyEq(dict.get("tz"), HStr.make("New_York"));
        double curVal = dict.getDouble("curVal");
        verify(curVal >= 0.0 && curVal <= 100.0);

        if (showLinkedHistories)
            verifyEq(dict.get("axHistoryRef"), HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvU2luZVdhdmUx"));
        else
            verify(dict.missing("axHistoryRef"));

        dict = client.readById(HRef.make("nhaystack_simple:c.MTA0"));
        verifyEq(dict.get("axType"), HStr.make("kitControl:SineWave"));
        verify(dict.missing("foo"));
        verify(dict.missing("bar"));
        verifyEq(dict.get("kind"), HStr.make("Number"));
        verify(dict.missing("his"));
        verifyEq(dict.get("curStatus"), HStr.make("ok"));
        verify(dict.missing("hisInterpolate"));
        verifyEq(dict.get("axSlotPath"), HStr.make("slot:/Foo/SineWave2"));
        verifyEq(dict.get("units"), HStr.make("psi"));
        verify(dict.has("point"));
        verify(dict.missing("tz"));
        curVal = dict.getDouble("curVal");
        verify(curVal >= 0.0 && curVal <= 100.0);
        verify(dict.missing("axHistoryRef"));

        dict = client.readById(HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvQXVkaXRIaXN0b3J5"));
        verifyEq(dict.get("axType"), HStr.make("history:HistoryConfig"));
        verify(dict.missing("kind"));
        verify(dict.has("his"));
        verify(dict.missing("curStatus"));
        verify(dict.missing("curVal"));
        verifyEq(dict.get("tz"), HStr.make("New_York"));
        verifyEq(dict.get("axHistoryId"), HStr.make("/nhaystack_simple/AuditHistory"));
        verify(dict.missing("hisInterpolate"));
        verify(dict.missing("units"));
        verify(dict.missing("axPointRef"));

        dict = client.readById(HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvTG9nSGlzdG9yeQ~~"));
        verifyEq(dict.get("axType"), HStr.make("history:HistoryConfig"));
        verify(dict.missing("kind"));
        verify(dict.has("his"));
        verify(dict.missing("curStatus"));
        verify(dict.missing("curVal"));
        verifyEq(dict.get("tz"), HStr.make("New_York"));
        verifyEq(dict.get("axHistoryId"), HStr.make("/nhaystack_simple/LogHistory"));
        verify(dict.missing("hisInterpolate"));
        verify(dict.missing("units"));
        verify(dict.missing("axPointRef"));

        if (showLinkedHistories)
        {
            dict = client.readById(HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvU2luZVdhdmUx"));
            verifyEq(dict.get("axType"), HStr.make("history:HistoryConfig"));
            verifyEq(dict.get("kind"), HStr.make("Number"));
            verify(dict.has("his"));
            verify(dict.missing("curStatus"));
            verify(dict.missing("curVal"));
            verifyEq(dict.get("tz"), HStr.make("New_York"));
            verifyEq(dict.get("axHistoryId"), HStr.make("/nhaystack_simple/SineWave1"));
            verifyEq(dict.get("hisInterpolate"), HStr.make("cov"));
            verifyEq(dict.get("units"), HStr.make("�F"));
            verifyEq(dict.get("axPointRef"), HRef.make("nhaystack_simple:c.OTk~"));
        }
        else
        {
            verify(client.readById(HRef.make("nhaystack_simple:h.L25oYXlzdGFja19zaW1wbGUvU2luZVdhdmUx"), false) == null);
        }
    }

////////////////////////////////////////////////////////////////////////////
//// Watches
////////////////////////////////////////////////////////////////////////////
//
//    void verifyWatches() throws Exception
//    {
//        // create new watch
//        HWatch w = client.watchOpen("Java Haystack Test");
//        verifyEq(w.id(), null);
//        verifyEq(w.dis(), "Java Haystack Test");
//
//        // do query to get some recs
//        HGrid recs = client.readAll("ahu");
//        verify(recs.numRows() >= 4);
//        HDict a = recs.row(0);
//        HDict b = recs.row(1);
//        HDict c = recs.row(2);
//        HDict d = recs.row(3);
//
//        // do first sub
//        HGrid sub = w.sub(new HRef[] { a.id(), b.id() });
//        verifyEq(sub.numRows(), 2);
//        verifyEq(sub.row(0).dis(), a.dis());
//        verifyEq(sub.row(1).dis(), b.dis());
//
//        // now add c, bad, d
//        HRef badId = HRef.make("badBadBad");
//        try { w.sub(new HRef[] { badId }).dump(); fail(); } catch (UnknownRecException e) { verifyException(e); }
//        sub = w.sub(new HRef[] { c.id(), badId , d.id() }, false);
//        verifyEq(sub.numRows(), 3);
//        verifyEq(sub.row(0).dis(), c.dis());
//        verifyEq(sub.row(1).missing("id"), true);
//        verifyEq(sub.row(2).dis(), d.dis());
//
//        // verify state of watch now
//        verify(client.watch(w.id()) == w);
//        verifyEq(client.watches().length, 1);
//        verify(client.watches()[0] == w);
//        verifyEq(w.lease().millis(), 60000L);
//
//        // poll for changes (should be none yet)
//        HGrid poll = w.pollChanges();
//        verifyEq(poll.numRows(), 0);
//
//        // make change to b and d
//        verifyEq(b.has("javaTest"), false);
//        verifyEq(d.has("javaTest"), false);
//        client.eval("commit(diff(readById(@" + b.id().val + "), {javaTest:123}))");
//        client.eval("commit(diff(readById(@" + d.id().val + "), {javaTest:456}))");
//        poll = w.pollChanges();
//        verifyEq(poll.numRows(), 2);
//        HDict newb, newd;
//        if (poll.row(0).id().equals(b.id())) { newb = poll.row(0); newd = poll.row(1); }
//        else { newb = poll.row(1); newd = poll.row(0); }
//        verifyEq(newb.dis(), b.dis());
//        verifyEq(newd.dis(), d.dis());
//        verifyEq(newb.get("javaTest"), HNum.make(123));
//        verifyEq(newd.get("javaTest"), HNum.make(456));
//
//        // poll refresh
//        poll = w.pollRefresh();
//        verifyEq(poll.numRows(), 4);
//        verifyGridContains(poll, "id", a.id());
//        verifyGridContains(poll, "id", b.id());
//        verifyGridContains(poll, "id", c.id());
//        verifyGridContains(poll, "id", d.id());
//
//        // remove d, and then poll changes
//        w.unsub(new HRef[] { d.id() });
//        client.eval("commit(diff(readById(@" + b.id().val + "), {-javaTest}))");
//        client.eval("commit(diff(readById(@" + d.id().val + "), {-javaTest}))");
//        poll = w.pollChanges();
//        verifyEq(poll.numRows(), 1);
//        verifyEq(poll.row(0).dis(), b.dis());
//        verifyEq(poll.row(0).has("javaTest"), false);
//
//        // remove a and c and poll refresh
//        w.unsub(new HRef[] { a.id(), c.id() });
//        poll = w.pollRefresh();
//        verifyEq(poll.numRows(), 1);
//        verifyEq(poll.row(0).dis(), b.dis());
//
//        // close
//        String expr = "folioDebugWatches().findAll(x=>x->dis.contains(\"Java Haystack Test\")).size";
//        verifyEq(client.eval(expr).row(0).getInt("val"), 1);
//        w.close();
//        try { poll = w.pollRefresh(); fail(); } catch (Exception e) { verifyException(e); }
//        verifyEq(client.eval(expr).row(0).getInt("val"), 0);
//        verifyEq(client.watch(w.id(), false), null);
//        verifyEq(client.watches().length, 0);
//    }
//
////////////////////////////////////////////////////////////////////////////
//// His Reads
////////////////////////////////////////////////////////////////////////////
//
//    void verifyHisRead() throws Exception
//    {
//        HDict kw = client.read("kw and siteMeter");
//        HGrid his = client.hisRead(kw.id(), "yesterday");
//        verifyEq(his.meta().id(), kw.id());
//        verifyEq(ts(his.meta(), "hisStart").date, HDate.today().minusDays(1));
//        verifyEq(ts(his.meta(), "hisEnd").date, HDate.today());
//        verify(his.numRows() > 90);
//        int last = his.numRows()-1;
//        verifyEq(ts(his.row(0)).date, HDate.today().minusDays(1));
//        verifyEq(ts(his.row(0)).time, HTime.make(0, 15));
//        verifyEq(ts(his.row(last)).date, HDate.today());
//        verifyEq(ts(his.row(last)).time, HTime.make(0, 0));
//        verifyEq(numVal(his.row(0)).unit, "kW");
//    }
//
//    private HDateTime ts(HDict r, String col) { return (HDateTime)r.get(col); }
//    private HDateTime ts(HDict r) { return (HDateTime)r.get("ts"); }
//    private HNum numVal(HRow r) { return (HNum)r.get("val"); }
//
////////////////////////////////////////////////////////////////////////////
//// His Reads
////////////////////////////////////////////////////////////////////////////
//
//    void verifyHisWrite() throws Exception
//    {
//        // setup test
//        HDict kw = client.read("kw and not siteMeter");
//        clearHisWrite(kw);
//
//        // create some items
//        HDate date = HDate.make(2010, 6, 7);
//        HTimeZone tz = HTimeZone.make(kw.getStr("tz"));
//        HHisItem[] write = new HHisItem[5];
//        for (int i=0; i<write.length; ++i)
//        {
//            HDateTime ts = HDateTime.make(date, HTime.make(i+1, 0), tz);
//            HVal val = HNum.make(i, "kW");
//            write[i] = HHisItem.make(ts, val);
//        }
//
//        // write and verify
//        client.hisWrite(kw.id(), write);
//        Thread.sleep(200);
//        HGrid read = client.hisRead(kw.id(), "2010-06-07");
//        verifyEq(read.numRows(), write.length);
//        for (int i=0; i<read.numRows(); ++i)
//        {
//            verifyEq(read.row(i).get("ts"), write[i].ts);
//            verifyEq(read.row(i).get("val"), write[i].val);
//        }
//
//        // clean test
//        clearHisWrite(kw);
//    }
//
//    private void clearHisWrite(HDict rec)
//    {
//        // existing data and verify we don't have any data for 7 June 20120
//        String expr = "hisClear(@" + rec.id().val + ", 2010-06)";
//        client.eval(expr);
//        HGrid his = client.hisRead(rec.id(), "2010-06-07");
//        verifyEq(his.numRows(), 0);
//    }

//////////////////////////////////////////////////////////////////////////
// Utils
//////////////////////////////////////////////////////////////////////////

    void verifyGridContains(HGrid g, String col, String val) { verifyGridContains(g, col, HStr.make(val)); }
    void verifyGridContains(HGrid g, String col, HVal val)
    {
        boolean found = false;
        for (int i=0; i<g.numRows(); ++i)
        {
            HVal x = g.row(i).get(col, false);
            if (x != null && x.equals(val)) { found = true; break; }
        }
        if (!found)
        {
            System.out.println("verifyGridContains " + col + "=" + val + " failed!");
            fail();
        }
    }

}