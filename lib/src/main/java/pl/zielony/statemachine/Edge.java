package pl.zielony.statemachine;

class Edge {

    OnTryChangeListener onTryChangeListener;
    OnStateChangedListener onStateChangedListener;

    Edge(OnTryChangeListener onTryChangeListener, OnStateChangedListener onStateChangedListener) {
        this.onTryChangeListener = onTryChangeListener;
        this.onStateChangedListener = onStateChangedListener;
    }
}
