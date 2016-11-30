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
    private OnStateChangeListener stateListener;

    public void save(Bundle bundle) {
        bundle.putInt(STATE, state);
    }

    public void restore(Bundle bundle) {
        state = bundle.getInt(STATE);
    }

    public void setState(int newState) {
        if (!hasEdge(state, newState))
            throw new IllegalStateException("cannot change state from " + state + " to state " + newState);
        setStateInternal(newState);
        update();
    }

    private void setStateInternal(int newState) {
        Edge edge = edges.get(state).get(newState);
        state = newState;
        Edge.OnStateChangedListener listener = edge.onStateChangedListener;
        if (listener != null)
            listener.onStateChanged();
        if (stateListener != null)
            stateListener.onStateChange(state);
    }

    public void reset() {
        state = STATE_NEW;
    }

    public void addEdge(int stateFrom, int stateTo, Edge.OnTryChangeListener listener, Edge.OnStateChangedListener listener2) {
        Edge edge = new Edge(listener, listener2);
        if (edges.indexOfKey(stateFrom) < 0) {
            SparseArray<Edge> list = new SparseArray<>();
            list.put(stateTo, edge);
            edges.put(stateFrom, list);
        } else {
            edges.get(stateFrom).put(stateTo, edge);
        }
    }

    public boolean hasEdge(int stateFrom, int stateTo) {
        return edges.indexOfKey(stateFrom) >= 0 && edges.get(stateFrom).indexOfKey(stateTo) >= 0;
    }

    public int getState() {
        return state;
    }

    public void setOnStateChangeListener(OnStateChangeListener stateListener) {
        this.stateListener = stateListener;
    }

    public void update() {
        while (updateInternal()) ;
    }

    private boolean updateInternal() {
        SparseArray<Edge> listeners = edges.get(state);
        for (int i = 0; i < listeners.size(); i++) {
            int newState = listeners.keyAt(i);
            Edge.OnTryChangeListener onTryChangeListener = listeners.get(newState).onTryChangeListener;
            if (onTryChangeListener == null)
                return false;
            if (onTryChangeListener.onTryChange()) {
                setStateInternal(newState);
                return true;
            }
        }
        return false;
    }
}
