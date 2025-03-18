## **Start Documnet - Design Patterns**

### **Assignment: Smart Home Automation System**

The developers are developing a **Smart Home Automation System** that manages smart devices (such as lights, thermostats, and security cameras). The system should be able to:

- Use the **Facade Pattern** to provide a simple interface for managing devices.
- Dynamically create different types of smart devices using a **Factory Method Pattern**.
- Notify users when a device's state changes using the **user.Observer Pattern**.

------

### **Requirements**

#### **1. Facade Pattern (SmartHomeControllerUI)**

- **SmartHomeController** class that acts as a **facade** for managing all smart devices.
- It should have methods like:
  - `addDevice(String type, String name)`: Adds a new smart device using the factory.
  - `removeDevice(String name)`: Removes a device from the system.
  - `changeDeviceState(String name, String newState)`: Updates a device's state.
  - `subscribeUser(String deviceName, user.User user)`: Adds a user as an observer.
- Internally, the controller should use a `List<SmartDevice>` to store devices.

#### **2.  Factory Method Pattern (Smart Device Creation)**

- Define an **abstract class** `SmartDevice` with attributes like `deviceName` and `status`.
- Implement concrete classes:
  - `SmartLight` (on/off state)
  - `SmartThermostat` (temperature settings)
  - `SmartCamera` (recording on/off)
- Create a factory class `SmartDeviceFactory` with a method `createDevice(String type, String name)` that returns a corresponding `SmartDevice`.

#### **3. user.Observer Pattern (Device State Notifications)**

- Create an `user.Observer` interface with `update(String message)`.
- Implement a `user.User` class that subscribes to smart devices and receives notifications.
- Each `SmartDevice` should maintain a list of observers (users) and notify them when their status changes.
- The `addObserver(user.User user)` and `removeObserver(user.User user)` methods should allow users to subscribe/unsubscribe.



##### **Architecture:**

The camera system follows a MVVM (Model-View-ViewModel) architecture

**Class Diagram:**

https://files.oaiusercontent.com/file-L7ERneAQybzCiFfKB9xfWo?se=2025-03-18T10%3A16%3A51Z&sp=r&sv=2024-08-04&sr=b&rscc=max-age%3D299%2C%20immutable%2C%20private&rscd=attachment%3B%20filename%3DScreenshot%25202025-03-18%2520at%252011.11.26.png&sig=fie0395dQYAAYm9HXmHlRWq3A0Q0br5s2qbsQYad1Ak%3D![image](https://github.com/user-attachments/assets/ae77a56d-dd86-4dee-91d3-86b7722fc4a6)




**MOSCOW analysis:**

| MUST have                                                    | SHOULD have                              | COULD have                                          | WON'T have                             |
| ------------------------------------------------------------ | ---------------------------------------- | --------------------------------------------------- | -------------------------------------- |
| Add/remove a user as an observer                             | Clears all devices                       | Retrieve the history of state change                | Support for voice assistants           |
| Add/remove devices                                           | `SmartDevice` stores a list of observers | Ability to group devices (e.g., "Living Room" group | A mobile interface for remote control. |
| Change device status                                         | Video door bells                         | Users can filter notifications                      |                                        |
| Checks if the device is active or disconnected               |                                          |                                                     |                                        |
| `user.User` class implementing `user.Observer`, receiving notifications |                                          |                                                     |                                        |



**Information about developers:**

| Developer name         | Email                                                        |
| ---------------------- | ------------------------------------------------------------ |
| Polina Zueva           | [polly.zueva@student.nhlstenden.com](mailto:polly.zueva@student.nhlstenden.com) |
| Ameli Masewge Fernando | <ameli.masewge.fernando@student.nhlstenden.com>              |
