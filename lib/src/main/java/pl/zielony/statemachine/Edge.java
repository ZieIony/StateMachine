package pl.zielony.statemachine;

/**
 * Created by Marcin on 2016-11-25.
 */
class Edge {

    OnTryChangeListener onTryChangeListener;
    OnStateChangedListener onStateChangedListener;

    Edge(OnTryChangeListener onTryChangeListener, OnStateChangedListener onStateChangedListener) {
        this.onTryChangeListener = onTryChangeListener;
        this.onStateChangedListener = onStateChangedListener;
    }
}
