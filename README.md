# Smart Home Application

Welcome to the Smart Home Application! This application allows users to manage and monitor smart devices in their home environment.

# 🏡 Smart Home Control Application – User Guide  

## ⚠️ Disclaimer  
If you encounter errors related to **JavaFX**, it is likely that JavaFX is not installed on your machine. To install it, follow these steps:  

### **🔧 Installing JavaFX**  
1. **📥 Download** JavaFX from the official website: https://gluonhq.com/products/javafx/
2. **📂 Move** the downloaded JavaFX folder to the directory where your **JDK** is installed.  
3. **🛠 Configure JavaFX in IntelliJ IDEA:**  
   - Open your project in **IntelliJ IDEA**.  
   - Go to **⚙️ Project Structure** → **📚 Global Libraries**.  
   - Click **➕**, select **Java**, navigate to the **JavaFX installation folder**, open the `lib` folder, and select all files. Click **✅ Apply**.  
   - Go to the **📚 Libraries** tab, click **➕**, select **Java**, navigate to the **JavaFX installation folder**, open the `lib` folder, and select all files. Click **✅ Apply** and then **✔️ OK**.  

---

## **🚀 How to Use the Application**  

### **📲 Adding and Managing Devices**  
1. **🔍 Select** the type of device.  
2. **✏️ Enter** a name for the device.  
3. Click **➕ Add Device**.  
4. Select the newly added device (or it will be selected by default).  
5. **🔄 Change the state** of the device by clicking the **⚡ State** button.  

### **📊 Device Status and Actions**  
- The **📋 "Device Status"** tab displays all updates related to the selected device.  
- **✅ Check Status**: Shows the current status of the selected device.  
- **🗑 Remove Device**: Removes the device from the system.  

---

## **📡 Device-Specific Controls**  

### **📸 Camera**  
- The **📷 Take Photo** and **🖼 Photo Gallery** buttons appear **only** when a **camera device** is created and selected.  

### **💡 Light**  
- Navigate to the **💡 Light Controls** tab to adjust light settings.  
- All changes will be displayed in the **📋 Device Status** tab.  

### **🌡 Thermostat**  
- **🌡 Current Temperature State**: Displays the current temperature.  
- **❄️ Cooling State**: Lowers the temperature. Click **⚡ Apply State** again to decrease it further, then click **✅ Check Status**.  
- **🔥 Heating State**: Increases the temperature. Click **⚡ Apply State** again to raise it further, then click **✅ Check Status**.  

---

## **🗣 Voice Assistant**  
1. **🎙 Select** the Voice Assistant device.  
2. Navigate to the **🎤 Voice Assistant** tab.  
3. Click **🎧 Activate Listening**:  
   - **🟢 Green circle** → The assistant is **listening**.  
   - **🟡 Natural state** → The assistant is **idle**.  
   - **🔴 Red circle** → The assistant is **muted**.  

### **📜 Commands and History**  
- **📜 Command History**: View past voice commands.  
- **⌨️ Typing Requests**: You can manually enter commands like:  
  - 🕰 `"Time"`  
  - 🌡 `"Temperature"`  
  - 🌦 `"Weather"`  
  - 📅 `"Date"`  

**⚠️ Note:** The voice assistant **only works when in the listening state**.  

---

This guide should help you **smoothly navigate** and **enjoy using** the **🏠 Smart Home Control application!** 🚀

For further assistance, refer to Ameli Fernando & Polina Zueva.
