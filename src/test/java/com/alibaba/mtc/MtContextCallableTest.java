package com.alibaba.mtc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


/**
 * @author ding.lid
 */
public class MtContextCallableTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);

    @BeforeClass
    public static void beforeClass() throws Exception {
        Thread.sleep(1000);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_MtContextCallable() throws Exception {
        MtContext.getContext().set("parent", "parent");
        MtContext.getContext().set("p", "p0");

        Call call = new Call("1");
        MtContextCallable mtContextCallable = MtContextCallable.get(call);
        assertEquals(call, mtContextCallable.getCallable());
        Future future = executorService.submit(mtContextCallable);

        Thread.sleep(100);
        assertEquals("ok", future.get());

        // Child independent & Inheritable
        assertEquals("1", call.copiedContent.get("key"));
        assertEquals("p01", call.copiedContent.get("p"));
        assertEquals("parent", call.copiedContent.get("parent"));

        // restored
        assertEquals(0, call.context.get().size());

        // children do not effect parent
        assertEquals(2, MtContext.getContext().get().size());
        assertEquals("parent", MtContext.getContext().get("parent"));
        assertEquals("p0", MtContext.getContext().get("p"));
    }

    @Test
    public void test_idempotent() throws Exception {
        MtContextCallable<String> call = MtContextCallable.get(new Call("1"));
        assertSame(call, MtContextCallable.get(call));
    }
}
