import os
import asyncio
import subprocess
from telethon import TelegramClient

def get_git_info():
    author = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%an']).decode()
    message = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%s']).decode()
    short_hash = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%h']).decode()
    return author, message, short_hash

api_id = int(os.getenv("API_ID"))
api_hash = os.getenv("API_HASH")
bot_token = os.getenv("BOT_TOKEN")
chat_id = int(os.getenv("CHAT_ID"))
topic_id = os.getenv("TOPIC_ID")
apk_path = os.getenv("APK_PATH")
run_number = os.getenv("GITHUB_RUN_NUMBER")

author, message, short_hash = get_git_info()

client = TelegramClient("bot_session", api_id, api_hash)

def progress(current, total):
    percent = current * 100 / total
    print(f"{percent:.1f}% uploaded", end="\r")

async def main():
    if not os.path.exists(apk_path):
        print("❌ APK not found:", apk_path)
        return

    await client.start(bot_token=bot_token)

    caption = (
        f"**Commit by:** {author}\n"
        f"**Message:** {message}\n"
        f"**Build:** #{run_number}\n"
        f"**Commit:** `{short_hash}`\n"
        f"**Version:** Android >= 8"
    )

    print("📤 Sending:", apk_path)

    await client.send_file(
        entity=chat_id,
        file=apk_path,
        caption=caption,
        parse_mode="markdown",
        progress_callback=progress,
        reply_to=int(topic_id) if topic_id else None
    )

    print("\n✅ Sent successfully")
    await client.disconnect()

asyncio.run(main())