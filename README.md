📔 Moments Journal — A Modern Journaling App (Open Source Initiative)
Welcome to the Moments Journal project!
This is a full-featured digital journaling platform designed to help users document their lives in a seamless and expressive way. Our goal is to build an open-source journaling tool for both web and mobile (React) with a powerful Spring Boot backend, and we’re inviting product designers, React (web/mobile) developers, and Java backend developers to collaborate.

🔧 Core Features
1. 🔐 User Authentication
Registration: Collects first name, last name, username, email, and password.

Email Verification: After signing up, users receive a verification link via email (resend option available). Clicking the link confirms and verifies the account.

Login: Users can sign in with either their email or username and password.

2. 📓 Journals and Entries
Users can create multiple journals.

Each journal contains entries tied to it.

An entry includes:

Title

Entry Date (can be backdated)

Rich Text Body with formatting and image uploads

Tags (optional; can be added/edited/removed later)

3. 🎙️ Speech to Text
Users can add content to journal entries via voice input (speech-to-text), similar to ChatGPT’s voice interface.

4. 🔄 Versioning & Recycle Bin
Every edit to an entry is tracked with version history. Users can view, restore, or delete versions.

Deleted entries are moved to a Recycle Bin for 30 days before permanent deletion.

5. 🔎 Smart Search
Search across all journals or within a single journal.

Filter by title or tags.

6. 🔐 Journal Controls
Journals can be locked (optional).

Each journal supports:

Viewing all entries

Creating new entries

Searching within the journal

7. 📅 Email Reminders
Users receive intelligent reminders:

Daily journaling prompts

Streak updates to build habit

"On This Day" reminders (e.g., entries written X months ago)

Anniversary reminders (entries written exactly 1 year ago)

8. 💾 AutoSave
Entries are automatically saved as users write.

🧩 Project Structure
Backend: Spring Boot (Java)

Frontend Web: React.js

Frontend Mobile: React Native (Expo)

✅ What’s Ready
✅ Spring Boot backend (on GitHub)

✅ User authentication

✅ Journal and entry management APIs

🧑‍💻 How to Contribute
We’re currently looking for contributors in:

Frontend Web (React.js): UI implementation of features like login, journal view, entry creation, versioning, and speech-to-text.

Frontend Mobile (React Native): Mobile-first experience with speech-to-text, reminders, and journal navigation.

Backend (Spring Boot): Email reminders logic, version tracking, recycle bin cleanup job, tagging enhancements.
