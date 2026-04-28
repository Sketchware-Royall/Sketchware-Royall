import os
import subprocess
import requests

def get_git_commit_info():
    commit_author = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%an']).decode()
    commit_message = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%s']).decode()
    commit_hash_short = subprocess.check_output(['git', 'log', '-1', '--pretty=format:%h']).decode()
    return commit_author, commit_message, commit_hash_short

bot_token = os.getenv("BOT_TOKEN")
chat_id = os.getenv("CHAT_ID")
topic_id = os.getenv("TOPIC_ID")
apk_path = os.getenv("APK_PATH")

commit_author, commit_message, commit_hash_short = get_git_commit_info()

def human_readable(size):
    for unit in ['B','KB','MB','GB']:
        if size < 1024:
            return f"{size:.2f} {unit}"
        size /= 1024

def send_apk():
    if not os.path.exists(apk_path):
        print("APK not found:", apk_path)
        return

    url = f"https://api.telegram.org/bot{bot_token}/sendDocument"

    caption = (
        f"*Commit by:* {commit_author}\n"
        f"*Message:* {commit_message}\n"
        f"*Build:* #{os.getenv('GITHUB_RUN_NUMBER')}\n"
        f"*Commit:* `{commit_hash_short}`\n"
        f"*Version:* Android >= 8"
    )

    size = human_readable(os.path.getsize(apk_path))
    print(f"Sending APK ({size})...")

    with open(apk_path, "rb") as f:
        data = {
            "chat_id": chat_id,
            "caption": caption,
            "parse_mode": "Markdown"
        }

        if topic_id:
            data["message_thread_id"] = int(topic_id)

        response = requests.post(url, data=data, files={"document": f})

    if response.status_code == 200:
        print("✅ Sent successfully")
    else:
        print("❌ Failed:", response.text)

if __name__ == "__main__":
    send_apk()