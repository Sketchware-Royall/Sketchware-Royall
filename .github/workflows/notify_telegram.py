import os
import subprocess
import requests
import re
import time

BOT_TOKEN = os.environ['BOT_TOKEN']
CHAT_ID = os.environ['CHAT_ID']
TOPIC_ID = os.environ.get('TOPIC_ID')
APK_PATH = os.environ.get('APK_PATH')  # optional


def run(cmd):
    return subprocess.check_output(cmd).decode().strip()


def get_git_info():
    return {
        "author": run(['git', 'log', '-1', '--pretty=format:%an']),
        "message": run(['git', 'log', '-1', '--pretty=format:%s']),
        "hash": run(['git', 'log', '-1', '--pretty=format:%H']),
        "short": run(['git', 'log', '-1', '--pretty=format:%h'])
    }


def esc(text):
    return re.sub(r'([_~`#+\-=|{}.!])', r'\\\1', text)


def send_message(text):
    url = f"https://api.telegram.org/bot{BOT_TOKEN}/sendMessage"
    payload = {
        "chat_id": CHAT_ID,
        "text": esc(text),
        "parse_mode": "MarkdownV2",
        "disable_web_page_preview": True
    }
    if TOPIC_ID:
        payload["message_thread_id"] = TOPIC_ID

    return requests.post(url, json=payload)


def send_apk(path):
    if not path or not os.path.exists(path):
        print("APK not found, skipping...")
        return

    url = f"https://api.telegram.org/bot{BOT_TOKEN}/sendDocument"
    data = {"chat_id": CHAT_ID}
    if TOPIC_ID:
        data["message_thread_id"] = TOPIC_ID

    with open(path, "rb") as f:
        r = requests.post(url, data=data, files={"document": f})

    print("APK upload:", r.status_code, r.text)


def main():
    git = get_git_info()

    msg = (
        f"🚀 New commit by *{git['author']}*\n\n"
        f"[View Commit](https://github.com/Sketchware-Royall/Sketchware-Royall/commit/{git['hash']})\n\n"
        f"*Changes:*\n> {git['message']}\n\n"
        f"#{git['short']}"
    )

    r = send_message(msg)

    if r.status_code != 200:
        print("Message failed:", r.text)
        return

    print("Message sent ✅")

    # Optional: wait for build artifact
    if APK_PATH:
        print("Waiting for APK...")
        time.sleep(20)  # adjust if needed
        send_apk(APK_PATH)


if __name__ == "__main__":
    main()