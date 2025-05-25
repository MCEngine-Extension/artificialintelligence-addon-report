# 📖 Information

This `AddOn` is designed to report players to admins using AI-powered summaries.

# ✨ Features

Ask AI to generate a summary report like:

```bash
/report me {player name}
```

It will send the player's name, UUID, and report details to the AI and return a summarized response.

✅ Only unsent reports will be processed.  
The database marks each report as "used" once it has been sent, so only new/unprocessed reports will be included.

# ⚠️ Important

This AddOn uses a secure server token to communicate with the AI 🔒.

The `/report me` command requires the `mcengine.report.ai` permission.  
Only OPs or players with this permission can use the command.
