# 📖 Information

This `AddOn` is designed to report players to admins using AI-powered summaries.

# ✨ Features

Ask AI to generate a summary report using:

```bash
/report {player name} <msg>              # Default
/report {player name} <platform> <model> # Requires permission: mcengine.artificialintelligence.addon.report.summary
```

It will send the player's name, UUID, and report details to the AI and return a summarized response.

✅ Only unsent reports will be processed.  
The database marks each report as "used" once it has been sent, so only new/unprocessed reports will be included.

# ⚠️ Important

This AddOn uses a secure server token to communicate with the AI 🔒.

Only players with the appropriate permission can use this command.
