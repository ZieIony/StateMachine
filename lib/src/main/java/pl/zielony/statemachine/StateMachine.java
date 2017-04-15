package pl.zielony.statemachine;

import android.os.Bundle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcin on 2016-07-30.
 */

public class StateMachine<Type extends Serializable> {
    private static final String STATE = "state";

    private final Type initialState;
    private Type state;

    private Map<Type, Map<Type, Edge>> edges = new HashMap<>();
    private Map<Type, OnMachineStateChangedListener> states = new HashMap<>();
    private OnMachineStateChangedListener globalStateListener;

    public StateMachine(Type initialState) {
        this.initialState = initialState;
        state = initialState;
    }

    public void save(Bundle bundle) {
        bundle.putSerializable(STATE, state);
    }

    public void restore(Bundle bundle) {
        state = (Type) bundle.getSerializable(STATE);
    }

    public void setState(Type newState) {
        setState(newState, null);
    }

    public <ParamType> void setState(Type newState, ParamType param) {
        if (!hasEdge(state, newState))
            throw new IllegalStateException("cannot change state from " + state + " to " + newState);
        setStateInternal(newState, param);
        update();
    }

    private <ParamType> void setStateInternal(Type newState, ParamType param) {
        Edge edge = edges.get(state).get(newState);
        state = newState;
        OnStateChangedListener listener = edge.onStateChangedListener;
        if (listener != null)
            listener.onStateChanged(param);
        OnMachineStateChangedListener stateListener = states.get(newState);
        if (stateListener != null)
            stateListener.onStateChanged(state);
        if (globalStateListener != null)
            globalStateListener.onStateChanged(state);
    }

    public void reset() {
        state = initialState;
    }

    public void addEdge(Type stateFrom, Type stateTo) {
        addEdge(stateFrom, stateTo, null, null);
    }

    public void addEdge(Type stateFrom, Type stateTo, OnStateChangedListener listener2) {
        addEdge(stateFrom, stateTo, null, listener2);
    }

    public void addEdge(Type stateFrom, Type stateTo, OnTryChangeListener listener) {
        addEdge(stateFrom, stateTo, listener, null);
    }

    public void addEdge(Type stateFrom, Type stateTo, OnTryChangeListener listener, OnStateChangedListener listener2) {
        Edge edge = new Edge(listener, listener2);
        if (!edges.containsKey(stateFrom)) {
            Map<Type, Edge> list = new HashMap<>();
            list.put(stateTo, edge);
            edges.put(stateFrom, list);
        } else if (edges.get(stateFrom).get(stateTo) != null) {
            throw new IllegalStateException("There's already an edge from state " + stateFrom + " to " + stateTo);
        } else {
            edges.get(stateFrom).put(stateTo, edge);
        }
    }

    public void addState(Type state, OnMachineStateChangedListener listener) {
        if (states.containsKey(state))
            throw new IllegalStateException("There's already state " + state);
        states.put(state, listener);
    }

    public boolean hasEdge(Type stateFrom, Type stateTo) {
        return edges.containsKey(stateFrom) && edges.get(stateFrom).containsKey(stateTo);
    }

    public Type getState() {
        return state;
    }

    public void setOnStateChangeListener(OnMachineStateChangedListener stateListener) {
        this.globalStateListener = stateListener;
    }

    public void update() {
        while (updateInternal()) ;
    }

    private boolean updateInternal() {
        Map<Type, Edge> listeners = edges.get(state);
        if (listeners == null)
            return false;
        for (Map.Entry<Type, Edge> e : listeners.entrySet()) {
            OnTryChangeListener onTryChangeListener = e.getValue().onTryChangeListener;
            if (onTryChangeListener == null)
                return false;
            if (onTryChangeListener.onTryChange()) {
                Type newState = e.getKey();
                setStateInternal(newState, null);
                return true;
            }
        }
        return false;
    }
}
