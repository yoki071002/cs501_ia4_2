# Counter++ – Reactive UI with StateFlow & Coroutines

A counter that can increase or reduce the value by clicking the button, while there's a back-end auto increment by certain seconds, which the default time interval is 3 seconds, and users are allowed to customize the interval.

- Use ViewModel to manage the counter state.
- Implement StateFlow for unidirectional data flow.
- Add buttons for +1, -1, and Reset.
- Launch a coroutine that increments the counter every 3 seconds when “Auto” mode is toggled on.
- Display current count and status (“Auto mode: ON/OFF”).
- Add a settings screen to configure the auto-increment interval.
