# Android TV Remote Input

Background Android TV utility for assigning two-digit remote codes to TV inputs.

First-run: assign codes, save once, then the app is prepared to start on every TV boot.

Important platform limitation: a normal Android TV app cannot globally intercept numeric remote keys while another app has focus. Global key interception and physical HDMI/input switching require a supported system/OEM API or privileged mechanism.