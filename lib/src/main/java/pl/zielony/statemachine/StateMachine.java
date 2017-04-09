package pl.zielony.statemachine;

import android.os.Bundle;
import android.util.SparseArray;

/**
 * Created by Marcin on 2016-07-30.
 */

public class StateMachine {
    private static final String STATE = "state";

    public static final int STATE_NEW = 0;

    private int state = STATE_NEW;

    private SparseArray<SparseArray<Edge>> edges = new SparseArray<>();
    private SparseArray<OnStateChangedListener> states = new SparseArray<>();
    private OnStateChangedListener globalStateListener;

    public void save(Bundle bundle) {
        bundle.putInt(STATE, state);
    }

    public void restore(Bundle bundle) {
        state = bundle.getInt(STATE);
    }

    public void setState(int newState) {
        setState(newState, null);
    }

    public <Type> void setState(int newState, Type param) {
        if (!hasEdge(state, newState))
            throw new IllegalStateException("cannot change state from " + state + " to state " + newState);
        setStateInternal(newState, param);
        update();
    }

    private <Type> void setStateInternal(int newState, Type param) {
        Edge edge = edges.get(state).get(newState);
        OnStateChangeListener listener = edge.onStateChangedListener;
        if (listener != null)
            listener.onStateChange(param);
        state = newState;
        OnStateChangedListener stateListener = states.get(newState);
        if (stateListener != null)
            stateListener.onStateChanged(state);
        if (globalStateListener != null)
            globalStateListener.onStateChanged(state);
    }

    public void reset() {
        state = STATE_NEW;
    }

    public void addEdge(int stateFrom, int stateTo) {
        addEdge(stateFrom, stateTo, null, null);
    }

    public void addEdge(int stateFrom, int stateTo, OnStateChangeListener listener2) {
        addEdge(stateFrom, stateTo, null, listener2);
    }

    public void addEdge(int stateFrom, int stateTo, OnTryChangeListener listener) {
        addEdge(stateFrom, stateTo, listener, null);
    }

    public void addEdge(int stateFrom, int stateTo, OnTryChangeListener listener, OnStateChangeListener listener2) {
        if (stateFrom == STATE_NEW && edges.indexOfKey(stateFrom) >= 0)
            throw new IllegalStateException("There can be only one entry point");
        Edge edge = new Edge(listener, listener2);
        if (edges.indexOfKey(stateFrom) < 0) {
            SparseArray<Edge> list = new SparseArray<>();
            list.put(stateTo, edge);
            edges.put(stateFrom, list);
        } else if (edges.get(stateFrom).get(stateTo) != null) {
            throw new IllegalStateException("There's already an edge from state " + stateFrom + " to " + stateTo);
        } else {
            edges.get(stateFrom).put(stateTo, edge);
        }
    }

    public void addState(int state, OnStateChangedListener listener) {
        if (states.indexOfKey(state) >= 0)
            throw new IllegalStateException("There's already state " + state);
        states.put(state, listener);
    }

    public boolean hasEdge(int stateFrom, int stateTo) {
        return edges.indexOfKey(stateFrom) >= 0 && edges.get(stateFrom).indexOfKey(stateTo) >= 0;
    }

    public int getState() {
        return state;
    }

    public void setOnStateChangeListener(OnStateChangedListener stateListener) {
        this.globalStateListener = stateListener;
    }

    public void update() {
        while (updateInternal()) ;
    }

    private boolean updateInternal() {
        SparseArray<Edge> listeners = edges.get(state);
        if (listeners == null)
            return false;
        for (int i = 0; i < listeners.size(); i++) {
            int newState = listeners.keyAt(i);
            OnTryChangeListener onTryChangeListener = listeners.get(newState).onTryChangeListener;
            if (onTryChangeListener == null)
                return false;
            if (onTryChangeListener.onTryChange()) {
                setStateInternal(newState, null);
                return true;
            }
        }
        return false;
    }
}
