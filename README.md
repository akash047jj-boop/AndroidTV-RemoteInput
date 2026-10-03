# Android TV Remote Input — v2

Target: BPL Android TV 11 / AI PONT, build RTMA.250416.061.

## v2
- Six setup targets: Google TV Home, Antenna, ATV, AV, HDMI 1 / MiBOX, HDMI 2 / PlayStation 5.
- Assign a unique 2-digit code to each.
- Discovers real TvInputInfo IDs from the TV instead of hard-coding HDMI IDs.
- Diagnostics shows label, ID, type and passthrough status.
- Uses AccessibilityService key filtering after the user explicitly enables it.
- Two digits within 1.8 seconds trigger a confirmation screen.
- HDMI/other passthrough inputs use the public TvContract passthrough URI.
- Tuner inputs also try firmware/system-TV intents, because Android has no general public setCurrentInput() API.
- Home uses the normal HOME intent.
- Boot receiver and foreground status service are included.

## Limitation
Android's public TV APIs do not expose a general third-party input-switch command. TvContract documents a passthrough URI and TvInputInfo exposes whether an input is passthrough. Whether a BPL firmware actually switches the physical input from these intents must be tested on the user's TV.

Accessibility services must be explicitly enabled by the user; they cannot silently enable themselves.

## Test
1. Install the debug APK.
2. Open Remote Input.
3. Refresh TV inputs.
4. Assign codes and save.
5. Enable **Remote Input** in Accessibility settings.
6. Enter a configured two-digit code from another app.
7. Confirm the switch.
8. If an input fails, open Diagnostics and send the displayed IDs/type/passthrough status.

GitHub Actions builds the debug APK on pushes to main.