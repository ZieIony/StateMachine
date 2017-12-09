# StateMachine

A simple state machine. Has state change listeners, automatic state updates and conditional edges. This state machine is very strict - it's not possible to create a machine with duplicated states, duplicated edges or nondeterministic state changes.

State machine can be used to keep track of a state of a complex screen. UI can be changed in state change listeners. State machine's state can be saved and restored in order to handle Activity's configuration changes.

```Java
enum SampleState {
    NEW, IDLE, LOADING, DONE, ERROR
}

private StateMachine<SampleState> stateMachine = new StateMachine<>(SampleState.NEW);

public void setUp() throws Exception {
    stateMachine.addStateListener(SampleState.LOADING, param -> {
        loadButton.setEnabled(false);
    });
    stateMachine.addStateListener(SampleState.DONE, param -> {
        loadButton.setEnabled(true);
    });

    stateMachine.addEdge(SampleState.NEW, SampleState.IDLE);
    stateMachine.addEdge(SampleState.IDLE, SampleState.LOADING, param -> loadFile());
    stateMachine.addEdge(SampleState.LOADING, SampleState.DONE, () -> bytesLoaded == FILE_SIZE);
    stateMachine.addEdge(SampleState.LOADING, SampleState.ERROR);
    
    stateMachine.setState(SampleState.IDLE);
    
    loadButton.setOnClickListener(view -> {
        stateMachine.setState(SampleState.LOADING);
    });
}

public void onSaveInstanceState(Bundle bundle){
    stateMachine.save(bundle);
}

public void onRestoreInstanceState(Bundle bundle){
    stateMachine.restore(bundle);
}
```
