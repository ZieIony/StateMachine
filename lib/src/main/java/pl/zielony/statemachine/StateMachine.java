package pl.zielony.statemachine;

import android.os.Bundle;

import com.annimon.stream.Stream;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StateMachine<Type extends Serializable> {
    private static final String STATE = "state";

    private final Type initialState;
    private Type state;

    private Map<Type, Map<Type, Edge>> edges = new HashMap<>();
    private Map<Type, OnStateChangedListener> states = new HashMap<>();
    private OnStateChangedListener globalStateListener;

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
            throw new IllegalStateException("Cannot change state from " + state + " to " + newState);
        setStateInternal(newState, param);
        update();
    }

    private <ParamType> void setStateInternal(Type newState, ParamType param) {
        Edge edge = edges.get(state).get(newState);
        state = newState;
        OnStateChangedListener listener = edge.onStateChangedListener;
        if (listener != null)
            listener.onStateChanged(param);
        OnStateChangedListener stateListener = states.get(newState);
        if (stateListener != null)
            stateListener.onStateChanged(param);
        if (globalStateListener != null)
            globalStateListener.onStateChanged(param);
    }

    public void reset() {
        state = initialState;
    }

    public void addEdge(Type stateFrom, Type stateTo) {
        addEdge(stateFrom, stateTo, null, null);
    }

    public void addEdge(Type stateFrom, Type stateTo, OnStateChangedListener changeListener) {
        addEdge(stateFrom, stateTo, null, changeListener);
    }

    public void addEdge(Type stateFrom, Type stateTo, OnTryChangeListener tryListener) {
        addEdge(stateFrom, stateTo, tryListener, null);
    }

    public void addEdge(Type stateFrom, Type stateTo, OnTryChangeListener tryListener, OnStateChangedListener changeListener) {
        if (stateFrom == stateTo)
            throw new IllegalArgumentException("Both states have the same value: '" + stateFrom + "'");
        Edge edge = new Edge(tryListener, changeListener);
        if (!edges.containsKey(stateFrom)) {
            Map<Type, Edge> list = new HashMap<>();
            list.put(stateTo, edge);
            edges.put(stateFrom, list);
        } else if (edges.get(stateFrom).get(stateTo) != null) {
            throw new IllegalStateException("There's already an edge from state '" + stateFrom + "' to '" + stateTo + "'");
        } else {
            long emptyListeners = Stream.of(edges.get(stateFrom).values()).filter(e -> e.onTryChangeListener == null).count();
            if (emptyListeners < edges.get(stateFrom).size() && tryListener == null || emptyListeners > 0 && tryListener != null)
                throw new IllegalStateException("Nondeterministic state change from '" + stateFrom + "'");
            edges.get(stateFrom).put(stateTo, edge);
        }
    }

    @Deprecated
    public void addState(Type state, OnMachineStateChangedListener listener) {
        addStateListener(state, listener::onStateChanged);
    }

    public void addStateListener(Type state, OnStateChangedListener listener) {
        if (states.containsKey(state))
            throw new IllegalStateException("There's already a state '" + state + "'");
        states.put(state, listener);
    }

    public boolean hasEdge(Type stateFrom, Type stateTo) {
        return edges.containsKey(stateFrom) && edges.get(stateFrom).containsKey(stateTo);
    }

    public Type getState() {
        return state;
    }

    @Deprecated
    public void setOnStateChangeListener(OnMachineStateChangedListener stateListener) {
        setOnStateChangedListener(stateListener::onStateChanged);
    }

    public void setOnStateChangedListener(OnStateChangedListener stateListener) {
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
