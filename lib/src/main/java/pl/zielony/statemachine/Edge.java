package pl.zielony.statemachine;

/**
 * Created by Marcin on 2016-11-25.
 */
public class Edge {
    public interface OnTryChangeListener {

        boolean onTryChange();
    }

    public interface OnStateChangedListener {
        void onStateChanged();
    }

    OnTryChangeListener onTryChangeListener;
    OnStateChangedListener onStateChangedListener;

    Edge(OnTryChangeListener onTryChangeListener, OnStateChangedListener onStateChangedListener) {
        this.onTryChangeListener = onTryChangeListener;
        this.onStateChangedListener = onStateChangedListener;
    }
}
