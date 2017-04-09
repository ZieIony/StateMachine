package pl.zielony.statemachine;

/**
 * Created by Marcin on 2016-11-25.
 */
class Edge {

    OnTryChangeListener onTryChangeListener;
    OnStateChangeListener onStateChangedListener;

    Edge(OnTryChangeListener onTryChangeListener, OnStateChangeListener onStateChangedListener) {
        this.onTryChangeListener = onTryChangeListener;
        this.onStateChangedListener = onStateChangedListener;
    }
}
