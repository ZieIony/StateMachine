package pl.zielony.statemachine;

import android.os.Bundle;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StateMachineTest extends TestCase {
    enum TestState {
        STATE1, STATE2, STATE3
    }

    private StateMachine<TestState> stateMachine;

    public void setUp() throws Exception {
        super.setUp();
        stateMachine = new StateMachine<>(TestState.STATE1);
        stateMachine.addEdge(TestState.STATE1, TestState.STATE2);
        stateMachine.addEdge(TestState.STATE2, TestState.STATE3);
    }

    public void testRestore() throws Exception {
        stateMachine.setState(TestState.STATE2);
        Bundle bundle = mock(Bundle.class);
        when(bundle.getSerializable("state")).thenReturn(TestState.STATE2);
        stateMachine.save(bundle);
        stateMachine.setState(TestState.STATE3);
        stateMachine.restore(bundle);
        assertEquals(stateMachine.getState(), TestState.STATE2);
    }

    public void testSetState() throws Exception {
        stateMachine.setState(TestState.STATE2);
        assertEquals(stateMachine.getState(), TestState.STATE2);
    }

    public void testAddEdge() throws Exception {
        try {
            // there's already a nonconditional edge from 1 to 3
            stateMachine.addEdge(TestState.STATE1, TestState.STATE3, () -> true);
            fail();
        } catch (IllegalStateException e) {
        }
        try {
            // this is ok, two edges from 1, both nonconditional
            stateMachine.addEdge(TestState.STATE1, TestState.STATE3);
        } catch (IllegalStateException e) {
            fail();
        }
        try {
            // this is also ok, two edges from 3, both conditional
            stateMachine.addEdge(TestState.STATE3, TestState.STATE2, () -> true);
            stateMachine.addEdge(TestState.STATE3, TestState.STATE1, () -> false);
        } catch (IllegalStateException e) {
            fail();
        }
        try {
            // edge's ends cannot have the same value
            stateMachine.addEdge(TestState.STATE2, TestState.STATE2);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            // there's already an edge from 1 to 2
            stateMachine.addEdge(TestState.STATE1, TestState.STATE2);
            fail();
        } catch (IllegalStateException e) {
        }
    }

    public void testAddState() throws Exception {
        stateMachine.addStateListener(TestState.STATE1, param -> {
        });
        // states and edges are independent
        stateMachine.addEdge(TestState.STATE3, TestState.STATE1);
        stateMachine.addStateListener(TestState.STATE2, param -> {
        });
    }

    public void testUpdate() throws Exception {
        stateMachine.update();
        assertEquals(stateMachine.getState(), TestState.STATE1);
        stateMachine.addEdge(TestState.STATE3, TestState.STATE1, () -> true);
        stateMachine.setState(TestState.STATE2);
        assertEquals(stateMachine.getState(), TestState.STATE2);
        // setState() calls update() internally and there's a conditional edge between 3 and 1
        stateMachine.setState(TestState.STATE3);
        assertEquals(stateMachine.getState(), TestState.STATE1);
    }

}