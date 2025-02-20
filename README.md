# SMS2Telegram

Barebone background service that forwards your incoming new SMS (text message) and incoming call phone number into a chat with a telegram bot.
This program does not connect to any intermediate server, it simply issues a new request to telegram bot API each time the phone gets a new SMS or call.


## Supported System

Only tested on

1. Oneplus 5T on Android 10 (dual SIM)
2. Oneplus 7t on Android 11 (dual SIM)
3. Pixel 3 on Android 11
4. Moto Z Play (dual SIM)

## Configuration

You'll need (1) a telegram bot key (2) the telegram chat id to which you want the bot to forward.
You should be able to follow https://dev.to/rizkyrajitha/get-notifications-with-telegram-bot-537l to obtain your telegram bot key and chat id.

You'll also want to turn off battery optimisation for this APP to avoid it being killed by the system.


## Download

See https://github.com/hyhugh/SMS2Telegram/tree/master/app/release
