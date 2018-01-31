package com.mfk.lockfree.singleton.access;

import com.mfk.lockfree.singleton.SingleRef;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class SingleRefAccessTest {
    @Test
    public void testGetAccess() {
        SingleRef<Object> singleRef = new SingleRef<>(Object::new);
        assertNotNull(singleRef.get());
    }
}
