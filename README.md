# 📔 Moments Journal — A Modern Journaling App (Open Source Initiative)

Welcome to the **Moments Journal** project — a full-featured digital journaling platform that helps users document their lives in a seamless and expressive way. This is an **open-source project** for both **web and mobile (React)**, powered by a **Spring Boot backend**.

We’re building a collaborative space for:
- 🎨 Product Designers  
- 💻 React (Web/Mobile) Developers  
- ☕ Java Backend Developers  

---

## 🔧 Core Features

### 1. 🔐 User Authentication  
- **Registration:**  
  - Fields: `First Name`, `Last Name`, `Username`, `Email`, `Password`  
- **Email Verification:**  
  - Users receive a verification link via email  
  - Option to resend verification  
  - Clicking the link activates the account  
- **Login:**  
  - Via **email** or **username** + password  

---

### 2. 📓 Journals and Entries  
- Users can create **multiple journals**  
- Each journal contains multiple **entries**

**Entry Fields:**
- `Title`  
- `Entry Date` (can be backdated)  
- `Rich Text Body`  
  - Includes formatting (bold, italic, bullet points, etc.)  
  - Image uploads  
- `Tags` (optional, editable anytime)

---

### 3. 🎙️ Speech to Text  
- Add journal entry content via **voice input**  
- Real-time speech-to-text functionality  
- Inspired by **ChatGPT voice input interface**

---

### 4. 🔄 Versioning & Recycle Bin  
- All edits are **versioned**  
- Users can:
  - View previous versions  
  - Restore or delete specific versions  
- Deleted entries go to **Recycle Bin**  
  - Stored for **30 days** before permanent deletion  

---

### 5. 🔎 Smart Search  
- Global search across **all journals**  
- Journal-specific search  
- Filter results by:
  - `Title`  
  - `Tags`  

---

### 6. 🔐 Journal Controls  
Each journal supports:  
- Viewing all entries  
- Creating new entries  
- Searching within itself  
- Optionally **locked** for privacy  

---

### 7. 📅 Email Reminders  
- Intelligent reminder system  
- Encourages regular journaling  

---

## 🤝 Contributing  
We welcome contributions! Please check our `CONTRIBUTING.md` for guidelines and open issues.
